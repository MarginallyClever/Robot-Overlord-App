package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.parameters.StringParameter;

import java.lang.invoke.StringConcatException;

/**
 * <p>A {@link ProgramEventComponent} is a component that can be used to trigger events in a robot program.
 * These events are not part of the robot's state, but are instead used to trigger actions in the robot's program.</p>
 * They include:</p>
 * <ul>
 *     <li>Wait for pin i/o value</li>
 *     <li>Set pin i/o value</li>
 *     <li>Set pin PWM value</li>
 * </ul>
 *
 * @author Dan Royer
 * @since 2.6.0
 */
public class ProgramEventComponent extends Component {
    public final StringParameter gcode = new StringParameter("GCode","");

    public ProgramEventComponent() {
        super();
    }
}
