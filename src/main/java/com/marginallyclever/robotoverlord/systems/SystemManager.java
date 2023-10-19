package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.motor.MotorSystem;
import com.marginallyclever.robotoverlord.systems.physics.PhysicsSystem;
import com.marginallyclever.robotoverlord.systems.render.RenderSystem;
import com.marginallyclever.robotoverlord.systems.robot.RobotGripperSystem;
import com.marginallyclever.robotoverlord.systems.robot.crab.CrabRobotSystem;
import com.marginallyclever.robotoverlord.systems.robot.dog.DogRobotSystem;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.ProgramExecutorSystem;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.RobotArmSystem;
import com.marginallyclever.robotoverlord.systems.vehicle.VehicleSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages all the {@link EntitySystem}s.
 */
public class SystemManager {

    private final List<EntitySystem> systems = new ArrayList<>();
    
    public SystemManager(EntityManager entityManager) {
        systems.add(new PhysicsSystem());
        systems.add(new RenderSystem());
        systems.add(new OriginAdjustSystem());
        systems.add(new RobotArmSystem(entityManager));
        systems.add(new DogRobotSystem(entityManager));
        systems.add(new CrabRobotSystem(entityManager));
        systems.add(new ProgramExecutorSystem(entityManager));
        systems.add(new RobotGripperSystem(entityManager));
        systems.add(new MotorSystem(entityManager));
        systems.add(new VehicleSystem(entityManager));
        //systems.add(new SoundSystem());
    }

    public void update(double dt) {
        for(EntitySystem system : systems) {
            system.update(dt);
        }
    }

    public List<? extends EntitySystem> getList() {
        return new ArrayList<>(systems);
    }
}
