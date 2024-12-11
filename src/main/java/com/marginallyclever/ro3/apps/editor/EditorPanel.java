package com.marginallyclever.ro3.apps.editor;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.apps.shared.PersistentJFileChooser;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodeDetachListener;
import com.marginallyclever.ro3.node.nodes.marlinrobot.MarlinListener;
import com.marginallyclever.ro3.node.nodes.marlinrobot.MarlinRobot;
import com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm.MarlinRobotArm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

/**
 * <p>{@link EditorPanel} is a panel for editing GCode that can be sent to a {@link MarlinRobotArm}.</p>
 * <p>It can also listen to a {@link MarlinRobotArm}, which generates GCode and status messages.</p>
 *
 * <p>{@link com.marginallyclever.ro3.node.Node}s build up to form a robot arm in 3d like Pose, Mesh, DH parameter, HingeJoint, Motor, and finally connect
 * it all to a {@link MarlinRobotArm} node (1)  that translates arm state to gcode and back.  With the node selected more info
 * is available in the <b>details</b> tab (2).  I can see the last output from the arm in the <b>output</b> field because it is
 * registered to listen for {@link MarlinListener} events.  The Editor can be assigned an existing MarlinRobotArm (3).
 * While it has one, all <b>MarlinEvent</b>s will be written to the status bar (8).</p>
 * <p>If the <b>get</b> toggle is on, the next event from MarlinRobotArm will be written out at the caret position (7).
 * <p>If the <b>lock</b> toggle is on then <b>get</b> will stay on until further notice.</p>
 * <p>If the <b>send</b> button is pressed, the current line of gcode is transmitted to the robot.  Any response from
 * the robot may be written to file, depending on the state of the <b>get</b> toggle.</p>
 * <p>There is room for more editing tools here like save, load, copy, cut, paste, undo, redo, etc.</p>
 */
public class EditorPanel extends App implements MarlinListener, PropertyChangeListener, NodeDetachListener, SceneChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(EditorPanel.class);
    private static final double PROGRESS_BAR_SCALE = 1000;
    private double reportInterval = 1.0;
    private static final int TIMER_INTERVAL_MS = 100;
    private final NodeSelector<MarlinRobot> marlinRobot = new NodeSelector<>(MarlinRobot.class);
    private final JTextArea text = new JTextArea();
    private final JLabel statusLabel = new JLabel();
    private static final JFileChooser chooser = new PersistentJFileChooser();
    private final JButton newButton = new JButton(new NewAction(this));
    private final JButton loadButton = new JButton(new LoadAction(this,chooser));
    private final JButton saveButton = new JButton(new SaveAction(this,chooser));
    private final JToolBar toolBar = new JToolBar("tools");
    private final JToggleButton playToggle = new JToggleButton(new AbstractAction() {
        // constructor
        {
            putValue(Action.NAME, "Play");
            putValue(Action.SHORT_DESCRIPTION, "Send the current line to the robot.");
            putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-play-16.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            recordToggle.setSelected(false);
        }
    });

    private final JToggleButton recordToggle = new JToggleButton(new AbstractAction() {
        // constructor
        {
            putValue(Action.NAME, "Record");
            putValue(Action.SHORT_DESCRIPTION, "Record events from an arm.");
            putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource(
                    "icons8-record-16.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            playToggle.setSelected(false);
        }
    });
    private JFormattedTextField secondsField;
    private final JToggleButton runToggle = new JToggleButton("Start");
    private final JProgressBar progressBar = new JProgressBar();
    private final Timer timer = new Timer(TIMER_INTERVAL_MS, null);
    private final ActionListener timerAction = (e)-> {
        int value = progressBar.getValue() + TIMER_INTERVAL_MS;
        if (value >= progressBar.getMaximum()) {
            value = 0;
            if(playToggle.isSelected()) playOnce();
            else if(recordToggle.isSelected()) recordOnce();
        }
        progressBar.setValue(value);
    };

    public EditorPanel() {
        super(new BorderLayout());
        text.setName("text");
        statusLabel.setName("status");
        addToolBar(toolBar);
        add(toolBar, BorderLayout.NORTH);

        var scroll = new JScrollPane();
        scroll.setViewportView(text);
        add(scroll, BorderLayout.CENTER);
        addStatusBar();

        Registry.addSceneChangeListener(this);
    }

    private void addStatusBar() {
        // create the status bar panel and shove it down the bottom of the frame
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);

        statusLabel.setText("No arm selected.");
    }

    private void addToolBar(JToolBar tools) {
        marlinRobot.setMaximumSize(new Dimension(150, 24));
        tools.add(marlinRobot);
        tools.add(newButton);
        tools.add(loadButton);
        tools.add(saveButton);
        tools.addSeparator();
        tools.add(playToggle);
        tools.add(recordToggle);
        tools.addSeparator();
        createReportInterval();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        marlinRobot.addPropertyChangeListener("subject", this );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        marlinRobot.removePropertyChangeListener("subject", this );
    }

    private void createReportInterval() {
        // report interval
        var formatter = NumberFormatHelper.getNumberFormatterDouble();
        formatter.setMinimum(0.05);
        secondsField = new JFormattedTextField(formatter);
        secondsField.setToolTipText("Time interval in seconds.");
        secondsField.setMaximumSize(new Dimension(100, 24));
        secondsField.addPropertyChangeListener("value", evt -> {
            if (evt.getSource() == secondsField) {
                setReportInterval((double) evt.getNewValue());
            }
        });
        setSecondsField(secondsField);

        // then a toggle to turn it on and off.
        runToggle.setToolTipText("Click to start reporting.");
        runToggle.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/apps/icons8-stopwatch-16.png"))));
        runToggle.addActionListener(e -> {
            if (runToggle.isSelected()) {
                runToggle.setText("Stop");
                runToggle.setToolTipText("Click to stop reporting.");
                timer.addActionListener(timerAction);
                timer.start();
            } else {
                runToggle.setText("Start");
                runToggle.setToolTipText("Click to start reporting.");
                progressBar.setValue(0); // Reset progress bar when toggle is off
                timer.stop();
                timer.removeActionListener(timerAction);
            }
        });

        // if the window closes, stop the timer.
        // TODO version 2 keep it going?
        runToggle.addHierarchyListener(e -> {
            if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) !=0
                    && !runToggle.isShowing()) {
                timer.stop();
                timer.removeActionListener(timerAction);
            }
        });

        updateLabels();

        // Add components to the toolbar
        toolBar.add(secondsField);
        toolBar.add(runToggle);
        toolBar.add(progressBar);
    }

    private void updateLabels() {
        setSecondsField(secondsField);
        var arm = marlinRobot.getSubject();
        if(arm!=null) {
            playToggle.setEnabled(true);
            recordToggle.setEnabled(true);
            runToggle.setEnabled(true);
            statusLabel.setText("Selected: " + arm.getName());
        } else {
            playToggle.setEnabled(false);
            recordToggle.setEnabled(false);
            runToggle.setEnabled(false);
            statusLabel.setText("No robot selected.");
        }
        progressBar.setMaximum((int)(getReportInterval() * PROGRESS_BAR_SCALE));
    }

    /**
     * @return the time between reports in seconds.
     */
    private double getReportInterval() {
        return reportInterval;
    }

    /**
     * @param seconds the time between reports in seconds.  Must be >= 0.
     */
    private void setReportInterval(double seconds) {
        if(seconds<=0) throw new IllegalArgumentException("seconds must be > 0");
        reportInterval = seconds;
        progressBar.setMaximum((int)(getReportInterval() * PROGRESS_BAR_SCALE));
    }

    private void setSecondsField(JFormattedTextField secondsField) {
        secondsField.setValue(getReportInterval());
    }

    /**
     * @return the line of text at the current caret position, or an empty string.
     */
    private String getLineAtCaret() {
        // get the current line of the document at the caret position.
        int caretPosition = text.getCaretPosition();
        Element root = text.getDocument().getDefaultRootElement();
        int lineNumber = root.getElementIndex(caretPosition);
        Element lineElement = root.getElement(lineNumber);
        int start = lineElement.getStartOffset();
        int end = lineElement.getEndOffset();
        try {
            return text.getDocument().getText(start, end - start);
        } catch (BadLocationException exception) {
            logger.error("Failed to get line from document.",exception);
        }
        return "";
    }

    @Override
    public void messageFromMarlin(String message) {
        statusLabel.setText(message);
        if(recordToggle.isSelected()) {
            if (!message.startsWith("Error")) {
                if (message.startsWith("Ok:")) message = message.substring(3);    // remove
                if (message.startsWith("Ok")) message = message.substring(2);    // remove
                message = message.trim();
                if(!message.isEmpty()) {
                    message += "\n";
                    text.insert(message, text.getCaretPosition());
                    int newCursorPos = text.getCaretPosition() + message.length();
                    text.setCaretPosition(Math.min(newCursorPos, text.getDocument().getLength()));
                }
            }
        }
    }

    public void setText(String s) {
        text.setText(s);
    }

    public String getText() {
        return text.getText();
    }

    public void reset() {
        setText("");
        recordToggle.setSelected(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() != marlinRobot) return;
        robotHasChanged(evt);
        updateLabels();
    }

    private void robotHasChanged(PropertyChangeEvent evt) {
        // make old arm forget me.
        MarlinRobot oldRobot = ((MarlinRobot)evt.getOldValue());
        if(oldRobot!=null) {
            oldRobot.removeMarlinListener(EditorPanel.this);
            oldRobot.removeDetachListener(this);
        }

        // make new arm remember me.
        MarlinRobot newRobot = marlinRobot.getSubject();
        if(newRobot!=null) {
            newRobot.addMarlinListener(EditorPanel.this);
            Node parent = newRobot.getParent();
            if(parent != null) parent.addDetachListener( this );
        }
    }

    @Override
    public void nodeDetached(Node child) {
        if(marlinRobot.getSubject() == child) {
            // robot arm has been removed from the scene tree.
            // Remove, Cut or Move will cause this to trigger.
            Node parent = child.getParent();
            if(parent != null) parent.removeDetachListener( this );
            marlinRobot.setSubject(null);
            updateLabels();
        }
    }

    private void playOnce() {
        var arm = marlinRobot.getSubject();
        if(arm==null) return;

        // get the next line that is not blank.
        String message="";
        do {
            message = getLineAtCaret();
            if(!moveCaretDownOneLine()) {
                return;  // end of document or failed to move.
            }
        } while(message.trim().isBlank());

        // not the end and not blank.
        arm.sendGCode(message);
    }

    private void recordOnce() {
        var arm = marlinRobot.getSubject();
        if(arm==null) return;
        arm.sendGCode("G0");
    }

    /**
     * Move the caret down one line.
     * @return true if the caret was moved, false otherwise.
     */
    private boolean moveCaretDownOneLine() {
        int caretPosition = text.getCaretPosition();
        Element root = text.getDocument().getDefaultRootElement();
        int lineNumber = root.getElementIndex(caretPosition);
        Element lineElement = root.getElement(lineNumber);
        int end = lineElement.getEndOffset();
        if(end >= text.getDocument().getLength()) {
            statusLabel.setText("End of document.");
            return false;
        }
        // try to move
        try {
            text.setCaretPosition(Math.min(end, text.getDocument().getLength()));
        } catch (Exception exception) {
            logger.error("Failed to move caret.",exception);
            return false;
        }

        statusLabel.setText("Line "+(lineNumber+1));
        return true;
    }

    @Override
    public void beforeSceneChange(Node oldScene) {
        marlinRobot.setSubject(null);
    }

    @Override
    public void afterSceneChange(Node newScene) {}
}
