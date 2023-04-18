package com.marginallyclever.robotoverlord.components.robot.robotarm.robotarminterface.presentationlayer;

import com.marginallyclever.communications.presentation.PresentationLayer;
import com.marginallyclever.robotoverlord.robots.Robot;

public class PresentationFactory {
    public static final String [] AVAILABLE_PRESENTATIONS = {
            "Marlin",
            "GRBL",
            "CANOpen",
    };

    public static PresentationLayer createPresentation(String type,Robot robot) {
        if (type.equalsIgnoreCase("Marlin")) {
            return new MarlinPresentation();
        } else if (type.equalsIgnoreCase("GRBL")) {
            return new GRBLPresentation();
        } else if( type.equalsIgnoreCase("CANOpen") ) {
            return new CANOpenPresentation();
        } else {
            // handle invalid or unsupported presentation types
            throw new IllegalArgumentException("Invalid presentation type: " + type);
        }
    }
}
