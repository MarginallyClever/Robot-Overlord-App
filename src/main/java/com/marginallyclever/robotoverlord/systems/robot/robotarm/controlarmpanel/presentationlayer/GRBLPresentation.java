package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.presentationlayer;

import com.marginallyclever.communications.application.ChooseConnectionPanel;
import com.marginallyclever.communications.application.TextInterfaceToSessionLayer;
import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.communications.session.SessionLayerListener;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.robotoverlord.robots.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link GRBLPresentation} is a {@link PresentationLayer} for <a href="https://github.com/gnea/grbl">GRBL</a> firmware.
 *
 */
@Deprecated
public class GRBLPresentation implements PresentationLayer {
    private static final Logger logger = LoggerFactory.getLogger(GRBLPresentation.class);

    // number of commands we'll hold on to in case there's a resend.
    private static final int HISTORY_BUFFER_LIMIT = 250;
    // can buffer this many commands from serial, before processing.
    private static final int MARLIN_SEND_LIMIT = 20;
    // If nothing is heard for this many ms then send a ping to check if the connection is still live. 
    private static final int TIMEOUT_DELAY = 2000;
    // says this when a resend is needed, followed by the last well-received line number.
    private static final String STR_ERROR = "error:";
    // sends this event when the robot is ready to receive more.
    private static final String STR_OK = "ok";
    // sends this as an ActionEvent to let listeners know it can handle more input.
    private static final String IDLE = "idle";

    private final JPanel panel = new JPanel(new BorderLayout());
    private final Robot myArm;
    private final TextInterfaceToSessionLayer chatInterface = new TextInterfaceToSessionLayer();
    private final List<NumberedCommand> myHistory = new ArrayList<>();

    private final JButton bESTOP = new JButton("EMERGENCY STOP");
    private final JButton bGetAngles = new JButton("M114");
    private final JButton bSetHome = new JButton("Set Home");
    private final JButton bGoHome = new JButton("Go Home");

    // the next line number I should send.  Marlin may say "please resend previous line x", which would change this.
    private int lineNumberToSend;
    // the last line number added to the queue.
    private int lineNumberAdded;
    // don't send more than this many at a time without acknowledgement.
    private int busyCount=MARLIN_SEND_LIMIT;

    private final Timer timeoutChecker = new Timer(10000,(e)->onTimeoutCheck());
    private long lastReceivedTime;

    public GRBLPresentation(Robot arm) {
        super();

        myArm = arm;

        panel.setLayout(new BorderLayout());
        panel.add(getToolBar(), BorderLayout.PAGE_START);
        panel.add(chatInterface, BorderLayout.CENTER);

        arm.addPropertyChangeListener(this::onRobotEvent);

        chatInterface.addActionListener((e) -> {
            switch (e.getID()) {
                case ChooseConnectionPanel.CONNECTION_OPENED -> {
                    onConnect();
                    notifyListeners(e);
                }
                case ChooseConnectionPanel.CONNECTION_CLOSED -> {
                    onClose();
                    updateButtonAccess();
                    notifyListeners(e);
                }
            }
        });
    }

    public void addNetworkSessionListener(SessionLayerListener a) {
        chatInterface.addNetworkSessionListener(a);
    }

    private void onRobotEvent(PropertyChangeEvent e)  {
        if(e.getPropertyName().contentEquals("ee")) sendGoto();
    }

    private void onConnect() {
        logger.info("connected.");
        setupListener();
        lineNumberToSend=1;
        lineNumberAdded=0;
        myHistory.clear();
        updateButtonAccess();
        timeoutChecker.start();

        // you are at the position I say you are at.
        new java.util.Timer().schedule(new java.util.TimerTask() {
                                           @Override
                                           public void run() {
                                               updateButtonAccess();
                                               sendSetHome();
                                           }
                                       }, 1000 // 1s delay
        );
    }

    private void onClose() {
        logger.info("disconnected.");
        timeoutChecker.stop();
    }

    private void onTimeoutCheck() {
        if(System.currentTimeMillis()-lastReceivedTime>TIMEOUT_DELAY) {
            chatInterface.sendCommand("M400");
        }
    }

    private void setupListener() {
        chatInterface.addNetworkSessionListener(this::onDataReceived);
    }

    private void onDataReceived(SessionLayerEvent evt) {
        if(evt.flag == SessionLayerEvent.DATA_AVAILABLE) {
            lastReceivedTime = System.currentTimeMillis();
            String message = ((String)evt.data).trim();
            if (message.startsWith("<") && message.contains("WPos")) {
                onHearStatus(message);
            } else if(message.startsWith(STR_OK)) {
                onHearOK();
            } else if(message.contains(STR_ERROR)) {
                onHearError(message);
            }
        }
    }

    /**
     * Parse and deal with GRBL error codes.
     * @param message the message to parse
     */
    private void onHearError(String message) {
        String numberPart = message.substring(message.indexOf(STR_ERROR) + STR_ERROR.length());
        int GRBLErrorCode = 0;
        try {
            GRBLErrorCode = Integer.parseInt(numberPart);
            // else line is no longer in the buffer.  should not be possible!
        } catch(NumberFormatException e) {
            logger.info("'\"+message+\"' could not be parsed: "+e.getMessage());
            return;
        }

        switch (GRBLErrorCode) {
            case 1 -> logger.info("GRBL error: G-code words consist of a letter and a value. Letter was not found.");
            case 2 -> logger.info("GRBL error: Numeric value format is not valid or missing an expected value.");
            case 3 -> logger.info("GRBL error: Grbl '$' system command was not recognized or supported.");
            case 4 -> logger.info("GRBL error: Negative value received for an expected positive value.");
            case 5 -> logger.info("GRBL error: Homing cycle failure. Homing is not enabled via settings.");
            case 6 -> logger.info("GRBL error: Minimum step pulse time must be greater than 3usec.");
            case 7 -> logger.info("GRBL error: EEPROM read failed. Reset and restored to default values.");
            case 8 -> logger.info("GRBL error: Grbl '$' command cannot be used unless Grbl is IDLE."
                                 +" Ensures smooth operation during a job.");
            case 9 -> logger.info("GRBL error: G-code locked out during alarm or jog state.");
        }
    }

    private void onHearOK() {
        SwingUtilities.invokeLater(() -> {
            busyCount++;
            sendQueuedCommand();
            clearOldHistory();
            if(lineNumberToSend>=lineNumberAdded) {
                fireIdleNotice();
            }
        });
    }

    private void fireIdleNotice() {
        notifyListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED, GRBLPresentation.IDLE));
    }

    private void clearOldHistory() {
        while(myHistory.size()>0 && myHistory.get(0).lineNumber<lineNumberAdded-HISTORY_BUFFER_LIMIT) {
            myHistory.remove(0);
        }
    }

    public void queueAndSendCommand(String str) {
        if(!chatInterface.getIsConnected()) return;
        if(str.trim().length()==0) return;

        lineNumberAdded++;
        // Line number and checksum would go here, but as far as I know GRBL does not support it.
        myHistory.add(new NumberedCommand(lineNumberAdded,str));

        if(busyCount>0) sendQueuedCommand();
    }

    private void sendQueuedCommand() {
        clearOldHistory();

        if(myHistory.size()==0) return;

        int smallest = Integer.MAX_VALUE;
        for( NumberedCommand mc : myHistory ) {
            if(smallest > mc.lineNumber) smallest = mc.lineNumber;
            if(mc.lineNumber == lineNumberToSend) {
                busyCount--;
                lineNumberToSend++;
                //logger.info("sending '"+mc.command+"'.");
                chatInterface.sendCommand(mc.command);
                return;
            }
        }

        if(smallest>lineNumberToSend) {
            // history no longer contains the line?!
            logger.info("did not find "+lineNumberToSend);
            for( NumberedCommand mc : myHistory ) {
                logger.info("..."+mc.lineNumber+": "+mc.command);
            }
        }
    }

    private String generateChecksum(String line) {
        byte checksum = 0;

        int i=line.length();
        while(i>0) checksum ^= (byte)line.charAt(--i);

        return "*" + Integer.toString(checksum);
    }

    public boolean getIsBusy() {
        return busyCount<=0;
    }

    /**
     *  <p>Parse the status message.  Format is normally</p>
     *  <pre>&lt;Idle|WPos:0.000,0.000,0.000|Bf:15,128|FS:0,0&gt;</pre>
     *  <p>In this message:</p>
     *  <ul>
     *  <li>"Idle" is the current state of the machine.</li>
     *  <li>"WPos:0.000,0.000,0.000" represents the work position in X, Y, and Z coordinates.</li>
     *  <li>"Bf:15,128" indicates the remaining block buffer and RX serial buffer space.</li>
     *  <li>"FS:0,0" shows the current feed rate and spindle speed.</li>
     *  </ul>
      * @param message the message to parse.
     */
    private void onHearStatus(String message) {
        try {
            message = message.substring(0, message.indexOf("WPos:"));
            String[] majorParts = message.split("|");
            String[] minorParts = majorParts[0].split(",");

            int count = (int)myArm.get(Robot.NUM_JOINTS);
            for (int i = 0; i < count; ++i) {
                myArm.set(Robot.ACTIVE_JOINT,i);
                double v = Double.parseDouble(minorParts[i]);
                myArm.set(Robot.JOINT_VALUE,v);
            }
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
        }
    }

    private void sendGoto() {
        //logger.info("sendGoto()");
        StringBuilder action = new StringBuilder("G1");
        int count = (int)myArm.get(Robot.NUM_JOINTS);
        for (int i = 0; i < count; ++i) {
            myArm.set(Robot.ACTIVE_JOINT,i);
            action.append(" ").append(myArm.get(Robot.JOINT_NAME)).append(StringHelper.formatDouble((double) myArm.get(Robot.JOINT_VALUE)));
        }
        queueAndSendCommand(action.toString());
    }

    private JToolBar getToolBar() {
        JToolBar bar = new JToolBar();
        bar.setRollover(true);

        bESTOP.setFont(panel.getFont().deriveFont(Font.BOLD));
        bESTOP.setForeground(Color.RED);

        bESTOP.addActionListener((e) -> sendESTOP() );
        bGetAngles.addActionListener((e) -> sendGetPosition() );
        bSetHome.addActionListener((e) -> sendSetHome() );
        bGoHome.addActionListener((e) -> sendGoHome() );

        bar.add(bESTOP);
        bar.addSeparator();
        bar.add(bGetAngles);
        bar.add(bSetHome);
        bar.add(bGoHome);

        updateButtonAccess();

        return bar;
    }

    private void sendESTOP() {
        chatInterface.sendCommand("M112");
        chatInterface.sendCommand("M112");
        chatInterface.sendCommand("M112");
    }

    private void sendGetPosition() {
        queueAndSendCommand("?");
    }

    private void updateButtonAccess() {
        boolean isConnected = chatInterface.getIsConnected();

        bESTOP.setEnabled(isConnected);
        bGetAngles.setEnabled(isConnected);
        bSetHome.setEnabled(isConnected);
        bGoHome.setEnabled(isConnected);
    }

    private void sendSetHome() {
        StringBuilder action = new StringBuilder("G92");
        int count = (int)myArm.get(Robot.NUM_JOINTS);
        for (int i = 0; i < count; ++i) {
            myArm.set(Robot.ACTIVE_JOINT,i);
            String name = (String)myArm.get(Robot.JOINT_NAME);
            double angle = (double)myArm.get(Robot.JOINT_HOME);
            action.append(" ").append(name).append(StringHelper.formatDouble(angle));
        }
        queueAndSendCommand(action.toString());
        sendGoHome();
    }

    public void sendGoHome() {
        int count = (int)myArm.get(Robot.NUM_JOINTS);
        if(count==0) return;

        double [] list = new double[count];
        for (int i = 0; i < count; ++i) {
            myArm.set(Robot.ACTIVE_JOINT, i);
            list[i] = (double)myArm.get(Robot.JOINT_HOME);
        }
        myArm.set(Robot.ALL_JOINT_VALUES,list);
        myArm.set(Robot.END_EFFECTOR_TARGET,myArm.get(Robot.END_EFFECTOR));
    }

    public void setNetworkSession(SessionLayer session) {
        chatInterface.setNetworkSession(session);
    }

    public void closeConnection() {
        this.chatInterface.closeConnection();
    }

    private void notifyListeners(ActionEvent e) {
        for (ActionListener listener : listeners) listener.actionPerformed(e);
    }

    public boolean isIdleCommand(ActionEvent e) {
        return e.getActionCommand().contentEquals(GRBLPresentation.IDLE);
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }
}
