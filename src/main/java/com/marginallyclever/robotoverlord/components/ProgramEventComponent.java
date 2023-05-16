package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.parameters.StringParameter;

/**
 * <p>A {@link ProgramEventComponent} is a component that can be used to trigger events in a robot program.
 * These events are not part of the robot's state, but are instead used to trigger actions in the robot's program.</p>
 * They include:</p>
 * <ul>
 *     <li>Home the robot</li>
 *     <li>Dwell for a period of time</li>
 *     <li>Wait for pin value</li>
 *     <li>Set pin value</li>
 * </ul>
 *
 * @author Dan Royer
 * @since 2.6.0
 */
public class ProgramEventComponent extends Component {
    public static final int HOME = 0;
    public static final int DWELL = 1;
    public static final int WAIT_PIN = 2;
    public static final int SET_PIN = 2;

    public final StringParameter gcode = new StringParameter("GCode","");

    public ProgramEventComponent() {
        super();
    }
}
