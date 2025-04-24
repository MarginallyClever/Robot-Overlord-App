package com.marginallyclever.ro3.node.nodes.environment;

import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * {@link EnvironmentPanel} adjusts settings for a {@link Environment}.
 */
public class EnvironmentPanel extends JPanel {
    private final Environment environment;
    private final Dial timeOfDay = new Dial();
    private final Dial declination = new Dial();
    private final JButton selectSunColor = new JButton();
    private final JButton selectAmbientColor = new JButton();

    public EnvironmentPanel() {
        this(new Environment());
    }

    public EnvironmentPanel(Environment environment) {
        super(new GridBagLayout());
        setName("Environment");
        this.environment = environment;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        setSunColor(environment.getSunlightColor());
        timeOfDay.setValue(environment.getTimeOfDay()-90);
        declination.setValue(environment.getDeclination());

        // ambient color
        PanelHelper.addColorChooser(this,"Ambient", environment::getAmbientColor,this::setAmbientColor,gbc);
        gbc.gridy++;
        // sun color
        PanelHelper.addColorChooser(this,"Sun color",environment::getSunlightColor,this::setSunColor,gbc);
        gbc.gridy++;

        // sunlight strength
        var nfPos = NumberFormatHelper.getNumberFormatterDouble();
        nfPos.setMinimum(0);
        var esField = PanelHelper.addNumberField("sunlight strength",environment.getSunlightStrength(), nfPos);
        esField.addPropertyChangeListener("value",e->environment.setSunlightStrength(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"Sunlight strength",esField,gbc);
        gbc.gridy++;

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

        // sky texture
        gbc.gridy++;
        PanelHelper.addTextureField(this,"Texture",environment::getSkyTexture,environment::setSkyTexture,gbc);

        // sky shape
        gbc.gridy++;
        JToggleButton isSphereButton = new JToggleButton("",environment.isSkyShapeIsSphere());
        isSphereButton.addActionListener(e -> {
            environment.setSkyShapeIsSphere(isSphereButton.isSelected());
            isSphereButton.setText(environment.isSkyShapeIsSphere()?"sphere":"box");
        });
        isSphereButton.setText(environment.isSkyShapeIsSphere()?"sphere":"box");
        PanelHelper.addLabelAndComponent(this,"Shape",isSphereButton,gbc);
        gbc.gridy++;
    }

    private void setAmbientColor(Color color) {
        selectAmbientColor.setBackground(color);
        environment.setAmbientColor(color);
    }

    private void setSunColor(Color color) {
        selectSunColor.setBackground(color);
        environment.setSunlightColor(color);
    }

    private void updateSunPosition() {
        environment.setDeclination(declination.getValue());
        environment.setTimeOfDay(timeOfDay.getValue()+90);
    }
}
