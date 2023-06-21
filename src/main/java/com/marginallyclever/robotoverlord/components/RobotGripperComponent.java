package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * A gripper is a component that can grab and hold things.
 *
 * @since 2.6.1
 * @author Dan Royer
 */
public class RobotGripperComponent extends ShapeComponent {
    public static final String [] names = new String[] {
            "Opening",
            "Open",
            "Closing",
            "Closed"
    };

    public static final int MODE_OPENING = 0;
    public static final int MODE_OPEN = 1;
    public static final int MODE_CLOSING = 2;
    public static final int MODE_CLOSED = 3;

    public IntParameter mode = new IntParameter("Mode",MODE_OPEN);
    public DoubleParameter openDistance = new DoubleParameter("Open Distance (cm)",5.0);
    public DoubleParameter closeDistance = new DoubleParameter("Close Distance (cm)",1.0);
    private final Vector3d gripDirection = new Vector3d();

    public RobotGripperComponent() {
        super();
        myMesh = new Mesh();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("mode",mode.toJSON(context));
        jo.put("openDistance",openDistance.toJSON(context));
        jo.put("closeDistance",closeDistance.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject json, SerializationContext context) {
        mode.parseJSON(json.getJSONObject("mode"),context);
        openDistance.parseJSON(json.getJSONObject("openDistance"),context);
        if(json.has("closeDistance")) closeDistance.parseJSON(json.getJSONObject("closeDistance"),context);
        super.parseJSON(json,context);
    }

    /**
     * @return the center of the first two child entities in world space OR an empty list if there are not two children.
     */
    public List<Point3d> getPoints() {
        List<Entity> children = getEntity().getChildren();
        List<Point3d> results = new ArrayList<>();
        for(Entity child : children) {
            if(child.getComponent(RobotGripperJawComponent.class)==null) continue;
            Matrix4d pose = child.getComponent(PoseComponent.class).getWorld();
            results.add(new Point3d(MatrixHelper.getPosition(pose)));
        }
        return results;
    }

    /**
     *
     * @return the {@link ShapeComponent} of all children.
     */
    public List<RobotGripperJawComponent> getJaws() {
        List<RobotGripperJawComponent> results = new ArrayList<>();
        List<Entity> children = getEntity().getChildren();
        for(Entity child : children) {
            RobotGripperJawComponent jaw = child.getComponent(RobotGripperJawComponent.class);
            if(jaw!=null) results.add(jaw);
        }
        return results;
    }

    @Override
    public void render(GL3 gl) {
        List<Entity> children = getEntity().getChildren();
        if(children.size()<2) return;

        myMesh.setRenderStyle(GL3.GL_LINES);
        myMesh.clear();
        for(RobotGripperJawComponent jaw : getJaws()) {
            Matrix4d m = jaw.getEntity().getComponent(PoseComponent.class).getLocal();
            Vector3d p = MatrixHelper.getPosition(m);
            Vector3d z = MatrixHelper.getZAxis(m);
            double d = (openDistance.get() - closeDistance.get());
            z.scaleAdd(d,z,p);

            myMesh.addColor(1.0f,0.0f,0.5f,1.0f);  myMesh.addVertex((float)p.x,(float)p.y,(float)p.z);
            myMesh.addColor(1.0f,0.5f,1.0f,1.0f);  myMesh.addVertex((float)z.x,(float)z.y,(float)z.z);
        }
        myMesh.render(gl);
    }
}
