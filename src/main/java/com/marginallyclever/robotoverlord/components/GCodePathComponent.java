package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.systems.render.gcodepath.*;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Point3d;

/**
 * A {@link RenderComponent} that uses a {@link PathWalker} to systems a {@link GCodePath}.
 * @author Dan Royer
 * @since 2.5.0
 */
@ComponentDependency(components = {PoseComponent.class})
public class GCodePathComponent extends ShapeComponent implements WalkablePath<Point3d> {
    public final StringParameter filename = new StringParameter("File","");
    public final IntParameter numCommands = new IntParameter("Commands",0);
    public final DoubleParameter distanceMeasured = new DoubleParameter("Distance",0);
    public final IntParameter getCommand = new IntParameter("Show",0);
    private final double maxStepSize = 0.1;
    private Point3d location;
    private GCodePath gCodePath;

    public GCodePathComponent() {
        super();
        filename.addPropertyChangeListener(e->load(filename.get()));
    }

    @Override
    public void render(GL3 gl) {
        if(gCodePath==null) return;

        boolean tex = OpenGLHelper.disableTextureStart(gl);

        myMesh.render(gl);

        if(location!=null) {
            MatrixHelper.drawMatrix(location,3).render(gl);
        }

        OpenGLHelper.disableTextureEnd(gl,tex);
    }

    private void drawEntirePath() {
        PathWalker pathWalker = new PathWalker(null,gCodePath,maxStepSize);

        if(myMesh==null) myMesh = new Mesh();
        myMesh.clear();
        myMesh.setRenderStyle(GL3.GL_LINE_STRIP);

        while (pathWalker.hasNext()) {
            pathWalker.next();
            Point3d currentPosition = pathWalker.getCurrentPosition();
            GCodePathElement currentElement = pathWalker.getCurrentElement();
            String command = currentElement.getCommand();

            if (command.equalsIgnoreCase("G0") || command.equalsIgnoreCase("G1")) {
                if(currentElement.getExtrusion()==null) {
                    // rapid
                    myMesh.addColor(0, 0, 1,0.25f);
                } else {
                    // extrusion / milling movement
                    myMesh.addColor(1, 0, 0,1);
                }
                myMesh.addVertex( (float)currentPosition.x, (float)currentPosition.y, (float)currentPosition.z );
            } else if (command.equalsIgnoreCase("G2") || command.equalsIgnoreCase("G3")) {
                // arc
                myMesh.addColor(0, 1, 0, 1);
                myMesh.addVertex( (float)currentPosition.x, (float)currentPosition.y, (float)currentPosition.z );
            } // else unknown, ignore.
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
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("filename",filename.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        filename.parseJSON(jo.getJSONObject("filename"),context);
    }

    public void load(String filename) {
        gCodePath = PathFactory.load(filename);
        drawEntirePath();
        updateNumCommands();
    }

    private void updateNumCommands() {
        if(gCodePath==null) {
            numCommands.set(0);
            location = null;
        } else {
            numCommands.set(gCodePath.getElements().size());
            distanceMeasured.set(calculateDistance());
            updateLocation();
        }
    }

    public void reload() {
        PathFactory.reload(gCodePath);
    }
}
