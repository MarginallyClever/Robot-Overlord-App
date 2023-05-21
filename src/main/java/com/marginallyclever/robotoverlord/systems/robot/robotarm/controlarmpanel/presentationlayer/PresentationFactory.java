package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.presentationlayer;

import com.marginallyclever.robotoverlord.robots.Robot;

/**
 * A Presentation Layer in the OSI model is responsible for presenting data to the user.
 * This factory creates a Presentation Layer for a specific robot.
 *
 * @author Dan Royer
 * @since 2.5.6
 */
public class PresentationFactory {
    public static final String [] AVAILABLE_PRESENTATIONS = {
            "Marlin",
            "GRBL",
            //"CANOpen",
    };

    public static PresentationLayer createPresentation(String type,Robot robot) {
        if (type.equalsIgnoreCase("Marlin")) {
            return new MarlinPresentation(robot);
        } else if (type.equalsIgnoreCase("GRBL")) {
            return new GRBLPresentation(robot);
        } else {
            // handle invalid or unsupported presentation types
            throw new IllegalArgumentException("Invalid presentation type: " + type);
        }
    }
}
