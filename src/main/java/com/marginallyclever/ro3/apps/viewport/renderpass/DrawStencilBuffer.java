package com.marginallyclever.ro3.apps.viewport.renderpass;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.ro3.apps.viewport.Viewport;

/**
 * DrawStencilBuffer is a render pass that is responsible for rendering the stencil buffer to the viewport.
 */
public class DrawStencilBuffer extends AbstractRenderPass {
    public DrawStencilBuffer() {
        super("Stencil Buffer");
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        super.reshape(glAutoDrawable, x, y, width, height);
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
        int requiredSize = width * height;
/*
        // Always allocate a new ByteBuffer of the correct size
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(requiredSize);

        // Read stencil buffer to CPU memory
        gl3.glReadPixels(0, 0, width, height, GL3.GL_STENCIL_INDEX, GL3.GL_UNSIGNED_BYTE, buffer);

        // Copy buffer to stencilData for texture upload
        buffer.rewind();

        // Create and upload to a GL texture
        int[] texId = new int[1];
        gl3.glGenTextures(1, texId, 0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, texId[0]);
        gl3.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_R8, width, height, 0, GL3.GL_RED, GL3.GL_UNSIGNED_BYTE, buffer);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);

        // 3. Draw fullscreen quad with the texture (assumes you have a simple shader and quad VAO)
        // Bind your shader and set uniforms as needed
        // Bind VAO for fullscreen quad
        // gl3.glBindVertexArray(quadVaoId);
        // gl3.glDrawArrays(GL3.GL_TRIANGLE_STRIP, 0, 4);

        // Cleanup
        gl3.glDeleteTextures(1, texId, 0);
*/
    }
}
