package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
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
    private final JToggleButton hardwareAccelerated = new JToggleButton();
    private final JToggleButton doubleBuffered = new JToggleButton();
    private final JToggleButton verticalSync = new JToggleButton();
    private final JComboBox<Integer> fsaaSamples = new JComboBox<>(new Integer[]{1, 2, 4, 8});

    public ViewportSettings() {
        super(new GridLayout(0,2));
        setName("Viewport Settings");

        PanelHelper.addLabelAndComponent(this, "Mouse scale", movementScale);
        movementScale.setValue(1.0);
        movementScale.addPropertyChangeListener("value", evt -> setMovementScale((Double) evt.getNewValue()));

        PanelHelper.addLabelAndComponent(this, "Hardware Accelerated", hardwareAccelerated);
        hardwareAccelerated.addActionListener(evt -> {
            setHardwareAccelerated(hardwareAccelerated.isSelected());
            setHardwareAcceleratedLabel();
        });

        PanelHelper.addLabelAndComponent(this, "Double Buffered", doubleBuffered);
        doubleBuffered.addActionListener(evt -> {
            setViewportDoubleBuffered(doubleBuffered.isSelected());
            setViewportDoubleBufferedLabel();
        });

        PanelHelper.addLabelAndComponent(this, "Vertical Sync", verticalSync);
        verticalSync.addActionListener(evt -> {
            setVerticalSync(verticalSync.isSelected());
            setVerticalSyncLabel();
        });

        PanelHelper.addLabelAndComponent(this, "FSAA Samples", fsaaSamples);
        fsaaSamples.addActionListener(evt -> setFsaaSamples((Integer) fsaaSamples.getSelectedItem()));
    }

    private void setVerticalSyncLabel() {
        verticalSync.setText( (subject != null && subject.isVerticalSync() ? "On" : "Off") );
    }

    private void setViewportDoubleBufferedLabel() {
        doubleBuffered.setText( (subject != null && subject.isDoubleBuffered() ? "On" : "Off") );
    }

    private void setHardwareAcceleratedLabel() {
        hardwareAccelerated.setText( (subject != null && subject.isHardwareAccelerated() ? "On" : "Off") );
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
