package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class OpenGLTestStencil extends OpenGLTestPerspective {
    private int width=0,height=0;
    private final int [] stencilFrameBuffer = new int[1];
    private final int [] stencilTexture = new int[1];
    private ShaderProgram shaderDebugTexture;

    public OpenGLTestStencil(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        GL3 gl3 = drawable.getGL().getGL3();

        super.init(drawable);
        drawable.getGL().glEnable(GL3.GL_STENCIL_TEST);
        shaderDebugTexture = new ShaderProgram(gl3,
                readResource("debugTexture_330.vert"),
                readResource("testTextureDepth1_330.frag"));
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        GL3 gl3 = drawable.getGL().getGL3();

        super.reshape(drawable, x, y, width, height);
        this.width=width;
        this.height=height;
        destroyStencilTexture(gl3);
        createStencilTexture(gl3);
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        GL3 gl3 = drawable.getGL().getGL3();
        gl3.glClear(GL3.GL_DEPTH_BUFFER_BIT | GL3.GL_STENCIL_BUFFER_BIT | GL3.GL_COLOR_BUFFER_BIT);

        gl3.glEnable(GL3.GL_STENCIL_TEST);
        gl3.glClear(GL3.GL_STENCIL_BUFFER_BIT);

        gl3.glStencilFunc(GL.GL_ALWAYS,1,0xff);
        gl3.glStencilOp(GL3.GL_KEEP, GL3.GL_KEEP, GL3.GL_REPLACE);
        //gl3.glStencilMask(0xFF);

        super.display(drawable);  // draws a spinning triangle in perspective mode.

        gl3.glDisable(GL3.GL_STENCIL_TEST);

        copyStencilBufferToTexture(gl3);
        debugTexture(gl3,stencilTexture[0]);
    }

    private void copyStencilBufferToTexture(GL3 gl3) {
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, stencilTexture[0]);

        // Read the stencil data into the stencil texture
        try {
            gl3.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL3.GL_STENCIL_BUFFER_BIT, GL3.GL_NEAREST);
            //gl3.glCopyTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_STENCIL_INDEX, 0, 0, width, height, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Unbind everything
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);
    }

    private void destroyStencilTexture(GL3 gl3) {
        gl3.glDeleteTextures(1, stencilTexture, 0);
        gl3.glDeleteFramebuffers(1, stencilFrameBuffer, 0);
    }

    private void createStencilTexture(GL3 gl3) {
        gl3.glGenFramebuffers(1, stencilFrameBuffer, 0);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, stencilFrameBuffer[0]);

        gl3.glGenTextures(1, stencilTexture, 0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, stencilTexture[0]);
        gl3.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_DEPTH24_STENCIL8, width, height, 0, GL3.GL_DEPTH_STENCIL, GL3.GL_UNSIGNED_INT_24_8, null);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);

        gl3.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_STENCIL_ATTACHMENT, GL3.GL_TEXTURE_2D, stencilTexture[0], 0);

        checkFrameBufferStatus(gl3,"stencil");

        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
    }

    private void checkFrameBufferStatus(GL3 gl3,String name) {
        // Check if the framebuffer is complete
        int result = gl3.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER);
        if(result != GL3.GL_FRAMEBUFFER_COMPLETE) {
            System.out.print("Error: "+name+" framebuffer is not complete.  ");
            switch (result) {
                case GL3.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT ->
                        System.out.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
                case GL3.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS ->
                        System.out.println("GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS");
                case GL3.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT ->
                        System.out.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
                case GL3.GL_FRAMEBUFFER_UNSUPPORTED -> System.out.println("GL_FRAMEBUFFER_UNSUPPORTED");
                default -> System.out.println("Unknown error!");
            }
        }
    }

    /**
     * Render the given texture to the screen, for debugging purposes.
     * @param gl3 The OpenGL state
     * @param textureID The texture to render
     */
    private void debugTexture(GL3 gl3, int textureID) {
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, textureID);
        // Use the debug texture shader
        shaderDebugTexture.use(gl3);
        // Set the debugTexture uniform to use texture unit 0
        shaderDebugTexture.set1i(gl3,"debugTexture",textureID);
        // Render a full-screen quad
        renderScreenSpaceQuad(gl3);
        // Unbind the shader
        gl3.glUseProgram(0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);
    }

    private void renderScreenSpaceQuad(GL3 gl3) {
        Mesh mesh = new Mesh();
        mesh.setRenderStyle(GL3.GL_QUADS);
        float v = 0.9f;
        mesh.addNormal(0,0,1);  mesh.addTexCoord(0,0);  mesh.addColor(1,1,1,1);  mesh.addVertex(-v, -v,0.0f);
        mesh.addNormal(0,0,1);  mesh.addTexCoord(1,0);  mesh.addColor(1,1,1,1);  mesh.addVertex( v, -v,0.0f);
        mesh.addNormal(0,0,1);  mesh.addTexCoord(1,1);  mesh.addColor(1,1,1,1);  mesh.addVertex( v,  v,0.0f);
        mesh.addNormal(0,0,1);  mesh.addTexCoord(0,1);  mesh.addColor(1,1,1,1);  mesh.addVertex(-v,  v,0.0f);
        mesh.render(gl3);
    }

    public static void main(String[] args) {
        // make a frame
        JFrame frame = new JFrame( OpenGLTestStencil.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        OpenGLTestStencil opengl = new OpenGLTestStencil(null);
        frame.setContentPane(opengl.getPanel());
        frame.setPreferredSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
