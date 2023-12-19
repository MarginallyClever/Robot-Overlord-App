package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;

public class DrawBackground implements RenderPass {
    private int activeStatus = ALWAYS;
    private final ColorRGB eraseColor = new ColorRGB(64,64,128);

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
    public void init(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {}

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void draw() {
        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        gl3.glClearColor(eraseColor.red/255.0f,
                        eraseColor.green/255.0f,
                        eraseColor.blue/255.0f,
                        1);
        gl3.glDepthMask(true);
        gl3.glColorMask(true,true,true,true);
        gl3.glStencilMask(0xFF);
        gl3.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
    }
}
