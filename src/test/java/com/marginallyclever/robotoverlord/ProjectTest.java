package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.Test;

public class ProjectTest {
    @Test
    public void createEmptyProject() {
        Project p = new Project();
        assert(p.getPath()==null);
        assert(p.getEntityManager()!=null);
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

}
