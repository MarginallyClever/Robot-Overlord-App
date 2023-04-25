package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Vector3d;

public class Grid extends ShapeComponent {
    private final BooleanParameter snap = new BooleanParameter("Snap",true);
    private final IntParameter width = new IntParameter("Width (cm)",100);
    private final IntParameter length = new IntParameter("Length (cm)",100);

    public Grid() {
        super();
    }

    @Override
    public void getView(ComponentPanelFactory view) {
        super.getView(view);
        view.add(width);
        view.add(length);
        view.add(snap);
    }

    @Override
    public void render(GL2 gl2) {
        if(snap.get()) drawGridWithSnap(gl2, width.get(), length.get(), 5);
        else drawGrid(gl2, width.get(), length.get(), 5);
    }

    private void drawGridWithSnap(GL2 gl2, int gridWidth, int gridLength, int gridSpace) {
        PoseComponent pose = getEntity().findFirstComponent(PoseComponent.class);
        if(pose==null) return;

        gl2.glPushMatrix();
            Vector3d p = pose.getPosition();
            double dx = p.x % gridSpace;
            double dy = p.y % gridSpace;
            gl2.glTranslated(-dx,-dy,0);

            drawGrid(gl2,gridWidth,gridLength,gridSpace);

        gl2.glPopMatrix();
    }

    /**
     * Draw a grid of lines in the current color
     * @param gl2 the render context
     * @param gridWidth the dimensions of the grid
     * @param gridLength the dimensions of the grid
     * @param gridSpace the distance between lines on the grid.
     */
    private void drawGrid(GL2 gl2,int gridWidth,int gridLength,int gridSpace) {
        // get diffuse material color.  use black if nothing is found.
        double r=0,g=0,b=0;
        MaterialComponent mat = getEntity().findFirstComponent(MaterialComponent.class);
        if(mat!=null) {
            double[] c = mat.getDiffuseColor();
            r=c[0];
            g=c[1];
            b=c[2];
        }

        gl2.glNormal3d(0,0,1);

        double halfWidth = gridWidth/2.0;
        double halfHeight = gridLength/2.0;

        double startx = -halfWidth;
        double starty = -halfHeight;
        double rx = startx % gridSpace;
        double ry = starty % gridSpace;
        startx -= rx;
        starty -= ry;
        double endx = startx + gridWidth;
        double endy = starty + gridLength;

        boolean wasTex = OpenGLHelper.disableTextureStart(gl2);

        boolean isBlend = gl2.glIsEnabled(GL2.GL_BLEND);
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        gl2.glBegin(GL2.GL_LINES);
        for(double i=startx;i<=endx;i+=gridSpace) {
            double v = 1.0 - Math.abs(i - 0) / halfWidth;
            gl2.glColor4d(r, g, b, 0);			gl2.glVertex2d(i,starty);
            gl2.glColor4d(r, g, b, v);      			gl2.glVertex2d(i,0);
                                                        gl2.glVertex2d(i,0);
            gl2.glColor4d(r, g, b, 0);			gl2.glVertex2d(i,endy  );
        }
        for(double i=starty;i<=endy;i+=gridSpace) {
            double v = 1.0 - Math.abs(i - 0) / halfHeight;
            gl2.glColor4d(r, g, b, 0);			gl2.glVertex2d(startx,i);
            gl2.glColor4d(r, g, b, v);			        gl2.glVertex2d(0  ,i);
                                                        gl2.glVertex2d(0  ,i);
            gl2.glColor4d(r, g, b, 0);			gl2.glVertex2d(endx  ,i);
        }
        gl2.glEnd();

        if(!isBlend) gl2.glDisable(GL2.GL_BLEND);
        OpenGLHelper.disableTextureEnd(gl2,wasTex);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("width",width.toJSON());
        jo.put("length",length.toJSON());
        jo.put("snap",snap.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        width.parseJSON(jo.getJSONObject("width"));
        length.parseJSON(jo.getJSONObject("length"));
        snap.parseJSON(jo.getJSONObject("snap"));
    }

    public void setWidth(int width) {
        this.width.set(width);
    }

    public void setLength(int length) {
        this.length.set(length);
    }
}
