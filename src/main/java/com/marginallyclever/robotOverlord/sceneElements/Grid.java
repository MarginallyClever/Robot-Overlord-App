package com.marginallyclever.robotOverlord.sceneElements;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.IntEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

public class Grid extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5329188355998127781L;
	public IntEntity width = new IntEntity("Width (cm)",100);
	public IntEntity height = new IntEntity("Height (cm)",100);
	private PoseEntity follow;
	private MaterialEntity m = new MaterialEntity();
	
	public Grid() {
		super("Grid");
		
		addChild(width);
		addChild(height);
		addChild(m);
		m.setLit(false);
		m.setDiffuseColor(0, 0, 0, 0);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		
		drawGrid(gl2,width.get(),height.get(),5);
		
		gl2.glPopMatrix();
	}

	/**
	 * Draw a grid of lines in the current color
	 * @param gl2 the render context
	 * @param gridWidth the dimensions of the grid
	 * @param gridHeight the dimensions of the grid
	 * @param gridSpace the distance between lines on the grid.
	 */
	private void drawGrid(GL2 gl2,int gridWidth,int gridHeight,int gridSpace) {
		m.render(gl2);
		double[] c = m.getDiffuseColor();
		double r=c[0];
		double g=c[1];
		double b=c[2];
		
		Vector3d p = (follow!=null) ? MatrixHelper.getPosition(follow.getPoseWorld()) : new Vector3d(0,0,0);
		p.z=0;
		
		gl2.glNormal3d(0,0,1);

		double halfWidth = gridWidth/2;
		double halfHeight = gridHeight/2;
		
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
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Gr", "Grid");
		super.getViewOfChildren(view);
		view.popStack();
		super.getView(view);
	}

	public void shadow(PoseEntity toFollow) {
		follow = toFollow;
	}
}
