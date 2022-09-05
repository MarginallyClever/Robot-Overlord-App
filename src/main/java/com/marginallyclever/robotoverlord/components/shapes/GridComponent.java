package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.IntEntity;

import javax.vecmath.Vector3d;

public class GridComponent extends ShapeComponent {
    public final IntEntity width = new IntEntity("Width (cm)",100);
    public final IntEntity height = new IntEntity("Height (cm)",100);

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        view.add(width);
        view.add(height);
    }

    @Override
    public void render(GL2 gl2) {
        drawGrid(gl2,width.get(),height.get(),5);
    }

    /**
     * Draw a grid of lines in the current color
     * @param gl2 the render context
     * @param gridWidth the dimensions of the grid
     * @param gridHeight the dimensions of the grid
     * @param gridSpace the distance between lines on the grid.
     */
    private void drawGrid(GL2 gl2,int gridWidth,int gridHeight,int gridSpace) {
        // get diffuse material color.  use black if nothing is found.
        double r=0,g=0,b=0;
        MaterialComponent mat = getEntity().getComponent(MaterialComponent.class);
        if(mat!=null) {
            double[] c = mat.getDiffuseColor();
            r=c[0];
            g=c[1];
            b=c[2];
        }

        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        Vector3d p = pose.getPosition();
        p.z=0;

        gl2.glNormal3d(0,0,1);

        double halfWidth = gridWidth/2.0;
        double halfHeight = gridHeight/2.0;

        double startx = p.x - halfWidth;
        double starty = p.y - halfHeight;
        double rx = startx % gridSpace;
        double ry = starty % gridSpace;
        startx -= rx;
        starty -= ry;
        double endx = startx + gridWidth;
        double endy = starty + gridHeight;

        boolean isBlend = gl2.glIsEnabled(GL2.GL_BLEND);
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        gl2.glBegin(GL2.GL_LINES);
        for(double i=startx;i<=endx;i+=gridSpace) {
            double v = 1.0 - Math.abs(i - p.x) / halfWidth;
            gl2.glColor4d(r, g, b, 0);			gl2.glVertex2d(i,starty);
            gl2.glColor4d(r, g, b, v);			gl2.glVertex2d(i,p.y);
            gl2.glVertex2d(i,p.y);
            gl2.glColor4d(r, g, b, 0);			gl2.glVertex2d(i,endy  );
        }
        for(double i=starty;i<=endy;i+=gridSpace) {
            double v = 1.0 - Math.abs(i - p.y) / halfHeight;
            gl2.glColor4d(r, g, b, 0);			gl2.glVertex2d(startx,i);
            gl2.glColor4d(r, g, b, v);			gl2.glVertex2d(p.x   ,i);
            gl2.glVertex2d(p.x   ,i);
            gl2.glColor4d(r, g, b, 0);			gl2.glVertex2d(endx  ,i);
        }
        gl2.glEnd();

        if(!isBlend) gl2.glDisable(GL2.GL_BLEND);
    }
}
