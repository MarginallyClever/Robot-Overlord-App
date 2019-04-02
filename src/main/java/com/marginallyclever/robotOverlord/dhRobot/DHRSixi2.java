package com.marginallyclever.robotOverlord.dhRobot;

import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.model.ModelFactory;

public class DHRSixi2 extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public DHRSixi2() {
		super();
		setDisplayName("DHRSixi2");
	}
	
	@Override
	public void setupLinks() {
		// setup sixi2 as default.
		setNumLinks(8);
		// roll
		links.get(0).d=13.44;
		links.get(0).theta=-90;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).angleMin=-160;
		links.get(0).angleMax=160;
		// tilt
		links.get(1).alpha=-20;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).angleMin=-72;
		// tilt
		links.get(2).d=44.55;
		links.get(2).alpha=30;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).angleMin=-83.369;
		links.get(2).angleMax=86;
		// interim point
		links.get(3).d=4.7201;
		links.get(3).alpha=90;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		links.get(4).d=28.805;
		links.get(4).theta=30;
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).angleMin=-90;
		links.get(4).angleMax=90;

		// tilt
		links.get(5).d=11.8;
		links.get(5).alpha=50;
		links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(5).angleMin=-90;
		links.get(5).angleMax=90;
		// roll
		links.get(6).d=3.9527;
		links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(6).angleMin=-90;
		links.get(6).angleMax=90;
		
		links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm.stl",0.1f);
			links.get(5).model = ModelFactory.createModelFromFilename("/Sixi2/tuningFork.stl",0.1f);
			links.get(6).model = ModelFactory.createModelFromFilename("/Sixi2/picassoBox.stl",0.1f);
			links.get(7).model = ModelFactory.createModelFromFilename("/Sixi2/hand.stl",0.1f);

			double ELBOW_TO_ULNA_Y = -28.805f;
			double ELBOW_TO_ULNA_Z = 4.7201f;
			double ULNA_TO_WRIST_Y = -11.800f;
			double ULNA_TO_WRIST_Z = 0;
			double ELBOW_TO_WRIST_Y = ELBOW_TO_ULNA_Y + ULNA_TO_WRIST_Y;
			double ELBOW_TO_WRIST_Z = ELBOW_TO_ULNA_Z + ULNA_TO_WRIST_Z;
			//double WRIST_TO_HAND = 8.9527;

			links.get(0).model.adjustOrigin(new Vector3f(0, 0, 5.150f));
			links.get(1).model.adjustOrigin(new Vector3f(0, 0, 8.140f-13.44f));
			links.get(2).model.adjustOrigin(new Vector3f(-1.82f, 0, 9));
			links.get(3).model.adjustOrigin(new Vector3f(0, (float)ELBOW_TO_WRIST_Y, (float)ELBOW_TO_WRIST_Z));
			links.get(5).model.adjustOrigin(new Vector3f(0, 0, (float)-ULNA_TO_WRIST_Y));
			links.get(7).model.adjustOrigin(new Vector3f(0,0,-3.9527f));

			links.get(0).model.adjustRotation(new Vector3f(90,90,0));
			links.get(1).model.adjustRotation(new Vector3f(90,0,0));
			links.get(2).model.adjustRotation(new Vector3f(90,0,0));
			links.get(3).model.adjustRotation(new Vector3f(90,180,0));
			links.get(5).model.adjustRotation(new Vector3f(180,0,180));
			links.get(6).model.adjustRotation(new Vector3f(180,0,180));
			links.get(7).model.adjustRotation(new Vector3f(180,0,180));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
