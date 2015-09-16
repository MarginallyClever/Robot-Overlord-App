package com.marginallyclever.evilOverlord;

import javax.media.opengl.GL2;

public class LightObject extends ObjectInWorld {
	public int index=0;
    public float[] position={1,1,1,0};
    public float[] ambient={0.0f,0.0f,0.0f,1f};
    public float[] diffuse={1f,1f,1f,1f};
    public float[] specular={0.5f,0.5f,0.5f,1f};
    
	public void render(GL2 gl2) {
		gl2.glEnable(GL2.GL_LIGHT0+index);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position,0);
	    gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient,0);
	    gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse,0);
	    gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, specular,0);
	}
}
