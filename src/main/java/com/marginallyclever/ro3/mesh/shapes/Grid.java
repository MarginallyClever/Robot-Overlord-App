package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;

/**
 * <p>{@link Grid} is a {@link Mesh} displaying a grid on the XY plane.  The origin is at the center of the grid.</p>
 *
 */
public class Grid extends ProceduralMesh {
    public double width = 100;
    public double length = 100;
    public double spacing = 5;

    public Grid() {
        super();
        this.setRenderStyle(GL3.GL_LINES);
        updateModel();
    }

    @Override
    public String getEnglishName() {
        return "Grid";
    }

    /**
     * Draw a grid of lines in the current color
     */
    @Override
    public void updateModel() {
        this.clear();
        this.clear();

        double halfWidth = width/2f;
        double halfHeight = length/2f;

        double startx = -halfWidth;
        double starty = -halfHeight;
        double rx = startx % spacing;
        double ry = starty % spacing;
        startx -= rx;
        starty -= ry;
        double endx = startx + width;
        double endy = starty + length;

        for(double i=startx;i<=endx;i+=spacing) {
            this.addNormal(0,0,1);    this.addVertex((int)i, (int)starty,0);
            this.addNormal(0,0,1);    this.addVertex((int)i, (int)0     ,0);
            this.addNormal(0,0,1);    this.addVertex((int)i, (int)0     ,0);
            this.addNormal(0,0,1);    this.addVertex((int)i, (int)endy  ,0);
        }

        for(double i=starty;i<=endy;i+=spacing) {
            this.addNormal(0,0,1);    this.addVertex((int)startx, (int)i,0);
            this.addNormal(0,0,1);    this.addVertex((int)0     , (int)i,0);
            this.addNormal(0,0,1);    this.addVertex((int)0     , (int)i,0);
            this.addNormal(0,0,1);    this.addVertex((int)endx  , (int)i,0);
        }

        fireMeshChanged();
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
