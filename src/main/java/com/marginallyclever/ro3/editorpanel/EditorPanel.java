package com.marginallyclever.ro3.editorpanel;

import com.marginallyclever.ro3.node.nodes.marlinrobotarm.MarlinListener;
import com.marginallyclever.ro3.node.nodes.marlinrobotarm.MarlinRobotArm;
import com.marginallyclever.ro3.node.nodeselector.NodeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * <p>{@link EditorPanel} is a panel for editing GCode that can be sent to a {@link MarlinRobotArm}.</p>
 * <p>It can also listen to a {@link MarlinRobotArm}, which generates GCode and status messages.</p>
 *
 * <p>{@link com.marginallyclever.ro3.node.Node}s build up to form a robot arm in 3d like Pose, Mesh, DH parameter, HingeJoint, Motor, and finally connect
 * it all to a {@link MarlinRobotArm} node (1)  that translates arm state to gcode and back.  With the node selected more info
 * is available in the `details` tab (2).  I can see the last output from the arm in the `output` field because it is
 * registered to listen for {@link MarlinListener} events.  The Editor can be assigned an existing MarlinRobotArm (3).
 * While it has one, all `MarlinEvent`s will be written to the status bar (8).</p>
 * <p>If the `get` toggle is on, the next event will be written out at the caret position (7).  You can
 * trigger events:  Send "ik" (6) and the event will contain the cartesian position of the end effector target.
 * Send "fk" (6) and the event will contain the current robot motor angles, in degrees.  Future marlin buttons may
 * create other events such as "find home" or "activate tool".  See MarlinRobotArm for more messages.</p>
 * <p>If the `send` button is pressed, the current line of gcode is transmitted to the robot.  The behavior then
 * follows (6).</p>
 * <p>There is room for more editing tools here like save, load, copy, cut, paste, undo, redo, etc.  I'd rather not
 * write a text editor, this is just to demonstrate the concept until I find an existing editor I can drop in.</p>
 * <p>TODO add a lock to the `get` button that keeps the get toggle on until further notice.</p>
 */
public class EditorPanel extends JPanel implements MarlinListener {
    private static final Logger logger = LoggerFactory.getLogger(EditorPanel.class);
    private final NodeSelector<MarlinRobotArm> armSelector = new NodeSelector<>(MarlinRobotArm.class);
    private final JTextArea text = new JTextArea("text");
    private final JLabel statusLabel = new JLabel("status");

    private final JToggleButton getButton = new JToggleButton(new AbstractAction("Get") {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(getButton.isSelected()) {
                // if there is no arm, deselect the button.
                MarlinRobotArm arm = armSelector.getSubject();
                if(arm!=null) {
                    getButton.setSelected(false);
                }
            } else {
                // when get is off, lock is off.
                lockButton.setSelected(false);
            }
        }
    });

    private final JToggleButton lockButton = new JToggleButton(new AbstractAction("Lock") {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(lockButton.isSelected()) {
                // press lock also activates get
                getButton.setSelected(true);
            } // else lock is off, stay deselected.
        }
    });

    private final JButton sendButton = new JButton(new AbstractAction("Send") {
        @Override
        public void actionPerformed(ActionEvent e) {
            MarlinRobotArm arm = armSelector.getSubject();
            if(arm!=null) {
                String message = getLineAtCaret();
                if(!message.trim().isEmpty()) {
                    arm.sendGCode(message);
                }
            }
        }
    });

    public EditorPanel() {
        super(new BorderLayout());
        JToolBar tools = new JToolBar("tools");
        addTools(tools);
        add(tools, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);
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

    private void addTools(JToolBar tools) {
        tools.add(armSelector);
        tools.addSeparator();
        tools.add(new JButton(new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                text.setText("");
            }
        }));/*
        tools.add(new JButton(new AbstractAction("Load") {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("Not implemented yet.");
            }
        }));
        tools.add(new JButton(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("Not implemented yet.");
            }
        }));*/

        tools.addSeparator();
        tools.add(sendButton);
        tools.add(getButton);
        tools.add(lockButton);
        tools.addSeparator();

        // FIXME set shortcut to ctrl+G?
        /*
        getButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control G"), "get");
        sendButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "send");
        lockButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control L"), "lock");

        getButton.getActionMap().put("get", getButton.getAction());
        sendButton.getActionMap().put("send", sendButton.getAction());
        lockButton.getActionMap().put("lock", lockButton.getAction());
         */
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
}
