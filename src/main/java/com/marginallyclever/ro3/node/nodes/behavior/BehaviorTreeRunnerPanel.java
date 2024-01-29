package com.marginallyclever.ro3.node.nodes.behavior;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.node.nodes.HingeJoint;

import javax.swing.*;
import java.awt.*;

public class BehaviorTreeRunnerPanel extends JPanel {

    public BehaviorTreeRunnerPanel() {
        this(new BehaviorTreeRunner());
    }

    public BehaviorTreeRunnerPanel(BehaviorTreeRunner behaviorTreeRunner) {
        super(new GridLayout(0,2));
        this.setName(BehaviorTreeRunner.class.getSimpleName());

        // is running
        {
            JToggleButton checkBox = new JToggleButton();
            checkBox.setSelected(behaviorTreeRunner.isRunning());
            checkBox.addActionListener((evt)->{
                behaviorTreeRunner.setRunning(checkBox.isSelected());
                setRunningText(checkBox);
            });
            behaviorTreeRunner.addActionListener(e-> {
                checkBox.setSelected(behaviorTreeRunner.isRunning());
                setRunningText(checkBox);
            } );
            setRunningText(checkBox);
            PanelHelper.addLabelAndComponent(this, "Is Running", checkBox);
        }

        // reset
        {
            JButton button = new JButton("Reset");
            button.addActionListener((evt)->{
                behaviorTreeRunner.setRunning(false);
                behaviorTreeRunner.resetAll();
            });
            PanelHelper.addLabelAndComponent(this, "Reset", button);
        }
    }

    private void setRunningText(JToggleButton button) {
        if(button.isSelected()) {
            button.setText("Running");
            button.setToolTipText("Click to stop");
        } else {
            button.setText("Stopped");
            button.setToolTipText("Click to start");
        }
    }
}
