package com.marginallyclever.ro3.node.nodes.stewartplatform.rotary;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.mesh.proceduralmesh.ProceduralMeshFactory;
import com.marginallyclever.ro3.mesh.proceduralmesh.Waldo;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * <p>A Stewart Platform node with rotary actuators.  Automatically adds a "top" Pose node and a "bottom" Pose node as
 * children.  The top and bottom poses each have six connection points.  The top and
 * bottom also get a {@link MeshInstance} child to visualize the platform.
 *
 * A single Material is added to the root node and applied to all MeshInstances.
 *
 * The bottom connection points (shoulders) have six rotating "biceps".  each bicep has a MeshInstance.
 * The top connection points (wrists) have ball joint links to "forearms".  each forearms has a MeshInstance.
 * The biceps and the forearms meet at the ball joint "elbows".</p>
 *
 * <p>Several parameters can be tweaked at run time:</p>
 * <ul>
 *     <li>the xyz offset of the first bottom connection point relative to the base.  the second is a mirror of the
 *     first; the rest are 120 and 240 degree offset repeats of the first two.</li>
 *     <li>the xyz offset of the top connection point relative to the end effector.  the second is a mirror of the
 *     first; the rest are 120 and 240 degree offset repeats of the first two.</li>
 *     <li>The length of the bicep</li>
 *     <li>The length of the forearm</li>
 *     <li>The radius of the ball joints</li>
 * </ul>
 * <p>The system should be able to generate approximate jacobians at any given pose.  When the top is moved the system
 * should generate a list of angle values for each motor and display that value as gcode in the control panel.</p>
 * <p>When the system is homed the biceps rest on the z=0 plane of the base.  The offset of the
 * first bottom connection point minus the radius of the ball joint gives the opposite of the right angle triangle.  the
 * hypotenuse is the length of the bicep.  The home angle of the bicep can be found with this information.</p>
 * <p>Given the homed position of the bottom connection point; the offset of the top connection point; and the fact
 * that, when homed, the end effector's origin is at x=0 and y=0... it should be possible to calculate the initial z
 * height of the end effector.</p>
 */
public class RotaryStewartPlatform3 extends Node {
    public static final int NUM_ACTUATORS = 6;
    private static final int [] CARDINALITY = {0,5,2,1,4,3};
    private static final double TOP_AT_HOME_POSITION = 20.045;  // from Fusion360 model

    private Pose bottom = null;
    private Pose top = null;
    private final Vector3d topOffset = new Vector3d(4.4452, .70,-1.75);
    private final Vector3d bottomOffset = new Vector3d(8.7, 2.2, 2.4);
    private double bicepLength=5.0;
    private double forearmLength=19.3;

    private static class Arm {
        public Pose shoulder;
        public Pose elbow;
        public Pose wrist;
        public double motorAngle;  // degrees
        public double previousAngle;  // degrees
    }
    private final Arm [] arms = new Arm[NUM_ACTUATORS];

    public RotaryStewartPlatform3() {
        super("RotaryStewartPlatform3");
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new RotaryStewartPlatform3Panel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        attachBottomMesh();
        attachTopMesh();
        attachAllArmMeshes();
        refreshShape();
        updateLimbs();
        storePreviousAngles();
    }

    private void attachAllArmMeshes() {
        for(int i=0;i<NUM_ACTUATORS;i++) {
            if(arms[i]==null) {
                attachOneArmMesh(i);
            }
        }
    }

    private void attachOneArmMesh(int i) {
        arms[i] = new Arm();

        var shoulder = arms[i].shoulder = new Pose("shoulder"+(i+1));
        bottom.addChild(shoulder);
        var bicepMesh = new MeshInstance();
        shoulder.addChild(bicepMesh);
        var m = new Material();
        m.setDiffuseColor(new Color(0xFF,0,0));
        shoulder.addChild(m);
        bicepMesh.setMesh(Registry.meshFactory.get(Lifetime.SCENE,"/com/marginallyclever/ro3/node/nodes/rotarystewartplatform3/bicep.obj"));

        arms[i].elbow = new Pose("elbow"+(i+1));

        var wrist = arms[i].wrist = new Pose("wrist"+(i+1));
        top.addChild(wrist);
        var forearmMesh = new MeshInstance();
        wrist.addChild(forearmMesh);
        m = new Material();
        m.setDiffuseColor(new Color(0x0,0xCC,0xCC));
        wrist.addChild(m);
        forearmMesh.setMesh(Registry.meshFactory.get(Lifetime.SCENE,"/com/marginallyclever/ro3/node/nodes/rotarystewartplatform3/forearm.obj"));
    }

    // attach the top mesh
    private void attachTopMesh() {
        top = this.findNodeByPath("Top", Pose.class);
        if(top!=null) return;
        top = new Pose("Top");
        this.addChild(top);
        // add a mesh instance to visualize the top plate
        var topMesh = new MeshInstance();
        top.addChild(topMesh);
        var m = new Material();
        m.setDiffuseColor(new Color(0xFF,0xCC,0x00));
        top.addChild(m);
        topMesh.setMesh(Registry.meshFactory.get(Lifetime.SCENE,"/com/marginallyclever/ro3/node/nodes/rotarystewartplatform3/top.obj"));
    }

    // attach the bottom mesh
    private void attachBottomMesh() {
        bottom = this.findNodeByPath("Bottom", Pose.class);
        if(bottom!=null) return;
        bottom = new Pose("Bottom");
        this.addChild(bottom);
        // add a mesh instance to visualize the bottom plate
        var bottomMesh = new MeshInstance();
        bottom.addChild(bottomMesh);
        bottom.addChild(new Material());
        bottomMesh.setMesh(Registry.meshFactory.get(Lifetime.SCENE,"/com/marginallyclever/ro3/node/nodes/rotarystewartplatform3/base.obj"));
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("topOffsetX", topOffset.x);
        json.put("topOffsetY", topOffset.y);
        json.put("topOffsetZ", topOffset.z);
        json.put("bottomOffsetX", bottomOffset.x);
        json.put("bottomOffsetY", bottomOffset.y);
        json.put("bottomOffsetZ", bottomOffset.z);
        json.put("bicepLength", bicepLength);
        json.put("forearmLength", forearmLength);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        topOffset.x = json.optDouble("topOffsetX", topOffset.x);
        topOffset.y = json.optDouble("topOffsetY", topOffset.y);
        topOffset.z = json.optDouble("topOffsetZ", topOffset.z);
        bottomOffset.x = json.optDouble("bottomOffsetX", bottomOffset.x);
        bottomOffset.y = json.optDouble("bottomOffsetY", bottomOffset.y);
        bottomOffset.z = json.optDouble("bottomOffsetZ", bottomOffset.z);
        bicepLength = json.optDouble("bicepLength", bicepLength);
        forearmLength = json.optDouble("forearmLength", forearmLength);
        refreshShape();
    }

    /**
     * Find or add 6 MeshInstances with Waldos into the top and bottom.
     * Then adjust the position of each Waldo according to the offsets.
     * Remember the top plate is rotated 60 degrees from the bottom and each "arm" is 120 degrees from the previous.
     */
    private void refreshShape() {
        if(bottom!=null) {
            setWaldoPositions(bottom,bottomOffset,0, CARDINALITY);
        }

        if(top!=null) {
            setWaldoPositions(top,topOffset,0, CARDINALITY);
        }
        adjustTopToMinimumHeight();
    }

    private void adjustTopToMinimumHeight() {
        top.setPosition(new Vector3d(0,0,TOP_AT_HOME_POSITION));
    }

    private void setWaldoPositions(Pose pose, Vector3d v,double offsetAngleDegrees,int [] cardinality) {
        List<Pose> waldoes = getOrCreateWaldos(pose);
        for(int i=0;i<NUM_ACTUATORS;i+=2) {
            var angle = Math.toRadians(i * 120 + offsetAngleDegrees);
            var px = new Vector3d(Math.cos(angle), Math.sin(angle),0);
            var py = new Vector3d(-Math.sin(angle), Math.cos(angle),0);
            var sum = new Vector3d(0,0,v.z);
            sum.scaleAdd(v.x, px, sum);
            sum.scaleAdd(-v.y, py, sum);
            waldoes.get(cardinality[i]).setPosition(sum);
            sum.set(0,0,v.z);
            sum.scaleAdd(v.x, px, sum);
            sum.scaleAdd(v.y, py, sum);
            waldoes.get(cardinality[i+1]).setPosition(sum);
        }
    }

    private List<Pose> getOrCreateWaldos(Pose parent) {
        List<Pose> waldoes = new java.util.ArrayList<>();
        int found=0;
        // find or create Waldo nodes under the given MeshInstance
        for(Node n : parent.getChildren()) {
            if(n instanceof Pose child && !(n instanceof MeshInstance)) {
                found++;
                waldoes.add(child);
                if(found==NUM_ACTUATORS) break;
            }
        }
        while(waldoes.size()<NUM_ACTUATORS) {
            var p = new Pose("joint"+waldoes.size());
            parent.addChild(p);
            waldoes.add(p);

            var mi = new MeshInstance();
            p.addChild(mi);
            Waldo waldo = (Waldo)ProceduralMeshFactory.createMesh("Waldo");
            assert waldo != null;
            waldo.setRadius(15);
            waldo.updateModel();
            mi.setMesh(waldo);
            mi.setHasShadow(false);
        }
        return waldoes;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        updateLimbs();
        if(poseHasChanged()) {
            firePoseUpdate();
            storePreviousAngles();
        }
    }

    private void updateLimbs() {
        findElbowPositions();
        pointForearmsTowardElbows();
        pointBicepTowardsElbows();
        calculateMotorAngles();
    }

    private void storePreviousAngles() {
        for(Arm a : arms) {
            a.previousAngle = a.motorAngle;
        }
    }

    /**
     * for each motor, find the angle of the bicep relative to the z=0 plane of the bottom.
     * make sure to account for the mirrored position of every other motor and the 120 degree offset of each pair.
     */
    private void calculateMotorAngles() {
        var bottomWorld = bottom.getWorld();
        var ibw = new Matrix4d(bottomWorld);
        ibw.invert();

        for(int i=0;i<NUM_ACTUATORS;i++) {
            var elbowWorld = arms[i].elbow.getWorld();
            elbowWorld.mul(ibw);
            var elbowPos = MatrixHelper.getPosition(elbowWorld);
            var shoulderWorld = arms[i].shoulder.getWorld();
            shoulderWorld.mul(ibw);
            var shoulderPos = MatrixHelper.getPosition(shoulderWorld);
            double z = elbowPos.z - shoulderPos.z;
            // given z (opposite) and bicep length (hypotenuse) find angle theta.
            if (bicepLength <= 0) {
                arms[i].motorAngle = 0.0;
                continue;
            }
            double ratio = z / bicepLength;
            // clamp to valid domain for asin to avoid NaN from small numerical errors
            if (ratio > 1.0) ratio = 1.0;
            if (ratio < -1.0) ratio = -1.0;
            // store the computed angle (degrees) in the arm for later use
            arms[i].motorAngle = Math.toDegrees(Math.asin(ratio));
        }
    }

    private boolean poseHasChanged() {
        for(Arm a : arms) {
            if(a.previousAngle != a.motorAngle) return true;
        }
        return false;
    }

    private void pointBicepTowardsElbows() {
        // the z axis of the model aligns with the motor axle.
        // the bicep mesh is attached to the shoulder pose, which is at the bottom connection point.
        // to orient the bicep, we can point the shoulder's x axis at the elbow.
        for(int i=0;i<NUM_ACTUATORS;++i) {
            if (arms[i] == null) continue;
            Pose shoulder = arms[i].shoulder;
            Pose elbowPose = arms[i].elbow;
            if (shoulder == null || elbowPose == null) continue;

            try {
                Vector3d sp = MatrixHelper.getPosition(shoulder.getWorld());
                Vector3d ep = MatrixHelper.getPosition(elbowPose.getWorld());

                Vector3d x = new Vector3d();
                x.sub(ep, sp);
                x.normalize();

                double theta = switch(i) {
                    case 0 -> 0;
                    case 1 -> -120;
                    case 2 -> -120;
                    case 3 -> -240;
                    case 4 -> -240;
                    default -> 0;
                };
                theta = Math.toRadians(theta);
                Vector3d y = new Vector3d(Math.cos(theta),Math.sin(theta),0);
                y.normalize();

                Vector3d z = new Vector3d();
                z.cross(x, y);
                z.normalize();

                //y.cross(z, x);

                // build world matrix with columns X,Y,Z and translation sp
                Matrix4d m = new Matrix4d();
                m.setIdentity();
                MatrixHelper.setXAxis(m, x);
                MatrixHelper.setYAxis(m, y);
                MatrixHelper.setZAxis(m, z);
                MatrixHelper.setPosition(m, sp);

                shoulder.setWorld(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pointForearmsTowardElbows() {
        for (int i = 0; i < NUM_ACTUATORS; ++i) {
            // wrist is the pose at the top that holds the forearm mesh
            Pose wrist = arms[i].wrist;
            Pose elbow = arms[i].elbow;
            if (wrist == null || elbow == null) continue;

            // compute world-space positions
            Vector3d wp = MatrixHelper.getPosition(wrist.getWorld());
            Vector3d ep = MatrixHelper.getPosition(elbow.getWorld());

            // if wrist and elbow coincide or are extremely close, skip orientation
            Vector3d diff = new Vector3d();
            diff.sub(ep, wp);
            if (diff.length() < 1e-6) continue;

            // build a lookAt orientation so the wrist's forward axis points at the elbow
            Matrix3d look = MatrixHelper.lookAt(wp, ep);
            Matrix4d m4 = new Matrix4d();
            m4.set(look);
            m4.setTranslation(wp);
            wrist.setWorld(m4);
        }
    }

    /**
     * <p>Given:</p>
     * <ul>
     *     <li>The position of each wrist</li>
     *     <li>The position of each shoulder</li>
     *     <li>The length of the biceps</li>
     *     <li>The length of the forearms</li>
     * </ul>
     * <p>Project the wrist position onto the plane of rotation of the bicep, adjust the length of the forearm
     * accordingly, and use intersection of circles to find the position of the elbow.</p>
     * <p>The first plane of rotation has normal x=1.
     * The second is rotated 120 degrees from the first.
     * The third is rotated 240 degrees from the first.</p>
     */
    private void findElbowPositions() {
        // get shoulder and wrist reference poses (lazy)
        List<Pose> bottomWaldos = getOrCreateWaldos(bottom);
        List<Pose> topWaldos = getOrCreateWaldos(top);

        for (int i = 0; i < NUM_ACTUATORS; ++i) {
            Pose shoulderPose = bottomWaldos.get(i);
            Pose wristPose = topWaldos.get(i);

            Vector3d shoulder = MatrixHelper.getPosition(shoulderPose.getWorld());
            Vector3d wrist = MatrixHelper.getPosition(wristPose.getWorld());

            // determine the plane normal for this actuator.
            // The platform has three rotation planes with normals at 0, 120, and 240 degrees in XY.
            // Find which cardinality pair this actuator belongs to so we pick the correct plane.
            int posIndex = -1;
            for (int k = 0; k < CARDINALITY.length; ++k) {
                if (CARDINALITY[k] == i) {
                    posIndex = k;
                    break;
                }
            }
            int planeIndex = Math.max(0, posIndex / 2); // 0..2
            double planeAngle = Math.toRadians(planeIndex * -120.0);
            Vector3d n = new Vector3d(Math.cos(planeAngle), Math.sin(planeAngle), 0); // plane normal in XY
            // in-plane axes: u is in-plane horizontal (perp to normal in XY), v is world Z
            Vector3d u = new Vector3d(-n.y, n.x, 0);
            u.normalize();
            Vector3d v = new Vector3d(0, 0, 1);

            // vector from shoulder to wrist
            Vector3d ST = new Vector3d();
            ST.sub(wrist, shoulder);

            // signed perpendicular offset of wrist from the bicep plane
            double h = ST.dot(n);

            // project wrist onto the plane through the shoulder
            Vector3d P = new Vector3d();
            P.scaleAdd(-h,n,wrist);  // wrist - n*h

            // effective radius of forearm circle in the plane
            double r2sq = forearmLength * forearmLength - h * h;
            double r2 = r2sq <= 0 ? 0.0 : Math.sqrt(r2sq);

            // coordinates of P relative to S in (u,v) basis
            Vector3d PS = new Vector3d();
            PS.sub(P, shoulder);
            double dx = PS.dot(u);
            double dy = PS.dot(v);
            double d = Math.hypot(dx, dy);

            double r1 = bicepLength;
            Vector3d chosen = new Vector3d();

            if (d < 1e-9) {
                // The wrist projects onto the shoulder. Pick a default elbow along +u
                chosen.scaleAdd(r1,u,shoulder);
            } else {
                // circle-circle intersection in plane
                if (d > r1 + r2 || d < Math.abs(r1 - r2)) {
                    // no proper intersection; choose the closest point on the bicep circle toward P
                    double scale = r1 / d;
                    Vector3d dir = new Vector3d(dx, dy, 0);
                    dir.scale(scale);
                    // map dir (in u,v coords) back to world
                    chosen.scaleAdd(dir.x,u,shoulder);
                    chosen.scaleAdd(dir.y,v,chosen);
                } else {
                    double a = (r1 * r1 - r2 * r2 + d * d) / (2.0 * d);
                    double h_inter = Math.sqrt(Math.max(0.0, r1 * r1 - a * a));

                    // point p2 in (u,v) coords
                    double p2x = dx * (a / d);
                    double p2y = dy * (a / d);

                    // perpendicular direction in plane (normalized)
                    double px = -dy / d;
                    double py = dx / d;

                    double ix, iy;

                    // two intersection candidates in (u,v)
                    // map both back to world and choose the one closer to the wrist-midpoint
                    if(posIndex%2 == 0) {
                        ix = p2x + h_inter * px;
                        iy = p2y + h_inter * py;
                    } else {
                        ix = p2x - h_inter * px;
                        iy = p2y - h_inter * py;
                    }
                    chosen.scaleAdd(ix, u, shoulder);
                    chosen.scaleAdd(iy, v, chosen);
                }
            }

            // set elbow world position (Pose.setPosition uses local position relative to parent;
            // since elbow parent is this node (whose local is identity for the platform) this acts as world)
            arms[i].elbow.setPosition(chosen);
        }
    }

    /**
     * Find or create a marker Pose under bottom with the given name, and position it at the given world position.
     * This was used to visualize debug information while solving the kinematics.
     * @param markerName
     * @param pos
     */
    private void adjustCandidatePosition(String markerName, Vector3d pos) {
        Pose marker = null;
        for (Node n : bottom.getChildren()) {
            if (n instanceof Pose p && p.getName().equals(markerName)) {
                marker = p;
                break;
            }
        }
        if (marker == null) {
            marker = new Pose(markerName);
            bottom.addChild(marker);
            var mi = new MeshInstance();
            marker.addChild(mi);
            marker.addChild(new Material());
            Waldo w = (Waldo) ProceduralMeshFactory.createMesh("Waldo");
            assert w != null;
            w.setRadius(5);
            w.updateModel();
            mi.setMesh(w);
            mi.setHasShadow(false);
        }
        // position it
        marker.setPosition(pos);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(PropertyChangeListener.class, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(PropertyChangeListener.class, listener);
    }

    private void firePoseUpdate() {
        PropertyChangeEvent event = null;
        for( PropertyChangeListener pcl : listeners.getListeners(PropertyChangeListener.class)) {
            if(event==null) {
                // lazy allocation
                event = new PropertyChangeEvent(this, "pose", null, null);
            }
            pcl.propertyChange(event);
        }
    }

    public Double [] getMotorAngles() {
        Double [] angles = new Double[NUM_ACTUATORS];
        for(int i = 0; i < NUM_ACTUATORS; i++) {
            angles[i] = arms[i].motorAngle;
        }
        return angles;
    }
}
