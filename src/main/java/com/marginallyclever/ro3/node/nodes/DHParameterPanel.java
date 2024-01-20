package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class DHParameterPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(DHParameterPanel.class);

    public DHParameterPanel() {
        this(new DHParameter());
    }

    public DHParameterPanel(DHParameter dhParameter) {
        super(new GridLayout(0,2));
        this.setName(DHParameter.class.getSimpleName());

        JButton fromPose = new JButton("From Pose");
        fromPose.addActionListener(e -> {
            try {
                dhParameter.fromPose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                logger.error("Error converting pose to DH parameters.", ex);
            }
        });

        JButton toPose = new JButton("To Pose");
        toPose.addActionListener(e -> dhParameter.toPoseAndAdjustMeshes());

        var formatter = NumberFormatHelper.getNumberFormatter();

        JFormattedTextField dh_d = new JFormattedTextField(formatter);        dh_d.setValue(dhParameter.getD());
        JFormattedTextField dh_r = new JFormattedTextField(formatter);        dh_r.setValue(dhParameter.getR());
        JFormattedTextField dh_alpha = new JFormattedTextField(formatter);        dh_alpha.setValue(dhParameter.getAlpha());
        JFormattedTextField dh_theta = new JFormattedTextField(formatter);        dh_theta.setValue(dhParameter.getTheta());

        dh_d.addPropertyChangeListener("value", e -> dhParameter.setD( ((Number)dh_d.getValue()).doubleValue() ));
        dh_r.addPropertyChangeListener("value", e -> dhParameter.setR( ((Number)dh_r.getValue()).doubleValue() ));
        dh_alpha.addPropertyChangeListener("value", e -> dhParameter.setAlpha( ((Number)dh_alpha.getValue()).doubleValue() ));
        dh_theta.addPropertyChangeListener("value", e -> dhParameter.setTheta( ((Number)dh_theta.getValue()).doubleValue() ));

        this.setLayout(new GridLayout(0,2));

        PanelHelper.addLabelAndComponent(this,"d",dh_d);
        PanelHelper.addLabelAndComponent(this,"theta",dh_theta);
        PanelHelper.addLabelAndComponent(this,"r",dh_r);
        PanelHelper.addLabelAndComponent(this,"alpha",dh_alpha);

        this.add(fromPose);
        this.add(toPose);

    }
}
