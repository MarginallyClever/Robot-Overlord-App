package com.marginallyclever.ro3.apps.editorpanel;

import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.shared.PersistentJFileChooser;
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
public class EditorPanel extends App implements MarlinListener {
    private static final Logger logger = LoggerFactory.getLogger(EditorPanel.class);
    private final NodeSelector<MarlinRobotArm> armSelector = new NodeSelector<>(MarlinRobotArm.class);
    private final JTextArea text = new JTextArea();
    private final JLabel statusLabel = new JLabel();
    private static final JFileChooser chooser = new PersistentJFileChooser();
    private final JButton newButton = new JButton(new NewAction(this));
    private final JButton loadButton = new JButton(new LoadAction(this,chooser));
    private final JButton saveButton = new JButton(new SaveAction(this,chooser));
    private final JToolBar tools = new JToolBar("tools");

    private final JButton sendButton = new JButton(new AbstractAction() {
        // constructor
        {
            putValue(Action.NAME, "Send");
            putValue(Action.SHORT_DESCRIPTION, "Send the current line to the robot.");
            putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-play-16.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MarlinRobotArm arm = armSelector.getSubject();
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

    private final JToggleButton getButton = new JToggleButton(new AbstractAction() {
        // constructor
        {
            putValue(Action.NAME, "Get");
            putValue(Action.SHORT_DESCRIPTION, "Get the next event from the robot.");
            putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-record-16.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(getButton.isSelected()) {
                // if there is no arm, deselect the button.
                MarlinRobotArm arm = armSelector.getSubject();
                if(arm==null) {
                    getButton.setSelected(false);
                }
            } else {
                // when get is off, lock is off.
                lockButton.setSelected(false);
            }
        }
    });

    private final JToggleButton lockButton = new JToggleButton(new AbstractAction() {
        // constructor
        {
            putValue(Action.NAME, "Lock");
            putValue(Action.SHORT_DESCRIPTION, "Lock the get button on.");
            putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-lock-16.png"))));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(lockButton.isSelected()) {
                // press lock also activates get
                getButton.setSelected(true);
            } // else lock is off, stay deselected.
        }
    });

    public EditorPanel() {
        super(new BorderLayout());
        text.setName("text");
        statusLabel.setName("status");
        addToolBar(tools);
        add(tools, BorderLayout.NORTH);

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

        armSelector.addPropertyChangeListener("subject",evt -> {
            MarlinRobotArm oldArm = ((MarlinRobotArm)evt.getOldValue());
            if(oldArm!=null) oldArm.removeMarlinListener(EditorPanel.this);

            MarlinRobotArm newArm = armSelector.getSubject();
            if(newArm!=null) {
                statusLabel.setText("Selected: " + newArm.getName());
                newArm.addMarlinListener(EditorPanel.this);
            } else {
                getButton.setSelected(false);
                statusLabel.setText("No arm selected.");
            }
        });
        statusLabel.setText("No arm selected.");
    }

    private void addToolBar(JToolBar tools) {
        armSelector.setMaximumSize(new Dimension(150, 24));
        tools.add(armSelector);
        tools.add(newButton);
        tools.add(loadButton);
        tools.add(saveButton);
        tools.addSeparator();
        tools.add(sendButton);
        tools.add(getButton);
        tools.add(lockButton);
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
        if(getButton.isSelected()) {
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
            if(!lockButton.isSelected()) {
                getButton.setSelected(false);
            } // else lock is on, stay selected.
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
        getButton.setSelected(false);
        lockButton.setSelected(false);
    }
}
