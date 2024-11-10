package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;
import org.json.JSONObject;

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

    public void setWidth(int width) {
        this.width = width;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("length", length);
        json.put("width", width);
        json.put("spacing", spacing);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("width")) width = from.getDouble("width");
        if(from.has("length")) length = from.getDouble("length");
        if(from.has("spacing")) spacing = from.getDouble("spacing");
        updateModel();
    }
}
