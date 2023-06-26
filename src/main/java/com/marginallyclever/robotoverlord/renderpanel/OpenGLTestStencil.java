package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class OpenGLTestStencil extends OpenGLTestPerspective {
    private int width=0, height=0;
    private final int [] stencilFrameBuffer = new int[1];
    private final int [] stencilTexture = new int[1];
    private ShaderProgram shaderDebugTextureUV;
    private ShaderProgram shaderDebugTexture1;
    private ShaderProgram shaderDebugTexture2;
    private int stencilMode = 0;

    public OpenGLTestStencil(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        super.init(drawable);
        GL3 gl3 = drawable.getGL().getGL3();
        shaderDebugTextureUV = new ShaderProgram(gl3,
                readResource("debugTexture_330.vert"),
                readResource("testTextureUV_330.frag"));
        shaderDebugTexture1 = new ShaderProgram(gl3,
                readResource("debugTexture_330.vert"),
                readResource("testTextureDepth1_330.frag"));
        shaderDebugTexture2 = new ShaderProgram(gl3,
                readResource("debugTexture_330.vert"),
                readResource("testTextureDepth2_330.frag"));
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        super.reshape(drawable, x, y, width, height);
        this.width=width;
        this.height=height;
        GL3 gl3 = drawable.getGL().getGL3();
        destroyStencilTexture(gl3);
        createStencilTexture(gl3);
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        GL3 gl3 = drawable.getGL().getGL3();
        gl3.glClear(GL3.GL_DEPTH_BUFFER_BIT | GL3.GL_COLOR_BUFFER_BIT);

        super.display(drawable);

        displayToStencilFramebuffer(gl3,drawable);
        copyStencilFramebufferToTexture(gl3);

        switch(stencilMode) {
            case 0 -> debugTexture(gl3,stencilTexture[0],shaderDebugTextureUV);
            case 1 -> debugTexture(gl3,stencilTexture[0],shaderDebugTexture1);
            case 2 -> debugTexture(gl3,stencilTexture[0],shaderDebugTexture2);
        }
    }

    private void displayToStencilFramebuffer(GL3 gl3, GLAutoDrawable drawable) {
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, stencilFrameBuffer[0]);
        gl3.glEnable(GL3.GL_STENCIL_TEST);

        gl3.glStencilFunc(GL.GL_ALWAYS,1,0xff);
        gl3.glStencilOp(GL3.GL_KEEP, GL3.GL_KEEP, GL3.GL_REPLACE);
        gl3.glStencilMask(0xFF);
        gl3.glClear(GL3.GL_STENCIL_BUFFER_BIT|GL3.GL_DEPTH_BUFFER_BIT);

        super.display(drawable);  // draws a spinning triangle in perspective mode.

        gl3.glDisable(GL3.GL_STENCIL_TEST);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
    }

    private void copyStencilFramebufferToTexture(GL3 gl3) {
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, stencilFrameBuffer[0]);
        checkGLError(gl3);

        //checkFrameBufferStatus(gl3,"stencil");
        // Read the stencil data into the stencil texture
        gl3.glCopyTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_STENCIL_INDEX8, 0, 0, width, height, 0);
        checkGLError(gl3);

        // Unbind everything
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
        checkGLError(gl3);
    }

    protected void checkGLError(GL3 gl3) {
        int err = gl3.glGetError();
        if(err != GL3.GL_NO_ERROR) System.out.println("Error: "+err);
    }

    private void destroyStencilTexture(GL3 gl3) {
        gl3.glDeleteTextures(1, stencilTexture, 0);
        checkGLError(gl3);
        gl3.glDeleteFramebuffers(1, stencilFrameBuffer, 0);
        checkGLError(gl3);
    }

    private void createStencilTexture(GL3 gl3) {
        // create stencil framebuffer
        gl3.glGenFramebuffers(1, stencilFrameBuffer, 0);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, stencilFrameBuffer[0]);

        // create stencil texture
        gl3.glGenTextures(1, stencilTexture, 0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, stencilTexture[0]);

        // bind the stencil texture to the framebuffer
        gl3.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_DEPTH24_STENCIL8, width, height, 0, GL3.GL_DEPTH_STENCIL, GL3.GL_UNSIGNED_INT_24_8, null);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
        gl3.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_STENCIL_ATTACHMENT, GL3.GL_TEXTURE_2D, stencilTexture[0], 0);

        // check it's sane
        checkFrameBufferStatus(gl3,"stencil");

        // unbind everything
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);
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
        } else {
            System.out.println("Framebuffer "+name+" is complete.");
        }
    }

    /**
     * Render the given texture to the screen, for debugging purposes.
     * @param gl3 The OpenGL state
     * @param textureID The texture to render
     */
    private void debugTexture(GL3 gl3,int textureID, ShaderProgram program) {
        // Use the debug texture shader
        program.use(gl3);

        // Set the depthTexture uniform to use texture unit 0
        gl3.glActiveTexture (GL3.GL_TEXTURE0);
        gl3.glBindTexture   (GL3.GL_TEXTURE_2D, textureID);
        program.set1i(gl3,"debugTexture",0);
        // Render a full-screen quad
        renderScreenSpaceQuad(gl3,0.9f);  // scaled so that we can see what's behind the quad
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);

        // Unbind the shader
        gl3.glUseProgram(0);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
            case KeyEvent.VK_7 -> stencilMode = 0;
            case KeyEvent.VK_8 -> stencilMode = 1;
            case KeyEvent.VK_9 -> stencilMode = 2;
        }
        super.keyTyped(e);
    }

    private void renderScreenSpaceQuad(GL3 gl3,float scale) {
        Mesh mesh = new Mesh();
        mesh.setRenderStyle(GL3.GL_QUADS);
        mesh.addNormal(0,0,1);  mesh.addTexCoord(0,0);  mesh.addColor(1,1,1,1);  mesh.addVertex(-scale, -scale,0.0f);
        mesh.addNormal(0,0,1);  mesh.addTexCoord(1,0);  mesh.addColor(1,1,1,1);  mesh.addVertex( scale, -scale,0.0f);
        mesh.addNormal(0,0,1);  mesh.addTexCoord(1,1);  mesh.addColor(1,1,1,1);  mesh.addVertex( scale,  scale,0.0f);
        mesh.addNormal(0,0,1);  mesh.addTexCoord(0,1);  mesh.addColor(1,1,1,1);  mesh.addVertex(-scale,  scale,0.0f);
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
