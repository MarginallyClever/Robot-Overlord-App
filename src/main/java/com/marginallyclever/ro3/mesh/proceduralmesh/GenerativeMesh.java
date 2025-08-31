package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.marginallyclever.ro3.mesh.Mesh;

/**
 * {@link GenerativeMesh} is a {@link Mesh} that is generated procedurally by some other part of the system.
 */
public class GenerativeMesh extends ProceduralMesh {
    public GenerativeMesh() {
        super();
    }

    @Override
    public void updateModel() {}

    @Override
    public String getEnglishName() {
        return "GenerativeMesh";
    }
}
