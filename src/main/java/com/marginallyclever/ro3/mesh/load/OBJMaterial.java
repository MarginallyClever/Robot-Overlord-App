package com.marginallyclever.ro3.mesh.load;

public class OBJMaterial {
    public String name;
    public String texture;
    public float[] ambient  = new float[4];
    public float[] diffuse  = new float[4];
    public float[] specular = new float[4];
    public float[] emissive = new float[4];
    public float shininess;
    public float transparency;

    public OBJMaterial() {
        ambient[3] = 1;

        diffuse[0] = 1;
        diffuse[1] = 1;
        diffuse[2] = 1;
        diffuse[3] = 1;

        specular[3] = 1;

        emissive[3] = 1;
    }
}