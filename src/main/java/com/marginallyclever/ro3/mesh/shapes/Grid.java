package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;

/**
 * <p>{@link Grid} is a {@link Mesh} displaying a grid on the XY plane.  The origin is at the center of the grid.</p>
 *
 * @author Dan Royer
 */
public class Grid extends Mesh {
    private int width = 100;
    private int length = 100;

    public Grid() {
        super();
        this.setRenderStyle(GL3.GL_LINES);

        //width.addPropertyChangeListener(e->updateMesh());
        //length.addPropertyChangeListener(e->updateMesh());
    }

    private void updateMesh() {
        this.clear();
        drawGrid(width, length, 5,0,0);
    }

    @Override
    public void render(GL3 gl) {
        super.render(gl);
        updateMesh();
    }

    /**
     * Draw a grid of lines in the current color
     * @param gridWidth the dimensions of the grid
     * @param gridLength the dimensions of the grid
     * @param gridSpace the distance between lines on the grid.
     * @param dx the offset of the grid from the origin
     * @param dy the offset of the grid from the origin
     */
    private void drawGrid(int gridWidth,int gridLength,int gridSpace,float dx,float dy) {
        this.clear();

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
            this.addNormal(0,0,1);    this.addVertex(dx+i, dy+starty,0);
            this.addNormal(0,0,1);    this.addVertex(dx+i, dy+0  ,0);
            this.addNormal(0,0,1);    this.addVertex(dx+i, dy+0  ,0);
            this.addNormal(0,0,1);    this.addVertex(dx+i, dy+endy  ,0);
        }

        for(float i=starty;i<=endy;i+=gridSpace) {
            this.addNormal(0,0,1);    this.addVertex(dx+startx, dy+i,0);
            this.addNormal(0,0,1);    this.addVertex(dx+0     , dy+i,0);
            this.addNormal(0,0,1);    this.addVertex(dx+0     , dy+i,0);
            this.addNormal(0,0,1);    this.addVertex(dx+endx  , dy+i,0);
        }
    }
/*
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
*/
    public void setWidth(int width) {
        this.width = width;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
