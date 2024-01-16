package com.marginallyclever.ro3.apps.editorpanel;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.shared.PersistentJFileChooser;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodeDetachListener;
import com.marginallyclever.ro3.node.nodes.marlinrobotarm.MarlinListener;
import com.marginallyclever.ro3.node.nodes.marlinrobotarm.MarlinRobotArm;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
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
public class EditorPanel extends App implements MarlinListener, PropertyChangeListener, NodeDetachListener {
    private static final Logger logger = LoggerFactory.getLogger(EditorPanel.class);
    private static final double PROGRESS_BAR_SCALE = 1000;
    private static final int TIMER_INTERVAL_MS = 100;
    private final NodeSelector<MarlinRobotArm> robotArm = new NodeSelector<>(MarlinRobotArm.class);
    private final JTextArea text = new JTextArea();
    private final JLabel statusLabel = new JLabel();
    private static final JFileChooser chooser = new PersistentJFileChooser();
    private final JButton newButton = new JButton(new NewAction(this));
    private final JButton loadButton = new JButton(new LoadAction(this,chooser));
    private final JButton saveButton = new JButton(new SaveAction(this,chooser));
    private final JToolBar toolBar = new JToolBar("tools");
    private JFormattedTextField secondsField;
    private final Timer timer = new Timer(TIMER_INTERVAL_MS, null);
    private final JProgressBar progressBar = new JProgressBar();
    private final ActionListener timerAction = (e)-> {
        int value = progressBar.getValue() + TIMER_INTERVAL_MS;
        if (value >= progressBar.getMaximum()) {
            value = 0;
            var arm = robotArm.getSubject();
            if(arm!=null) {
                arm.sendGCode("G0"); // Send G0 command when progress bar is full
            }
        }
        progressBar.setValue(value);
    };

    private final JButton sendButton = new JButton(new AbstractAction() {
        // constructor
        {
            putValue(Action.NAME, "Send");
            putValue(Action.SHORT_DESCRIPTION, "Send the current line to the robot.");
            putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-play-16.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MarlinRobotArm arm = robotArm.getSubject();
            if(arm!=null) {
                String message = getLineAtCaret();
                if(!message.trim().isEmpty()) {
                    arm.sendGCode(message);
                    // move down one line in the file.
                    int caretPosition = text.getCaretPosition();
                    Element root = text.getDocument().getDefaultRootElement();
                    int lineNumber = root.getElementIndex(caretPosition);
                    Element lineElement = root.getElement(lineNumber);
                    int end = lineElement.getEndOffset();
                    try {
                        text.setCaretPosition(Math.min(end, text.getDocument().getLength()));
                    } catch (Exception exception) {
                        logger.error("Failed to move caret.",exception);
                    }
                }
            }
        }
    });

    private final JToggleButton recordButton = new JToggleButton(new AbstractAction() {
        // constructor
        {
            putValue(Action.NAME, "Record");
            putValue(Action.SHORT_DESCRIPTION, "Record events from an arm.");
            putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource(
                    "icons8-record-16.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(recordButton.isSelected()) {
                // if there is no arm, deselect the button.
                MarlinRobotArm arm = robotArm.getSubject();
                if(arm==null) {
                    recordButton.setSelected(false);
                }
            }
        }
    });
    private final JToggleButton recordToggle = new JToggleButton("Start");

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
        robotArm.setMaximumSize(new Dimension(150, 24));
        tools.add(robotArm);
        tools.add(newButton);
        tools.add(loadButton);
        tools.add(saveButton);
        tools.addSeparator();
        tools.add(sendButton);
        tools.add(recordButton);
        tools.addSeparator();
        createReportInterval();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        robotArm.addPropertyChangeListener("subject", this );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        robotArm.removePropertyChangeListener("subject", this );
    }

    private void createReportInterval() {
        var arm = robotArm.getSubject();

        // report interval
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.05);
        secondsField = new JFormattedTextField(formatter);
        secondsField.setToolTipText("Time interval in seconds.");
        secondsField.setMaximumSize(new Dimension(100, 24));
        setSecondsField(secondsField);

        // then a toggle to turn it on and off.
        recordToggle.setToolTipText("Click to start reporting.");
        recordToggle.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/apps/icons8-stopwatch-16.png"))));
        recordToggle.addActionListener(e -> {
            if (recordToggle.isSelected()) {
                recordToggle.setText("Stop");
                recordToggle.setToolTipText("Click to stop reporting.");
                timer.addActionListener(timerAction);
                timer.start();
            } else {
                recordToggle.setText("Start");
                recordToggle.setToolTipText("Click to start reporting.");
                progressBar.setValue(0); // Reset progress bar when toggle is off
                timer.stop();
                timer.removeActionListener(timerAction);
            }
        });

        // if the window closes, stop the timer.
        // TODO keep it going and refresh the window when it re-opens.
        recordToggle.addHierarchyListener(e -> {
            if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) !=0
                    && !recordToggle.isShowing()) {
                timer.stop();
                timer.removeActionListener(timerAction);
            }
        });

        updateLabels();

        // Add components to the toolbar
        toolBar.add(secondsField);
        toolBar.add(recordToggle);
        toolBar.add(progressBar);
    }

    private void updateLabels() {
        setSecondsField(secondsField);
        var arm = robotArm.getSubject();
        if(arm!=null) {
            sendButton.setEnabled(true);
            recordButton.setEnabled(true);
            recordToggle.setEnabled(true);
            statusLabel.setText("Selected: " + arm.getName());
            progressBar.setMaximum((int)(arm.getReportInterval() * PROGRESS_BAR_SCALE));
        } else {
            sendButton.setEnabled(false);
            recordButton.setEnabled(false);
            recordToggle.setEnabled(false);
            statusLabel.setText("No arm selected.");
        }
    }

    private void setSecondsField(JFormattedTextField secondsField) {
        MarlinRobotArm arm = robotArm.getSubject();
        if(arm==null) return;
        secondsField.setValue(arm.getReportInterval());
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
        if(recordButton.isSelected()) {
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
        recordButton.setSelected(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() != robotArm) return;
        robotArmHasChanged(evt);
        updateLabels();
    }

    private void robotArmHasChanged(PropertyChangeEvent evt) {
        // make old arm forget me.
        MarlinRobotArm oldArm = ((MarlinRobotArm)evt.getOldValue());
        if(oldArm!=null) {
            oldArm.removeMarlinListener(EditorPanel.this);
            oldArm.removeDetachListener(this);
        }

        // make new arm remember me.
        MarlinRobotArm newArm = robotArm.getSubject();
        if(newArm!=null) {
            newArm.addMarlinListener(EditorPanel.this);
            Node parent = newArm.getParent();
            if(parent != null) parent.addDetachListener( this );
        }
    }

    @Override
    public void nodeDetached(Node child) {
        if(robotArm.getSubject() == child) {
            // robot arm has been removed from the scene tree.
            // Remove, Cut or Move will cause this to trigger.
            Node parent = child.getParent();
            if(parent != null) parent.removeDetachListener( this );
            robotArm.setSubject(null);
            updateLabels();
        }
    }
}
