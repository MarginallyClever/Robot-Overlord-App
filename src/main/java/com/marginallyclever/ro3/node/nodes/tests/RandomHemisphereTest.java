package com.marginallyclever.ro3.node.nodes.tests;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.apps.pathtracer.PathTracerHelper;
import com.marginallyclever.ro3.apps.pathtracer.halton.HaltonWithMemory;
import com.marginallyclever.ro3.mesh.proceduralmesh.GenerativeMesh;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * A procedural mesh that generates random points on a hemisphere using different sampling methods.
 * This is used to visualize different sampling techniques for path tracing and confirm their correctness.
 * The type of sampling can be changed by setting the 'type' variable:
 * <ul>
 * <li>0: Uniformly random points on the unit sphere</li>
 * <li>1: Uniformly random points on the hemisphere oriented along the Z-axis</li>
 * <li>2: Cosine-weighted random points on the hemisphere oriented along the Z-axis</li>
 * </ul>
 */
public class RandomHemisphereTest extends Node {
    public int type = 0;
    private final MeshInstance meshInstance = new MeshInstance();
    private final Material material = new Material();
    private final GenerativeMesh mesh = new GenerativeMesh();

    public RandomHemisphereTest() {
        super("RandomHemisphereTest");
        meshInstance.setMesh(mesh);
        mesh.setRenderStyle(GL3.GL_POINTS);
        material.setDiffuseColor(Color.YELLOW);
        material.setLit(false);
        updateModel();
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new RandomHemisphereTestPanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        addChild(meshInstance);
        addChild(material);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        removeChild(meshInstance);
        removeChild(material);
    }

    public void updateModel() {
        mesh.clear();
        HaltonWithMemory halton = new HaltonWithMemory();
        halton.resetMemory(0xDEADBEEFL);
        //Vector3d up = new Vector3d(0,0,1);
        var up = MatrixHelper.getZAxis(meshInstance.getWorld());

        switch(type){
            case 0: createRandomHemispherePoints(()->PathTracerHelper.getRandomUnitVector(halton)); break;
            case 1: createRandomHemispherePoints(()->PathTracerHelper.getRandomUnitHemisphere(halton,up)); break;
            default: createRandomHemispherePoints(()->PathTracerHelper.getRandomCosineWeightedHemisphere(halton,up)); break;
        }
    }

    private void createRandomHemispherePoints(Supplier<Vector3d> supplier) {
        for (int i = 0; i < 10000; i++) {
            Vector3d v = supplier.get();
            v.scale(3);
            // Assuming addVertex(double x,double y,double z) exists; adjust if API differs
            mesh.addVertex(v);
        }
    }

    public void setType(int type) {
        this.type = Math.clamp(type,0,2);
        updateModel();
    }
}
