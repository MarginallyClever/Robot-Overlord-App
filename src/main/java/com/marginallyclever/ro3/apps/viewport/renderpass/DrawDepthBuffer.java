package com.marginallyclever.ro3.apps.viewport.renderpass;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.mesh.Mesh;

import java.nio.ByteBuffer;

/**
 * {@link DrawDepthBuffer} is a render pass that is responsible for rendering the stencil buffer to the viewport.
 */
public class DrawDepthBuffer extends AbstractRenderPass {
    private int[] fbo;
    private int[] texId;

    public DrawDepthBuffer() {
        super("Depth Buffer");
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        super.init(glAutoDrawable);
        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        // Create FBO
        fbo = new int[1];
        gl3.glGenFramebuffers(1, fbo, 0);

        // Create a texture to store the depth data
        texId = new int[1];
        gl3.glGenTextures(1, texId, 0);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        super.dispose(glAutoDrawable);
        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        // Delete FBO
        if (fbo != null && fbo[0] != 0) {
            gl3.glDeleteFramebuffers(1, fbo, 0);
            fbo[0] = 0;
        }
        // Clean up texture
        if(texId != null && texId[0] != 0) {
            gl3.glDeleteTextures(1, texId, 0);
        }
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        super.reshape(glAutoDrawable, x, y, width, height);
        GL3 gl3 = GLContext.getCurrentGL().getGL3();

        // Allocate the texture storage
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, texId[0]);
        gl3.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_DEPTH_COMPONENT, width, height, 0, GL3.GL_DEPTH_COMPONENT, GL3.GL_FLOAT, null);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        super.display(glAutoDrawable);
    }

    @Override
    public void draw(Viewport viewport) {
        GL3 gl3 = GLContext.getCurrentGL().getGL3();

        int width = viewport.getWidth();
        int height = viewport.getHeight();

        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fbo[0]);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, texId[0]);

        // Attach depth texture to FBO
        gl3.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_TEXTURE_2D, texId[0], 0);
        // Bind FBO as read framebuffer
        gl3.glBindFramebuffer(GL3.GL_READ_FRAMEBUFFER, fbo[0]);
        // Copy the depth buffer directly to the texture
        //gl3.glCopyTexSubImage2D(GL3.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
/*
        // Set texture parameters
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);

        // Render the texture on a fullscreen quad
        //renderFullscreenQuad(gl3);
*/
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);
    }

    /**
     * Renders a fullscreen quad using the provided texture ID.
     * @param gl3 OpenGL context
     */
    private void renderFullscreenQuad(GL3 gl3) {
        // Bind the texture
        gl3.glActiveTexture(GL3.GL_TEXTURE0);

        var quad = new Mesh();
        quad.setRenderStyle(GL3.GL_QUADS);
        quad.addVertex(-1, -1, 0);
        quad.addVertex(1, -1, 0);
        quad.addVertex(-1, 1, 0);
        quad.addVertex(1, 1, 0);
        quad.addTexCoord(0, 0);
        quad.addTexCoord(1, 0);
        quad.addTexCoord(0, 1);
        quad.addTexCoord(1, 1);
        quad.render(gl3);
    }
}
