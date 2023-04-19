package com.marginallyclever.robotoverlord.physics.ode;

import org.junit.jupiter.api.Test;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.OdeHelper;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ODEPhysicsTests {
	@Test
	public void planeGetQuaternionFail() {
		OdeHelper.initODE2(0);
		DSpace space = OdeHelper.createHashSpace(null);
		DPlane plane = OdeHelper.createPlane(space,0,0,1,0);
		assertThrows(NullPointerException.class, ()->plane.getQuaternion());
	}
}
