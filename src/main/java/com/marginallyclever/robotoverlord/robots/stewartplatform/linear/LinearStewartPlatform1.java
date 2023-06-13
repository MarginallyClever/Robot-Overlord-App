package com.marginallyclever.robotoverlord.robots.stewartplatform.linear;

import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;

/**
 * A linear stewart platform with 6 legs.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Deprecated
public class LinearStewartPlatform1 extends LinearStewartPlatformCore {
    private final MeshFromFile baseModel;
    private final MeshFromFile eeModel;
    private final MeshFromFile armModel;

    public LinearStewartPlatform1() {
        super();

        // load models and fix scale/orientation.
        baseModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/linear/base.stl");
        //baseModel.setShapeScale(0.1);
        eeModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/linear/endEffector.stl");
        //eeModel.setShapeScale(0.1);
        //eeModel.setShapeRotation(new Vector3d(0,0,-30));
        armModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/linear/arm.stl");
        //armModel.setShapeScale(0.1);
    }
}
