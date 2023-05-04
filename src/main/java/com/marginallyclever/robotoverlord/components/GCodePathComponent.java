package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementSlider;
import com.marginallyclever.robotoverlord.systems.render.gcodepath.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3d;

/**
 * A {@link RenderComponent} that uses a {@link PathWalker} to systems a {@link GCodePath}.
 * @author Dan Royer
 * @since 2.5.0
 */
public class GCodePathComponent extends RenderComponent implements WalkablePath<Point3d> {
    private static final Logger logger = LoggerFactory.getLogger(GCodePathComponent.class);

    public final StringParameter filename = new StringParameter("File","");
    public final IntParameter numCommands = new IntParameter("Commands",0);
    public final DoubleParameter distanceMeasured = new DoubleParameter("Distance",0);
    public final IntParameter getCommand = new IntParameter("Show",0);

    private final double maxStepSize = 0.1;
    private Point3d location;
    private ViewElementSlider slider;

    private GCodePath gCodePath;

    public GCodePathComponent() {
        super();
        filename.addPropertyChangeListener(e->load(filename.get()));
    }

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        if(entity!=null) {
            entity.addComponent(new PoseComponent());
        }
    }

    @Override
    public void render(GL2 gl2) {
        if(gCodePath==null) return;

        boolean tex = OpenGLHelper.disableTextureStart(gl2);
        boolean light = OpenGLHelper.disableLightingStart(gl2);

        drawEntirePath(gl2);

        OpenGLHelper.disableTextureEnd(gl2,tex);
        OpenGLHelper.disableLightingEnd(gl2,light);
    }

    private void drawEntirePath(GL2 gl2) {
        PathWalker pathWalker = new PathWalker(null,gCodePath,maxStepSize);
        gl2.glBegin(GL2.GL_LINE_STRIP);

        double prevX = 0, prevY = 0, prevZ = 0;
        gl2.glColor4d(0, 0, 1,0.25);

        while (pathWalker.hasNext()) {
            pathWalker.next();
            Point3d currentPosition = pathWalker.getCurrentPosition();
            GCodePathElement currentElement = pathWalker.getCurrentElement();
            String command = currentElement.getCommand();

            if (command.equalsIgnoreCase("G0") || command.equalsIgnoreCase("G1")) {
                if(currentElement.getExtrusion()==null) {
                    // rapid
                    gl2.glColor4d(0, 0, 1,0.25);
                } else {
                    // extrusion / milling movement
                    gl2.glColor3d(1, 0, 0);
                }
                gl2.glVertex3d(currentPosition.x,currentPosition.y,currentPosition.z);
            } else if (command.equalsIgnoreCase("G2") || command.equalsIgnoreCase("G3")) {
                // arc
                gl2.glColor3d(0, 1, 0);
                gl2.glVertex3d(currentPosition.x,currentPosition.y,currentPosition.z);
            } // else unknown, ignore.
        }

        gl2.glEnd();

        if(location!=null) {
            PrimitiveSolids.drawStar(gl2,location,3);
        }
    }

    public void updateLocation() {
        location = get(getCommand.get());
    }

    private double calculateDistance() {
        double sum = 0;

        PoseComponent myPose = this.getEntity().getComponent(PoseComponent.class);
        PathWalker pathWalker = new PathWalker(myPose,gCodePath,maxStepSize);
        Point3d now = new Point3d();
        Point3d next = new Point3d();
        while (pathWalker.hasNext()) {
            pathWalker.next();
            next.set(pathWalker.getCurrentPosition());
            sum += now.distance(next);
            now.set(next);
        }
        return sum;
    }

    public PathWalker getPathWalker() {
        if(gCodePath==null) return null;
        PoseComponent myPose = this.getEntity().getComponent(PoseComponent.class);
        return new PathWalker(myPose,gCodePath,maxStepSize);
    }

    public double getNumCommands() {
        return numCommands.get();
    }

    @Override
    public double getDistanceMeasured() {
        return distanceMeasured.get();
    }

    /**
     * Get the position at a given distance along the gcodepath.
     * @param d how far to travel along the gcodepath, where d is a value between 0 and distanceMeasured.
     * @return position in world at distance d or null if d is out of range.
     */
    @Override
    public Point3d get(double d) {
        double sum = 0;
        if(gCodePath==null) return null;

        PoseComponent myPose = this.getEntity().getComponent(PoseComponent.class);
        PathWalker pathWalker = new PathWalker(null,gCodePath,5);
        Point3d now = new Point3d();
        Point3d nextPosition;
        while (pathWalker.hasNext()) {
            pathWalker.next();
            nextPosition = pathWalker.getCurrentPosition();
            double stepSize = now.distance(nextPosition);
            if(d>=sum && d<sum+stepSize) {
                double t = (d-sum)/stepSize;
                Point3d result = new Point3d();
                result.interpolate(now,nextPosition,t);
                myPose.getWorld().transform(result);
                return result;
            }
            sum += stepSize;
            now = nextPosition;
        }
        return null;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("filename",filename.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        filename.parseJSON(jo.getJSONObject("filename"));
    }

    public void load(String filename) {
        gCodePath = PathFactory.load(filename);
        updateNumCommands();
    }

    private void updateNumCommands() {
        if(gCodePath==null) {
            numCommands.set(0);
            location = null;
        } else {
            numCommands.set(gCodePath.getElements().size());
            distanceMeasured.set(calculateDistance());
            if(slider!=null) slider.setMaximum(numCommands.get());
            updateLocation();
        }
    }

    public void reload() {
        PathFactory.reload(gCodePath);
    }
}
