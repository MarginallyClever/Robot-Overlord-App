package com.marginallyclever.ro3.node.nodes.stewartplatform.rotary;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Control panel for a {@link RotaryStewartPlatform3}.
 */
public class RotaryStewartPlatform3Panel extends JPanel {
    private static final JLabel angles = new JLabel("");

    public RotaryStewartPlatform3Panel() {
        this(new RotaryStewartPlatform3());
    }

    public RotaryStewartPlatform3Panel(RotaryStewartPlatform3 rotaryStewartPlatform3) {
        super(new GridLayout(0,2));
        this.setName(RotaryStewartPlatform3.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;

        PanelHelper.addLabelAndComponent(this, "angles", angles, gbc);

        rotaryStewartPlatform3.addPropertyChangeListener(e-> {
            if (e.getPropertyName().equals("pose")) {
                setAngleText(rotaryStewartPlatform3);
            }
        });
        setAngleText(rotaryStewartPlatform3);
    }

    private void setAngleText(RotaryStewartPlatform3 rotaryStewartPlatform3) {
        Double [] angleList = rotaryStewartPlatform3.getMotorAngles();
        String output = String.format("G0 X%.2f Y%.2f Z%.2f A%.2f B%.2f C%.2f",
                angleList[0], angleList[1],
                angleList[2], angleList[3],
                angleList[4], angleList[5]);
        angles.setText(output);
    }
}
