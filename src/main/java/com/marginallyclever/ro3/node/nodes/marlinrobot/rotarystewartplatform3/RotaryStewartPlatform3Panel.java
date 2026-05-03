package com.marginallyclever.ro3.node.nodes.marlinrobot.rotarystewartplatform3;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Control panel for a {@link RotaryStewartPlatform3}.
 */
public class RotaryStewartPlatform3Panel extends JPanel {
    private static final JTextField angles = new JTextField("G0 X0 Y0 Z0 A0 B0 C0");

    public RotaryStewartPlatform3Panel() {
        this(new RotaryStewartPlatform3());
    }

    public RotaryStewartPlatform3Panel(RotaryStewartPlatform3 rsp) {
        super(new GridBagLayout());
        this.setName(RotaryStewartPlatform3.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=2;

        JPanel panel = new JPanel(new BorderLayout());
        angles.setEditable(false);
        //angles.setMaximumSize(new Dimension(50, angles.getPreferredSize().height));
        panel.add(new JLabel("angles"),BorderLayout.LINE_START);
        panel.add(angles,BorderLayout.CENTER);
        this.add(panel,gbc);
        rsp.addPropertyChangeListener(e-> {
            if (e.getPropertyName().equals("pose")) {
                setAngleText(rsp);
            }
        });
        setAngleText(rsp);
        gbc.gridwidth=1;
        {
            gbc.gridy++;
            JButton button = new JButton("G92 ...");
            button.setToolTipText("Declare all motors are at home position.");
            button.addActionListener(e -> {
                rsp.sendGCode("G92 " +
                        " X" + RotaryStewartPlatform3.HOME_ANGLE +
                        " Y" + RotaryStewartPlatform3.HOME_ANGLE +
                        " Z" + RotaryStewartPlatform3.HOME_ANGLE +
                        " A" + RotaryStewartPlatform3.HOME_ANGLE +
                        " B" + RotaryStewartPlatform3.HOME_ANGLE +
                        " C" + RotaryStewartPlatform3.HOME_ANGLE);
            });
            PanelHelper.addLabelAndComponent(this, "Set home", button, gbc);
        }
    }


    private void setAngleText(RotaryStewartPlatform3 rsp) {
        angles.setText(rsp.getMotorsAndFeedrateAsString());
    }
}
