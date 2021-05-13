package com.marginallyclever.robotOverlord.entity.linearStewartPlatform;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.shapeEntity.ShapeEntity;

public class LinearStewartPlatform  extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final double SLIDE_TRAVEL = 10.0;  // cm

	public static final double BASE_X=6.0968;  // cm
	public static final double BASE_Y=1.6000;  // cm
	public static final double BASE_Z=7.8383;  // cm

	public static final double EE_X= 3.6742;  // cm
	public static final double EE_Y= 0.7500;  // cm
	public static final double EE_Z=-2.4000;  // cm

	public static final double ARM_LENGTH=15.0362;  // cm

	private ShapeEntity baseModel;
	private ShapeEntity eeModel;
	
	private double [] linearPosition = new double[6];
	private PoseEntity ee = new PoseEntity("ee");

	// lowest point that the magnetic balls on each linear actuator can travel.
	// they can only move up from this point.
	Point3d [] pBase = {
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
	};
	// center of each magnetic ball at the end effector, before being transformed by ee.pose
	Point3d [] pEE = {
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
	};
	// pEE after transform by ee.pose.  will be same coordinate system as base.
	Point3d [] pEE2 = {
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
	};
	// point where arm is connected to slider after EE has moved.
	Point3d [] pSlide = {
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
			new Point3d(),
	};

	public LinearStewartPlatform() {
		super("Linear Stewart Platform");
		addChild(ee);
		
		baseModel = new ShapeEntity("/linearStewartPlatform/base.stl");
		baseModel.setShapeScale(0.1);
		eeModel = new ShapeEntity("/linearStewartPlatform/endEffector.stl");
		eeModel.setShapeScale(0.1);
		eeModel.setShapeRotation(new Vector3d(0,0,-30));
		
		for(int i=0;i<linearPosition.length;++i) {
			linearPosition[i]=0;
		}
		
		int [] indexes = {0,5,2,1,4,3};

		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();

		// calculate end effector points ONE TIME.
		// end effector points are ordered counter clockwise
		//       1
		//  2       0 <-- first
		//      x     <-- center
		//  3       5 <-- last
		//       4
		for(int i=0;i<6;++i) {
			double r = Math.toRadians(60.0+120.0*i/2.0);
			vx.set(Math.cos(r),Math.sin(r),0);
			vy.set(-vx.y,vx.x,0);
			tx.scale( EE_X,vx);
			ty.scale(-EE_Y,vy);
			pEE[i].add(tx,ty);
			pEE[i].z=EE_Z;
			++i;
			tx.scale( EE_X,vx);
			ty.scale( EE_Y,vy);
			pEE[i].add(tx,ty);
			pEE[i].z=EE_Z;
		}

		// calculate base of linear slides ONE TIME.
		// linear slides are ordered counter clockwise
		//     1
		//  2       0 <-- first
		//      x     <-- center
		//  3       5 <-- last
		//     4
		for(int i=0;i<6;++i) {
			double r = Math.toRadians(120.0*i/2.0);
			vx.set(Math.cos(r),Math.sin(r),0);
			vy.set(-vx.y,vx.x,0);
			tx.scale( BASE_X,vx);
			ty.scale( BASE_Y,vy);
			pBase[indexes[i]].add(tx,ty);
			++i;
			tx.scale( BASE_X,vx);
			ty.scale(-BASE_Y,vy);
			pBase[indexes[i]].add(tx,ty);
		}		
		
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		MaterialEntity me = new MaterialEntity();
		me.setAmbientColor(0, 0, 0, 1);
		me.setDiffuseColor(0.1f,0.1f,0.1f,1);
		me.setEmissionColor(0, 0, 0, 1);
		me.setLit(true);
		me.setShininess(0);
		me.render(gl2);
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
			baseModel.setMaterial(me);
			baseModel.render(gl2);
			
			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, ee.getPose());
			eeModel.setMaterial(me);
			eeModel.render(gl2);
			gl2.glPopMatrix();

			boolean wasLit = OpenGLHelper.disableLightingStart(gl2);

			// use calculated end effector points to find same points after EE moves.
			Matrix4d eeMatrix = ee.getPose();
			for(int i=0;i<6;++i) {
				eeMatrix.transform(pEE[i], pEE2[i]);
			}

			// We have pEE2 and pBase.  one end of the rod is at pEE2[n].  
			// The sphere formed by pDD2[n] and ARM_LENGTH intersects the vertical line at bBase[n] twice.
			// The first intersection traveling up is the one we want.
			Ray ray = new Ray();
			ray.direction.set(0,0,1);
			for(int i=0;i<6;++i) {
				ray.start.set(pBase[i]);
				linearPosition[i] = IntersectionHelper.raySphere(ray, pEE2[i], ARM_LENGTH);
				
				pSlide[i].set(pBase[i].x,
							  pBase[i].y,
							  pBase[i].z+linearPosition[i]);
			}

			// draw updated ee points.
			Vector3d eeCenter = ee.getPosition();
			gl2.glColor3d(1, 0, 0);
			gl2.glBegin(GL2.GL_LINES);
			for(int i=0;i<6;++i) {
				eeMatrix.transform(pEE[i], pEE2[i]);
				gl2.glVertex3d(eeCenter.x,eeCenter.y,eeCenter.z);
				gl2.glVertex3d(pEE2[i].x,
							   pEE2[i].y,
							   pEE2[i].z);
				gl2.glColor3d(0, 0, 0);
			}
			gl2.glEnd();
			
			// draw linear slides
			for(int i=0;i<6;++i) {
				renderOneLinearSlide(gl2,
						pSlide[i].x,
						pSlide[i].y,
						pSlide[i].z,
						BASE_Z,
						BASE_Z+SLIDE_TRAVEL);
			}
			
			// draw arms
			gl2.glColor3d(1, 0, 0);
			gl2.glBegin(GL2.GL_LINES);
			for(int i=0;i<6;++i) {
				gl2.glVertex3d(pEE2[i].x,
							   pEE2[i].y,
							   pEE2[i].z);
				gl2.glVertex3d(pSlide[i].x,
								pSlide[i].y,
								pSlide[i].z);
				gl2.glColor3d(0, 0, 0);
			}
			gl2.glEnd();
			
		gl2.glPopMatrix();

		OpenGLHelper.disableLightingEnd(gl2,wasLit);
	}
	
	private void renderOneLinearSlide(GL2 gl2,double x,double y,double h,double min,double max) {
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(1, 1, 1);		gl2.glVertex3d(x, y, min);		gl2.glVertex3d(x, y, h);
		gl2.glColor3d(0, 0, 1);		gl2.glVertex3d(x, y, h);		gl2.glVertex3d(x, y, max);
		gl2.glEnd();
	}
	
	@Override
	public void update(double dt) {
		//super.update(dt);
	}
}
