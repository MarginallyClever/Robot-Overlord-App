package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A gripper is a component that can grab and hold things.
 *
 * @since 2.6.1
 * @author Dan Royer
 */
public class RobotGripperComponent extends Component {
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
        if(children.size()>=2) {
            for(int i=0;i<2;++i) {
                Matrix4d pose = children.get(i).getComponent(PoseComponent.class).getWorld();
                results.add(new Point3d(MatrixHelper.getPosition(pose)));
            }
        }
        return results;
    }

    /**
     *
     * @return the {@link ShapeComponent} of all children.
     */
    public List<ShapeComponent> getJaws() {
        List<ShapeComponent> results = new ArrayList<>();
        List<Entity> children = getEntity().getChildren();
        for(Entity child : children) {
            ShapeComponent shape = child.getComponent(ShapeComponent.class);
            if(shape!=null) results.add(shape);
        }
        return results;
    }

    /**
     * @return the direction of the gripper in local space.
     */
    public Vector3d getGripDirection() {
        return new Vector3d(gripDirection);
    }

    /**
     * Set the direction of the gripper in local space.
     * @param direction the direction of the gripper in local space.
     */
    public void setGripDirection(Vector3d direction) {
        gripDirection.set(direction);
    }
}
