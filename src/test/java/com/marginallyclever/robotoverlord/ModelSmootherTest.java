package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.shapes.mesh.MeshSmoother;

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
        MeshSmoother.smoothModel("/robots/AH/WristRot.stl", wd + "/robots/AH/WristRot-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("anchor");
        MeshSmoother.smoothModel("/robots/AH/rotBaseCase.stl", wd + "/robots/AH/rotBaseCase-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("shoulder");
        MeshSmoother.smoothModel("/robots/AH/Shoulder_r1.stl", wd + "/robots/AH/Shoulder_r1-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("elbow");
        MeshSmoother.smoothModel("/robots/AH/Elbow.stl", wd + "/robots/AH/Elbow-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("forearm");
        MeshSmoother.smoothModel("/robots/AH/Forearm.stl", wd + "/robots/AH/Forearm-smooth.stl", vertexEpsilon, normalEpsilon);
        Log.message("wrist");
        MeshSmoother.smoothModel("/robots/AH/Wrist_r1.stl", wd + "/robots/AH/Wrist_r1-smooth.stl", vertexEpsilon, normalEpsilon);
    }

}
