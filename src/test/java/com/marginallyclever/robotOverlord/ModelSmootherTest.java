package com.marginallyclever.robotOverlord;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.shape.MeshSmoother;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

public class ModelSmootherTest {
	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	

    //@Test
    public void smoothAll() throws IOException {
        float vertexEpsilon = 0.1f;
        float normalEpsilon = 0.25f;
        String wd = System.getProperty("user.dir");
        Log.message("Working directory=" + wd);

        Log.message("hand");
        MeshSmoother.smoothModel("/AH/WristRot.stl", wd + "/AH/WristRot-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("anchor");
        MeshSmoother.smoothModel("/AH/rotBaseCase.stl", wd + "/AH/rotBaseCase-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("shoulder");
        MeshSmoother.smoothModel("/AH/Shoulder_r1.stl", wd + "/AH/Shoulder_r1-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("elbow");
        MeshSmoother.smoothModel("/AH/Elbow.stl", wd + "/AH/Elbow-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("forearm");
        MeshSmoother.smoothModel("/AH/Forearm.stl", wd + "/AH/Forearm-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("wrist");
        MeshSmoother.smoothModel("/AH/Wrist_r1.stl", wd + "/AH/Wrist_r1-smooth.stl", vertexEpsilon, normalEpsilon);
    }

}
