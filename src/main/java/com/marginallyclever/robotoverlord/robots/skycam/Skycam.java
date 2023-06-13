package com.marginallyclever.robotoverlord.robots.skycam;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.Vector3DParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3d;

/**
 * A skycam robot suspends a gondola inside a box from four cables attached to motors at the top corners of the box.
 *
 * @author Dan Royer
 * @since 1.7.1
 */
@Deprecated
public class Skycam extends Component {
	private static final Logger logger = LoggerFactory.getLogger(Skycam.class);
	protected transient Vector3DParameter size = new Vector3DParameter("size",100,100,100);
	private PoseComponent eePose;
	
	public Skycam() {
		super();
	}

	@Override
	public void onAttach() {
		Entity maybe = getEntity().findByPath("./ee");
		Entity ee = (maybe!=null) ? maybe : new Entity("ee");
		eePose = ee.getComponent(PoseComponent.class);
		eePose.setPosition(new Vector3d(0,0,0));
	}

	public boolean setPosition(Vector3d p) {
		Vector3d s = size.get();

		boolean ok=true;
		if(p.x> s.x) { p.x =  s.x; ok=false; }
		if(p.x<-s.x) { p.x = -s.x; ok=false; }
		if(p.y> s.y) { p.y =  s.y; ok=false; }
		if(p.y<-s.y) { p.y = -s.y; ok=false; }
		if(p.z> s.z) { p.z =  s.z; ok=false; }
		if(p.z<-s.z) { p.z = -s.z; ok=false; }
		eePose.setPosition(p);

		return ok;
	}
}
