package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.List;

public class DrawPose implements RenderPass {
    private int activeStatus = ALWAYS;
    private double scale=1.0;

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
        return "Pose";
    }

    @Override
    public void draw(ShaderProgram shader) {
        GL3 gl3 = GLContext.getCurrentGL().getGL3();

        // draw the world pose of every node in the Registry.
        List<Node> toScan = new ArrayList<>(Registry.scene.getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            if(node instanceof Pose pose) {
                Matrix4d w = pose.getWorld();
                // set modelView to world
                w.transpose();
                shader.setMatrix4d(gl3,"modelView",w);
                // draw one MatrixHelper here.
            }
        }
    }
}
