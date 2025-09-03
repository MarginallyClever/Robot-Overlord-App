package com.marginallyclever.ro3.mesh.proceduralmesh;

import javax.swing.*;

/**
 * WaldoPanel provides a settings panel for the {@link Waldo} procedural mesh.
 * the radius of the waldo can be adjusted.
 */
public class WaldoPanel extends JPanel {
    private final Waldo waldo;
    private JSpinner radiusSpinner;

    public WaldoPanel() {
        this(new Waldo());
    }

    public WaldoPanel(Waldo waldo) {
        super();
        setName(Sphere.class.getSimpleName());
        this.waldo = waldo;
        initComponents();
        radiusSpinner.setValue((double)waldo.getRadius());
    }

    private void initComponents() {
        JLabel radiusLabel = new JLabel("Radius:");
        radiusSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 100.0, 0.1));
        radiusSpinner.addChangeListener(e -> {
            double value = (double) radiusSpinner.getValue();
            waldo.setRadius((float) value);
            waldo.updateModel();
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(radiusLabel);
        add(radiusSpinner);
    }
}
