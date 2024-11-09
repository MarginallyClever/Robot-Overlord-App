package com.marginallyclever.ro3.mesh.shapes;

import com.marginallyclever.ro3.mesh.Mesh;

public abstract class ProceduralMesh extends Mesh {
    /**
     * Procedurally generate a list of triangles
     */
    abstract public void updateModel();

    abstract public String getEnglishName();
}
