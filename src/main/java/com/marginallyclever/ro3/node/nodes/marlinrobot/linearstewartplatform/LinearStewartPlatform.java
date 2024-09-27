package com.marginallyclever.ro3.node.nodes.marlinrobot.linearstewartplatform;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.marlinrobot.MarlinListener;
import com.marginallyclever.ro3.node.nodes.marlinrobot.MarlinRobot;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.LookAt;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * When this node is added to the scene, it creates a Linear Stewart Platform.
 * The Linear Stewart Platform is a 6DOF robot that can move in any direction.
 * When it attaches to the scene, it loads in "C:\Users\aggra\Desktop\RO3 test scenes\sp-linear\sp-linear2.ro"
 * which is a scene file that contains the Linear Stewart Platform.
 */
public class LinearStewartPlatform extends MarlinRobot {
    private final Logger logger = LoggerFactory.getLogger(LinearStewartPlatform.class);

    private static final String RESOURCE_PATH = "/com/marginallyclever/ro3/node/nodes/marlinrobot/linearstewartplatform/";
    private static final String RESOURCE_BASE = RESOURCE_PATH + "001 stewart platform linear v12.obj";
    private static final String RESOURCE_ARM  = RESOURCE_PATH + "arm assembly v4.obj";
    private static final String RESOURCE_EE   = RESOURCE_PATH + "end effector with magnets v1.obj";
    private static final String RESOURCE_CAR  = RESOURCE_PATH + "car.obj";

    private static final double ARM_LENGTH = 11.340;  //cm
    private static final String [] namesBase = {"X","C","Z","Y","B","A"};
    private static final String [] namesEE = {"X","Y","Z","A","B","C"};
    private Pose ee;
    private final List<Pose> cars = new ArrayList<>();
    private final List<Ray> rays = new ArrayList<>();

    public LinearStewartPlatform() {
        this("Linear Stewart Platform");
    }

    public LinearStewartPlatform(String name) {
        super(name);
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LinearStewartPlatformPanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        // make sure Base has a MeshInstance for RESOURCE_BASE
        if(this.findChild("MeshInstance")==null) {
            MeshInstance i = new MeshInstance();
            this.addChild(i);
            var m = Registry.meshFactory.load(RESOURCE_BASE);
            i.setMesh(m);
        }
        // make sure Base has a Material.
        var m = makeSureHasMaterial(this);
        m.setDiffuseColor(Color.YELLOW);

        prepareEndEffector();

        cars.clear();
        int i=0;
        // for (key in  names {X,Y,Z,A,B,C})
        for(String key : namesBase) {
            prepareOneArm(key,i++);
        }
    }

    private void prepareEndEffector() {
        // make sure Base has a Pose "EE"
        ee = (Pose)this.findChild("EE");
        if(ee==null) {
            ee = new Pose("EE");
            this.addChild(ee);
        }
        // make sure EE has a MeshInstance for RESOURCE_EE
        if(ee.findChild("MeshInstance")==null) {
            MeshInstance i = new MeshInstance();
            ee.addChild(i);
            i.setMesh(Registry.meshFactory.load(RESOURCE_EE));
        }
        // make sure EE has a Material.
        var m = makeSureHasMaterial(ee);
        m.setDiffuseColor(Color.RED);
        // position EE
        ee.setPosition(new Vector3d(0,0,30));
        ee.setRotationEuler(new Vector3d(0,0,60-90),MatrixHelper.EulerSequence.XYZ);

        // add six children to EE named
        int i=0;
        for(String key : namesEE) {
            Pose p = (Pose)ee.findChild(key);
            if(p==null) {
                p = new Pose(key);
                ee.addChild(p);
            }

            p.setPosition(new Vector3d(0.75 * ((i%2==0)?1:-1),3.6742,-2.4));

            Matrix4d rot = new Matrix4d();
            rot.rotZ(Math.toRadians((int)(i/2)*120));
            var pWorld = p.getWorld();
            pWorld.mul(rot,pWorld);
            p.setWorld(pWorld);

            p.setRotationEuler(new Vector3d(0,0,(int)(i/2)*120),MatrixHelper.EulerSequence.XYZ);
            ++i;
        }
    }

    private Material makeSureHasMaterial(Node p) {
        Material m = (Material)p.findChild("Material");
        if(m==null) {
            m = new Material();
            p.addChild(m);
        }
        return m;
    }

    private void prepareOneArm(String key,int i) {
        // make sure this has a child Pose named key
        Pose car = (Pose)this.findChild(key);
        if(car==null) {
            car = new Pose(key);
            this.addChild(car);
        }
        cars.add(car);

        if(car.findChild("MeshInstance")==null) {
            // make sure car has a MeshInstance for RESOURCE_CAR
            MeshInstance mesh = new MeshInstance();
            car.addChild(mesh);
            mesh.setMesh(Registry.meshFactory.load(RESOURCE_CAR));
            mesh.setRotationEuler(new Vector3d(0,90,180), MatrixHelper.EulerSequence.XYZ);
        }
        car.setPosition(new Vector3d(10.015,1.6 * ((i%2==0)?1:-1),10.6));

        var carWorld = car.getWorld();
        Matrix4d rot = new Matrix4d();
        rot.rotZ(Math.toRadians((int)(i/2)*120));
        carWorld.mul(rot,carWorld);
        car.setWorld(carWorld);

        // make sure car has a Material.
        var m = makeSureHasMaterial(car);
        m.setDiffuseColor(Color.BLUE);

        // make sure car has a Pose "attachPoint"
        Pose attachPoint = (Pose)car.findChild("AttachPoint");
        if(attachPoint==null) {
            attachPoint = new Pose("AttachPoint");
            car.addChild(attachPoint);
        }

        LookAt arm = new LookAt();
        attachPoint.addChild(arm);

        if(arm.findChild("MeshInstance")==null) {
            // make sure attachPoint has a MeshInstance for RESOURCE_ARM
            MeshInstance mesh = new MeshInstance();
            arm.addChild(mesh);
            mesh.setMesh(Registry.meshFactory.load(RESOURCE_ARM));
        }
        // make sure attachPoint has a Material.
        m = makeSureHasMaterial(arm);
        m.setDiffuseColor(Color.GREEN);

        attachPoint.setPosition(new Vector3d(-3.5493,-0.0011,7.2653));
        // set the target
        arm.setTarget((Pose)ee.findChild(key));

        Vector3d attachPos = MatrixHelper.getPosition(attachPoint.getWorld());
        Vector3d startPos = new Vector3d(attachPos);
        startPos.z=0;
        Ray ray = new Ray(startPos, new Vector3d(0,0,1));
        rays.add(ray);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        updateCarPositions();
    }

    private String generateGCode() {
        StringBuilder gcode = new StringBuilder("G0");

        // for each car,
        for(Pose car : cars) {
            // generate GCode to move the car to the target.
            Vector3d carPos = car.getPosition();
            gcode.append(' ');
            gcode.append(car.getName());
            gcode.append(StringHelper.formatDouble(carPos.z));
        }
        return gcode.toString();
    }

    /**
     * for each car, adjust the z height of the car so that the attachPoint is always ARM_LENGTH away from the ee
     * target.  Only make the adjustment if all cars can reach their targets.
     */
    private void updateCarPositions() {
        double [] newHeights = new double[cars.size()];
        boolean canReach = getNewHeights(newHeights);
        if(!canReach) return;

        // if we get this far then all cars can reach their targets.
        for(int i = 0; i< cars.size(); i++) {
            Pose car = cars.get(i);
            Matrix4d m = car.getWorld();
            Vector3d carPos = MatrixHelper.getPosition(m);
            carPos.z = newHeights[i];
            MatrixHelper.setPosition(m,carPos);
            car.setWorld(m);
        }
    }

    /**
     * There is a sphere centered at the eeTarget with radius ARM_LENGTH.
     * There is a ray along the axis of travel of the linear actuator.  The ray starts at z=0 in the
     * {@link LinearStewartPlatform}'s coordinate system.
     * the ray intersects the sphere at a point.  That point is the attachPoint of the car on the linear actuator.
     * the new z height of the car is the intersection point minus the z height of the attachPoint.
     * @param newHeights the new z height of each car.
     * @return true if all cars can reach their targets.
     */
    private boolean getNewHeights(double[] newHeights) {
        for(int i = 0; i< cars.size(); i++) {
            Pose car = cars.get(i);
            Pose target = (Pose)ee.findChild(car.getName());
            if(target==null) continue;

            // This code assumes the LinearStewartPlatform is at/aligned-with the world origin.
            Vector3d carPos = MatrixHelper.getPosition(car.getWorld());
            Pose attachPoint = (Pose)car.findChild("AttachPoint");
            var attachPointWorld = MatrixHelper.getPosition(attachPoint.getWorld());
            var car2Attach = new Vector3d(attachPointWorld);
            car2Attach.sub(carPos);

            Vector3d targetPos = MatrixHelper.getPosition(target.getWorld());

            double distance = IntersectionHelper.raySphere(rays.get(i), targetPos, ARM_LENGTH) - car2Attach.z;
            if(distance>=1.2 && distance<10) {
                newHeights[i] = distance;
                Matrix4d m = car.getWorld();
                MatrixHelper.setPosition(m,carPos);
                car.setWorld(m);
            } else {
                // can't reach
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Send a single gcode command to the robot.  It will reply by firing a
     * {@link MarlinListener#messageFromMarlin} event with the String response.</p>
     * @param gcode GCode command
     */
    @Override
    public void sendGCode(String gcode) {
        if(!super.isConnected()) {
            if (gcode.startsWith("G0")) fireMarlinMessage(parseG0(gcode));
            else if(gcode.startsWith("M114")) fireMarlinMessage(parseM114());
            else if(gcode.startsWith("G28")) fireMarlinMessage(parseG28());
        }
        super.sendGCode(gcode);
    }

    private String parseG28() {
        return "Error: Find home not implemented.";
    }

    private String parseG0(String gcode) {

        return "Error: Forward Kinematics not implemented.";
    }

    private String parseM114() {
        return "Ok: "+generateGCode();
    }
}
