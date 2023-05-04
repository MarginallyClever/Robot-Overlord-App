package com.marginallyclever.robotoverlord.systems.render.mesh;

import com.marginallyclever.convenience.log.Log;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ModelSmootherTest {
    private static final Logger logger = LoggerFactory.getLogger(ModelSmootherTest.class);
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
        logger.info("Working directory=" + wd);

        logger.info("hand");
        MeshSmoother.smoothModel("/robots/AH/WristRot.stl", wd + "/robots/AH/WristRot-smooth.stl", vertexEpsilon, normalEpsilon);
        logger.info("anchor");
        MeshSmoother.smoothModel("/robots/AH/rotBaseCase.stl", wd + "/robots/AH/rotBaseCase-smooth.stl", vertexEpsilon, normalEpsilon);
        logger.info("shoulder");
        MeshSmoother.smoothModel("/robots/AH/Shoulder_r1.stl", wd + "/robots/AH/Shoulder_r1-smooth.stl", vertexEpsilon, normalEpsilon);
        logger.info("elbow");
        MeshSmoother.smoothModel("/robots/AH/Elbow.stl", wd + "/robots/AH/Elbow-smooth.stl", vertexEpsilon, normalEpsilon);
        logger.info("forearm");
        MeshSmoother.smoothModel("/robots/AH/Forearm.stl", wd + "/robots/AH/Forearm-smooth.stl", vertexEpsilon, normalEpsilon);
        logger.info("wrist");
        MeshSmoother.smoothModel("/robots/AH/Wrist_r1.stl", wd + "/robots/AH/Wrist_r1-smooth.stl", vertexEpsilon, normalEpsilon);
    }

}
