package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.helpers.PathHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        PathHelper.deleteDirectory(tempDirectory.toFile());
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

    // TODO in a separate test put stuff in project 'a'
    // TODO in a separate test use a different context for project 'b'
    @Test
    public void saveAndLoad() throws IOException {
        Project a = new Project();
        Project b = new Project();

        SerializationContext contexta = new SerializationContext("");
        SerializationContext contextb = new SerializationContext("");

        b.parseJSON(a.toJSON(contexta),contextb);

        SerializationContext contextc = new SerializationContext("");
        Assertions.assertEquals(a.toJSON(contextc).toString(),b.toJSON(contextc).toString());
    }
}
