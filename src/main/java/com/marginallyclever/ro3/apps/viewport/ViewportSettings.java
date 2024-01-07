package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.view.View;
import com.marginallyclever.ro3.view.ViewProvider;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;

/**
 * {@link ViewportSettings} is a {@link View} for {@link Viewport}.
 */
@View(of=Viewport.class)
public class ViewportSettings extends JPanel implements ViewProvider<Viewport> {
    private Viewport subject;
    private final NumberFormatter formatter = NumberFormatHelper.getNumberFormatter();
    private final JFormattedTextField movementScale = new JFormattedTextField(formatter);
    private final JToggleButton hardwareAccelerated = new JToggleButton("Hardware Accelerated");
    private final JToggleButton doubleBuffered = new JToggleButton("Double Buffered");
    private final JToggleButton verticalSync = new JToggleButton("Vertical Sync");
    private final JComboBox<Integer> fsaaSamples = new JComboBox<>(new Integer[]{1, 2, 4, 8});

    public ViewportSettings() {
        super(new GridLayout(0,2));
        setName("Viewport Settings");

        add(new JLabel("Mouse scale"));
        add(movementScale);
        movementScale.setValue(1.0);
        movementScale.addPropertyChangeListener("value", evt -> setMovementScale((Double) evt.getNewValue()));

        add(new JLabel("Hardware Accelerated"));
        add(hardwareAccelerated);
        hardwareAccelerated.addActionListener(evt -> setHardwareAccelerated(hardwareAccelerated.isSelected()));

        add(new JLabel("Double Buffered"));
        add(doubleBuffered);
        doubleBuffered.addActionListener(evt -> setViewportDoubleBuffered(doubleBuffered.isSelected()));

        add(new JLabel("Vertical Sync"));
        add(verticalSync);
        verticalSync.addActionListener(evt -> setVerticalSync(verticalSync.isSelected()));

        add(new JLabel("FSAA Samples"));
        add(fsaaSamples);
        fsaaSamples.addActionListener(evt -> setFsaaSamples((Integer) fsaaSamples.getSelectedItem()));
    }

    private void setHardwareAccelerated(boolean selected) {
        if (subject != null) subject.setHardwareAccelerated(selected);
    }

    private void setViewportDoubleBuffered(boolean selected) {
        if (subject != null) subject.setDoubleBuffered(selected);
    }

    private void setVerticalSync(boolean selected) {
        if (subject != null) subject.setVerticalSync(selected);
    }

    private void setFsaaSamples(Integer value) {
        if(value > 0) {
            fsaaSamples.setForeground(Color.BLACK);
            if (subject != null) subject.setFsaaSamples(value);
        } else {
            fsaaSamples.setForeground(Color.RED);
        }
    }

    private void setMovementScale(double v) {
        if(v > 0) {
            movementScale.setForeground(Color.BLACK);
            if (subject != null) subject.setUserMovementScale(v);
        } else {
            movementScale.setForeground(Color.RED);
        }
    }

    @Override
    public void setViewSubject(Viewport subject) {
        setMovementScale(subject.getUserMovementScale());
        hardwareAccelerated.setSelected(subject.isHardwareAccelerated());
        doubleBuffered.setSelected(subject.isDoubleBuffered());
        verticalSync.setSelected(subject.isVerticalSync());
        fsaaSamples.setSelectedItem(subject.getFsaaSamples());
        this.subject = subject;
    }
}
