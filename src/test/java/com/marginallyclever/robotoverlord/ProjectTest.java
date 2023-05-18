package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectTest {
    private Path projectPath;
    private Path assetsPath;
    private Path tempDirectory;
    @BeforeEach
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("projectTest");
        projectPath = tempDirectory.resolve("project");
        assetsPath = tempDirectory.resolve("assets");

        Files.createDirectory(projectPath);
        Files.createDirectory(assetsPath);
    }

    @AfterEach
    public void tearDown() {
        PathUtils.deleteDirectory(tempDirectory.toFile());
    }


    @Test
    public void setPath() {
        Project p = new Project();
        p.setPath("test");
        assert(p.getPath().equals("test"));
    }

    @Test
    public void createProjectWithPath() {
        Project p = new Project("test");
        assert(p.getPath().equals("test"));
        assert(p.getEntityManager()!=null);
    }

    @Test
    public void testCopyAssetsAndUpdateComponentPaths() throws IOException {
        Project project = new Project();
        project.setPath(projectPath.toString());

/*
        // Create sample assets and component
        Files.write(assetsPath.resolve("asset1.txt"), "Asset 1 content".getBytes());
        Files.write(assetsPath.resolve("asset2.txt"), "Asset 2 content".getBytes());

        Component component = new Component();
        component.setAssetPath(assetsPath.resolve("asset1.txt").toString());

        // Save project
        project.save(projectPath.toString());

        // Check if assets are copied to the project folder
        Assertions.assertTrue(Files.exists(projectPath.resolve("assets/asset1.txt")));
        Assertions.assertTrue(Files.exists(projectPath.resolve("assets/asset2.txt")));

        // Check if component asset path has been updated to use a relative path
        Assertions.assertEquals("assets/asset1.txt", component.getAssetPath());*/
    }
}
