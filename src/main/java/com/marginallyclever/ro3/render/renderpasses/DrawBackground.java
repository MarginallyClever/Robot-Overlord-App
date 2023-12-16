package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;

public class DrawBackground implements com.marginallyclever.ro3.render.RenderPass {
    private int activeStatus = ALWAYS;

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
        return "Erase/Background";
    }

    @Override
    public void draw(ShaderProgram shader) {
        GL3 gl = GLContext.getCurrentGL().getGL3();
        gl.glClearColor(0.25f,0.25f,0.5f,1);

        gl.glDepthMask(true);
        gl.glColorMask(true,true,true,true);
        gl.glStencilMask(0xFF);
        // erase!
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
    }
}
