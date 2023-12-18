package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.DHParameter;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws each {@link DHParameter} as two lines from the previous joint to
 * the current joint.
 */
public class DrawDHParameters implements RenderPass {
    private int activeStatus = ALWAYS;
    private final Mesh mesh = new Mesh();

    public DrawDHParameters() {
        // add mesh to a list that can be unloaded and reloaded as needed.
        mesh.setRenderStyle(GL3.GL_LINES);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);
    }

    /**
     * @return NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public int getActiveStatus() {
        return activeStatus;
    }

    /**
     * @param status NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public void setActiveStatus(int status) {
        activeStatus = status;
    }

    /**
     * @return the localized name of this overlay
     */
    @Override
    public String getName() {
        return "DH Parameters";
    }

    @Override
    public void draw(ShaderProgram shader) {
        GL3 gl3 = GLContext.getCurrentGL().getGL3();

        shader.set1f(gl3,"useVertexColor",1);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"useTexture",0);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);

        List<Node> toScan = new ArrayList<>();
        toScan.add(Registry.getScene());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            toScan.addAll(node.getChildren());

            if(node instanceof DHParameter parameter) {
                // set modelView to world
                Pose parentPose = parameter.findParent(Pose.class);
                Matrix4d w = (parentPose==null)
                    ? MatrixHelper.createIdentityMatrix4()
                    : parentPose.getWorld();
                w.transpose();
                shader.setMatrix4d(gl3,"modelMatrix",w);
                mesh.render(gl3);
            }
        }

        shader.set1f(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",1);
        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }
}
