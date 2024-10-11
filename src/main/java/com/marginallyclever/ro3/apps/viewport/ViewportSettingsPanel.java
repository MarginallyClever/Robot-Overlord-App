package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.viewport.renderpasses.DrawMeshes;
import com.marginallyclever.ro3.view.View;
import com.marginallyclever.ro3.view.ViewProvider;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;

/**
 * {@link ViewportSettingsPanel} is a {@link View} for {@link Viewport}.
 */
public class ViewportSettingsPanel extends JPanel implements ViewProvider<Viewport> {
    private Viewport subject;
    private final NumberFormatter formatter = NumberFormatHelper.getNumberFormatter();
    private final JFormattedTextField movementScale = new JFormattedTextField(formatter);
    private final JToggleButton hardwareAccelerated = new JToggleButton();
    private final JToggleButton doubleBuffered = new JToggleButton();
    private final JToggleButton verticalSync = new JToggleButton();
    private final JComboBox<Integer> fsaaSamples = new JComboBox<>(new Integer[]{1, 2, 4, 8});
    private final Dial timeOfDay = new Dial();
    private final Dial declination = new Dial();
    private final JButton selectSunColor = new JButton();
    private final JButton selectAmbientColor = new JButton();

    public ViewportSettingsPanel() {
        super(new GridBagLayout());
        setName("Viewport");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        PanelHelper.addLabelAndComponent(this, "Mouse scale", movementScale,gbc);
        formatter.setMinimum(0.001);
        movementScale.setValue(1.0);
        movementScale.addPropertyChangeListener("value", evt -> setMovementScale((Double) evt.getNewValue()));

        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "Hardware Accelerated", hardwareAccelerated,gbc);
        hardwareAccelerated.addActionListener(evt -> {
            setHardwareAccelerated(hardwareAccelerated.isSelected());
        });
        setHardwareAcceleratedLabel();

        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "Double Buffered", doubleBuffered,gbc);
        doubleBuffered.addActionListener(evt -> {
            setViewportDoubleBuffered(doubleBuffered.isSelected());
        });
        setViewportDoubleBufferedLabel();

        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "Vertical Sync", verticalSync,gbc);
        verticalSync.addActionListener(evt -> {
            setVerticalSync(verticalSync.isSelected());
        });
        setVerticalSyncLabel();

        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "FSAA Samples", fsaaSamples,gbc);
        fsaaSamples.addActionListener(evt -> setFSAASamples((Integer) fsaaSamples.getSelectedItem()));

        // TODO the lighting settings below here should be per-scene.
        // ambient color
        PanelHelper.addColorChooser(this,"Ambient",Color.DARK_GRAY,this::setAmbientColor,gbc);
        // sun color
        PanelHelper.addColorChooser(this,"Sun color",Color.WHITE,this::setSunColor,gbc);

        gbc.weighty = 1.0;
        // sun position
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "Time of day (24h)", timeOfDay,gbc);
        timeOfDay.addActionListener(e->updateSunPosition());
        timeOfDay.setPreferredSize(new Dimension(100,100));

        // sun position
        gbc.gridy++;
        PanelHelper.addLabelAndComponent(this, "Declination (+/-90)", declination,gbc);
        declination.addActionListener(e->{
            if(declination.getValue()>90) declination.setValue(90);
            if(declination.getValue()<-90) declination.setValue(-90);
            updateSunPosition();
        });
        declination.setPreferredSize(new Dimension(100,100));
    }

    @Override
    public void removeNotify() {
        super.removeNotify();

        if(subject!=null) subject.savePrefs();

        var dm = getDrawMeshes();
        if(dm!=null) dm.savePrefs();
    }

    private void setAmbientColor(Color color) {
        var dm = getDrawMeshes();
        if(dm==null) return;
        dm.setAmbientColor(color);
        selectAmbientColor.setBackground(color);
    }

    private void setSunColor(Color color) {
        var dm = getDrawMeshes();
        if(dm==null) return;
        dm.setSunlightColor(color);
        selectSunColor.setBackground(color);
    }

    private void updateSunPosition() {
        DrawMeshes meshes = getDrawMeshes();
        if(meshes==null) return;

        meshes.setDeclination(declination.getValue());
        meshes.setTimeOfDay(timeOfDay.getValue()+90);
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
        hardwareAccelerated.setSelected(selected);
        setHardwareAcceleratedLabel();
    }

    private void setViewportDoubleBuffered(boolean selected) {
        if (subject != null) subject.setDoubleBuffered(selected);
        doubleBuffered.setSelected(selected);
        setViewportDoubleBufferedLabel();
    }

    private void setVerticalSync(boolean selected) {
        if (subject != null) subject.setVerticalSync(selected);
        verticalSync.setSelected(selected);
        setVerticalSyncLabel();
    }

    private void setFSAASamples(Integer value) {
        if (subject != null) subject.setFsaaSamples(value);
        fsaaSamples.setSelectedItem(value);
    }

    private void setMovementScale(double v) {
        if (subject != null) subject.setUserMovementScale(v);
    }

    @Override
    public void setViewSubject(Viewport subject) {
        this.subject = subject;
        setMovementScale(subject.getUserMovementScale());
        setHardwareAccelerated(subject.isHardwareAccelerated());
        setViewportDoubleBuffered(subject.isDoubleBuffered());
        setVerticalSync(subject.isVerticalSync());
        setFSAASamples(subject.getFsaaSamples());

        // this only allows parameters from one render pass.
        // TODO: add other passes?
        DrawMeshes meshes = getDrawMeshes();
        if(meshes!=null) {
            setSunColor(meshes.getSunlightColor());
            timeOfDay.setValue(meshes.getTimeOfDay()-90);
            declination.setValue(meshes.getDeclination());
        }
    }

    private DrawMeshes getDrawMeshes() {
        if(subject==null) return null;

        for(var rp : subject.renderPasses.getList()) {
            if(rp instanceof DrawMeshes) {
                return (DrawMeshes)rp;
            }
        }
        return null;
    }
}
