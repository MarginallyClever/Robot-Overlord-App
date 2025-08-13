package com.marginallyclever.ro3.mesh;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * <p>{@link VertexProvider} is an interface for providing vertices and normals to a {@link Mesh}.  Historically
 * this has only been used for {@link com.marginallyclever.convenience.Ray} intersections.</p>
 */
public interface VertexProvider {
    /**
     * Provides a vertex at the given index.
     * @param index the index of the vertex to provide
     * @return the vertex at the given index
     */
    Point3d provideVertex(int index);

    /**
     * Provides a normal at the given index.
     * @param index the index of the vertex to provide
     * @return the vertex at the given index
     */
    Vector3d provideNormal(int index);

    /**
     * @return the number of vertexes and normals available.  This should be number of triangles * 3.
     */
    int provideCount();
}