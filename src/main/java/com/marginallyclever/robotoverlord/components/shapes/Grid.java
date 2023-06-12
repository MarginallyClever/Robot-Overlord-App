package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Vector3d;

/**
 * Procedurally draws a grid on a plane.  If the material for this Grid has lighting turned off then the
 * grid will be transparent at the edges.
 *
 * @author Dan Royer
 */
public class Grid extends ShapeComponent {
    public final BooleanParameter snap = new BooleanParameter("Snap",false);
    public final IntParameter width = new IntParameter("Width (cm)",100);
    public final IntParameter length = new IntParameter("Length (cm)",100);

    public Grid() {
        super();
        myMesh = new Mesh();
        setModel(myMesh);
        myMesh.setRenderStyle(GL3.GL_LINES);

        //width.addPropertyChangeListener(e->updateMesh());
        //length.addPropertyChangeListener(e->updateMesh());
    }

    private void updateMesh() {
        myMesh.clear();
        if(snap.get()) drawGridWithSnap(width.get(), length.get(), 5);
        else drawGrid(width.get(), length.get(), 5,0,0);
        setModel(myMesh);
    }

    @Override
    public void render(GL3 gl) {
        updateMesh();
        super.render(gl);
    }

    private void drawGridWithSnap(int gridWidth, int gridLength, int gridSpace) {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        if(pose==null) return;

        Vector3d p = pose.getPosition();
        float dx = (float)p.x % gridSpace;
        float dy = (float)p.y % gridSpace;

        drawGrid(gridWidth,gridLength,gridSpace,-dx,-dy);
    }

    /**
     * Draw a grid of lines in the current color
     * @param gl the systems context
     * @param gridWidth the dimensions of the grid
     * @param gridLength the dimensions of the grid
     * @param gridSpace the distance between lines on the grid.
     */
    private void drawGrid(int gridWidth,int gridLength,int gridSpace,float dx,float dy) {
        myMesh.clear();

        // get diffuse material color.  use black if nothing is found.
        float r=0,g=0,b=0;
        MaterialComponent mat = getEntity().getComponent(MaterialComponent.class);
        if(mat!=null) {
            double[] c = mat.getDiffuseColor();
            r = (float)c[0];
            g = (float)c[1];
            b = (float)c[2];
        }

        float halfWidth = gridWidth/2f;
        float halfHeight = gridLength/2f;

        float startx = -halfWidth;
        float starty = -halfHeight;
        float rx = startx % gridSpace;
        float ry = starty % gridSpace;
        startx -= rx;
        starty -= ry;
        float endx = startx + gridWidth;
        float endy = starty + gridLength;

        for(float i=startx;i<=endx;i+=gridSpace) {
            float v = 1.0f - Math.abs(i) / halfWidth;
            myMesh.addNormal(0,0,1);
            myMesh.addNormal(0,0,1);
            myMesh.addNormal(0,0,1);
            myMesh.addNormal(0,0,1);

            myMesh.addColor(r, g, b, 0);
            myMesh.addColor(r, g, b, v);
            myMesh.addColor(r, g, b, v);
            myMesh.addColor(r, g, b, 0);

            myMesh.addVertex(dx+i, dy+starty,0);
            myMesh.addVertex(dx+i, dy+0  ,0);
            myMesh.addVertex(dx+i, dy+0  ,0);
            myMesh.addVertex(dx+i, dy+endy  ,0);
        }

        for(float i=starty;i<=endy;i+=gridSpace) {
            float v = 1.0f - Math.abs(i) / halfHeight;
            myMesh.addNormal(0,0,1);
            myMesh.addNormal(0,0,1);
            myMesh.addNormal(0,0,1);
            myMesh.addNormal(0,0,1);

            myMesh.addColor(r, g, b, 0);
            myMesh.addColor(r, g, b, v);
            myMesh.addColor(r, g, b, v);
            myMesh.addColor(r, g, b, 0);

            myMesh.addVertex(dx+startx, dy+i,0);
            myMesh.addVertex(dx+0     , dy+i,0);
            myMesh.addVertex(dx+0     , dy+i,0);
            myMesh.addVertex(dx+endx  , dy+i,0);
        }

        setModel(myMesh);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("width",width.toJSON(context));
        jo.put("length",length.toJSON(context));
        jo.put("snap",snap.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        width.parseJSON(jo.getJSONObject("width"),context);
        length.parseJSON(jo.getJSONObject("length"),context);
        snap.parseJSON(jo.getJSONObject("snap"),context);
    }

    public void setWidth(int width) {
        this.width.set(width);
    }

    public void setLength(int length) {
        this.length.set(length);
    }
}
