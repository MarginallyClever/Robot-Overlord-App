package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.node.nodes.MaterialPanel;

import java.util.List;
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;

/**
 * A panel for controlling an ODE4J creature.
 */
public class CreatureControllerPanel extends JPanel {
    private double myFirstTorque = 250000;
    private JLabel brainLabel = new JLabel();
    private CreatureController creatureController;
    private Timer timer;

    public CreatureControllerPanel() {
        this(new CreatureController());
    }

    public CreatureControllerPanel(CreatureController creatureController) {
        super(new GridBagLayout());
        setName(CreatureController.class.getSimpleName());

        this.creatureController = creatureController;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;

        addHingeControls(gbc);

        gbc.weighty = 0.0;
        PanelHelper.addLabelAndComponent(this, "Brain Scan", brainLabel, gbc);
        gbc.weighty = 1.0;

        addSaveButton(gbc);

        beginTimer();
    }

    private void addHingeControls(GridBagConstraints gbc) {
        for (ODEHinge hinge : creatureController.getHinges()) {
            addAction(hinge.getName(), hinge, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
        }
    }

    // a save button to export the brain to a file
    private void addSaveButton(GridBagConstraints gbc) {
        JButton saveButton = new JButton("Save!");
        saveButton.addActionListener((e) -> {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // save brain image to fc.getSelectedFile()
                var brainScan = creatureController.getBrain().getImage();
                try {
                    javax.imageio.ImageIO.write(brainScan, "png", fc.getSelectedFile());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        gbc.gridy++;
        gbc.gridwidth=2;
        PanelHelper.addLabelAndComponent(this, "Image", saveButton, gbc);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        beginTimer();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        endTimer();
    }

    private void endTimer() {
        if (timer != null) {
            timer.stop();
            timer=null;
        }
    }

    private void beginTimer() {
        // Create a timer that calls an ActionListener every 67 milliseconds
        if (timer == null) {
            timer = new Timer(1000 / 15, e -> updateBrainImage());
            timer.start();
        }
    }

    private void updateBrainImage() {
        // update the brain image
        brainLabel.setIcon(new ImageIcon(creatureController.getBrain().getImage()));
        brainLabel.repaint();
    }

    private void addAction(String label,ODEHinge hinge,GridBagConstraints gbc) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));

        // torque qty
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.0);
        JFormattedTextField torqueField = new JFormattedTextField(formatter);
        torqueField.setValue(myFirstTorque);
        torqueField.addPropertyChangeListener("value", (evt) ->{
            myFirstTorque = ((Number) torqueField.getValue()).doubleValue();
        });
        panel.add(torqueField);

        // add torque
        JButton selector = new JButton("+");
        selector.addActionListener((e)-> hinge.addTorque(myFirstTorque));
        panel.add(selector);

        // add reverse torque
        JButton selector2 = new JButton("-");
        selector2.addActionListener((e)-> hinge.addTorque(-myFirstTorque));
        panel.add(selector2);
        PanelHelper.addLabelAndComponent(this, label, panel, gbc);
    }
}
