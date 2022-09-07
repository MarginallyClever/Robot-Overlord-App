package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.BooleanEntity;
import com.marginallyclever.robotoverlord.uiexposedtypes.DoubleEntity;
import com.marginallyclever.robotoverlord.uiexposedtypes.IntEntity;

import javax.vecmath.Vector3d;
import java.io.BufferedWriter;
import java.io.IOException;

public class Grid extends ShapeComponent {
    private final BooleanEntity snap = new BooleanEntity("Snap",true);
    private final IntEntity width = new IntEntity("Width (cm)",100);
    private final IntEntity length = new IntEntity("Length (cm)",100);

    @Override
    public void getView(ViewPanel view) {
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
        gl2.glPushMatrix();

        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
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
        MaterialComponent mat = getEntity().getComponent(MaterialComponent.class);
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
    }

    @Override
    public void save(BufferedWriter writer) throws IOException {
        super.save(writer);
        width.save(writer);
        length.save(writer);
        snap.save(writer);
    }
}
