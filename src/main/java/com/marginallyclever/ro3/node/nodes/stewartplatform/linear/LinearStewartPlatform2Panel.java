package com.marginallyclever.ro3.node.nodes.stewartplatform.linear;

import com.marginallyclever.ro3.PanelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for editing LinearStewartPlatform2 parameters.
 */
public class LinearStewartPlatform2Panel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(LinearStewartPlatform2Panel.class);

    public LinearStewartPlatform2Panel() {
        this(new LinearStewartPlatform2());
    }

    public LinearStewartPlatform2Panel(LinearStewartPlatform2 platform) {
        super(new GridLayout(0,2));
        this.setName(LinearStewartPlatform2.class.getSimpleName());

        // top offsets
        var topOffset = platform.getTopOffset();
        JFormattedTextField topXField = PanelHelper.addNumberFieldDouble("top x",topOffset.x);
        topXField.addPropertyChangeListener("value",e->{
            topOffset.x = ((Number) topXField.getValue()).doubleValue();
            platform.setTopOffset(topOffset);
        });        
        PanelHelper.addLabelAndComponent(this,"Top Offset X", topXField);

        JFormattedTextField topYField = PanelHelper.addNumberFieldDouble("top y",topOffset.y);
        topYField.addPropertyChangeListener("value",e->{
            topOffset.y = ((Number) topYField.getValue()).doubleValue();
            platform.setTopOffset(topOffset);
        });
        PanelHelper.addLabelAndComponent(this,"Top Offset Y", topYField);

        // bottom offsets
        var bottomOffset = platform.getBottomOffset();
        JFormattedTextField bottomXField = PanelHelper.addNumberFieldDouble("bottom x",bottomOffset.x);
        bottomXField.addPropertyChangeListener("value",e->{
            bottomOffset.x = ((Number) bottomXField.getValue()).doubleValue();
            platform.setBottomOffset(bottomOffset);
        });
        PanelHelper.addLabelAndComponent(this,"Bottom Offset X", bottomXField);

        JFormattedTextField bottomYField = PanelHelper.addNumberFieldDouble("bottom y",bottomOffset.y);
        bottomYField.addPropertyChangeListener("value",e->{
            bottomOffset.y = ((Number) bottomYField.getValue()).doubleValue();
            platform.setBottomOffset(bottomOffset);
        });
        PanelHelper.addLabelAndComponent(this,"Bottom Offset Y", bottomYField);


        // actuator stroke length
        JFormattedTextField maxField = PanelHelper.addNumberFieldDouble("max",platform.getMaxActuatorLength());
        maxField.addPropertyChangeListener("value",e->{
            platform.setMaxActuatorLength( ((Number) maxField.getValue()).doubleValue() );
        });
        PanelHelper.addLabelAndComponent(this,"Actuator max", maxField);

        JFormattedTextField minField = PanelHelper.addNumberFieldDouble("min",platform.getMinActuatorLength());
        minField.addPropertyChangeListener("value",e->{
            platform.setMinActuatorLength( ((Number) minField.getValue()).doubleValue() );
        });
        PanelHelper.addLabelAndComponent(this,"Actuator min", minField);
    }
}
