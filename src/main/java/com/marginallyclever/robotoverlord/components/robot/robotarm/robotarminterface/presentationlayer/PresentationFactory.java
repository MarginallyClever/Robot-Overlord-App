package com.marginallyclever.robotoverlord.components.robot.robotarm.robotarminterface.presentationlayer;

import com.marginallyclever.robotoverlord.robots.Robot;

public class PresentationFactory {
    public static final String [] AVAILABLE_PRESENTATIONS = {
            "Marlin",
            "GRBL",
            "CANOpen",
    };

    public static PresentationLayer createPresentation(String type,Robot robot) {
        if (type.equalsIgnoreCase("Marlin")) {
            return new MarlinPresentation(robot);
        } else if (type.equalsIgnoreCase("GRBL")) {
            return new GRBLPresentation(robot);
        } else if( type.equalsIgnoreCase("CANOpen") ) {
            return new CANOpenPresentation(robot);
        } else {
            // handle invalid or unsupported presentation types
            throw new IllegalArgumentException("Invalid presentation type: " + type);
        }
    }
}
