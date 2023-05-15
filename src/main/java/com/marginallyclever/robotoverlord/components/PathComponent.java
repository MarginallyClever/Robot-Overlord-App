package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.bezier3.Bezier3;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * PathComponent may contain an orderd list of children with {@link PoseComponent}.
 */
public class PathComponent extends ShapeComponent {
    public static final String[] MOVE_TYPE_NAMES = {"Rapid","Linear"};  // ,"Bezier"
    public static final int MOVE_RAPID = 0;
    public static final int MOVE_LINEAR = 1;
    //public static final int MOVE_BEZIER = 2;
    public IntParameter moveType = new IntParameter("Move Type",MOVE_RAPID);
    public DoubleParameter moveSpeed = new DoubleParameter("Move Speed",1.0);

    public PathComponent() {
        super();
        myMesh = new Mesh();
    }

    @Override
    public void render(GL2 gl2) {
        if (!this.getVisible() || !this.getEnabled()) return;

        List<Matrix4d> waypoints = buildListOfWaypoints();
        switch (moveType.get()) {
            default -> drawRapid(gl2, waypoints);
            case MOVE_LINEAR -> drawLinear(gl2, waypoints);
            //case MOVE_BEZIER -> drawBezier(gl2, waypoints);
        }
        setModel(myMesh);
        myMesh.render(gl2);
    }

    private void drawRapid(GL2 gl2,List<Matrix4d> waypoints) {
        buildLinearPath(waypoints);
        myMesh.setRenderStyle(GL2.GL_LINE_STRIP);
    }

    private void drawLinear(GL2 gl2,List<Matrix4d> waypoints) {
        buildLinearPath(waypoints);
        myMesh.setRenderStyle(GL2.GL_LINES);
    }

    private void buildLinearPath(List<Matrix4d> waypoints) {
        myMesh.clear();
        if(waypoints.size()<2) return;

        double stepSize = moveSpeed.get();

        Iterator<Matrix4d> it = waypoints.iterator();
        Vector3d p0 = new Vector3d();
        Vector3d p1 = new Vector3d();
        Vector3d pN = new Vector3d();
        Vector3d pDiff = new Vector3d();
        Matrix4d m = it.next();
        m.get(p0);
        double remainder = 0;
        while(it.hasNext()) {
            m = it.next();
            m.get(p1);
            pDiff.sub(p1,p0);
            double distance = (float)pDiff.length();
            double i;

            myMesh.addVertex((float) p0.x, (float) p0.y, (float) p0.z);

            for(i=remainder;i<distance;i+=stepSize) {
                pN.interpolate(p0,p1,i/distance);
                myMesh.addVertex((float) pN.x, (float) pN.y, (float) pN.z);
            }
            myMesh.addVertex((float) p1.x, (float) p1.y, (float) p1.z);

            remainder = Math.max(0,i-distance);
            p0.set(p1);
        }
    }

    private List<Matrix4d> buildListOfWaypoints() {
        List<Matrix4d> waypoints = new ArrayList<>();
        List<Entity> list = getEntity().getChildren();
        for(Entity entity : list) {
            PoseComponent waypoint = entity.getComponent(PoseComponent.class);
            if(waypoint!=null) waypoints.add(waypoint.getLocal());
        }
        return waypoints;
    }
}
