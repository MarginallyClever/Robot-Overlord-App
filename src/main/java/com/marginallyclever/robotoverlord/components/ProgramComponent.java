package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;

/**
 * The ProgramComponent holds run-time information about the program being executed by a robot.
 * The ProgramExecutorSystem will use this information to execute the program.
 * When placed in the same {@link com.marginallyclever.robotoverlord.Entity} as a {@link RobotComponent}, this
 * {@link Component} will allow a ProgramExecutor a robot to a execute {@link PathComponent}s
 * and {@link ProgramEventComponent}s.
 *
 * @since 2.6.0
 * @author Dan Royer
 */
public class ProgramComponent extends Component {
    public ReferenceParameter programEntity = new ReferenceParameter("Program",null);
    public ReferenceParameter stepEntity = new ReferenceParameter("Step",null);
    public BooleanParameter isRunning = new BooleanParameter("Running",false);
    public BooleanParameter isStepMode = new BooleanParameter("Step mode",false);

    public ProgramComponent() {
        super();
    }
}
