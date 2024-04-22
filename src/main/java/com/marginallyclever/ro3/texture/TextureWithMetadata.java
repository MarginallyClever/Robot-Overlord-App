package com.marginallyclever.ro3.texture;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;

import java.awt.image.BufferedImage;

/**
 * {@link TextureWithMetadata} is an OpenGL {@link Texture} with metadata about where it came from and the source
 * image.
 */
public class TextureWithMetadata {
    private final BufferedImage image;
    private Texture texture;
    private final String source;
    private boolean doNotExport=false;

    public TextureWithMetadata(BufferedImage image,String source) {
        super();
        this.image = image;
        this.source = source;
    }

    public Texture getTexture() {
        return texture;
    }

    public String getSource() {
        return source;
    }

    public BufferedImage getImage() {
        return image;
    }

    /**
     * Must only be called when there is a valid OpenGL viewport context, likely from within
     * a {@link com.jogamp.opengl.GLAutoDrawable}.
     * @param shader the shader to use.
     */
    public void use(ShaderProgram shader) {
        if(texture==null) {
            BufferedImage flip = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<image.getHeight();++y) {
                for(int x=0;x<image.getWidth();++x) {
                    flip.setRGB(x,y,image.getRGB(x,image.getHeight()-y-1));
                }
            }
            texture = AWTTextureIO.newTexture(GLProfile.getDefault(), flip, false);
        }

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        if(texture==null) {
            gl3.glDisable(GL3.GL_TEXTURE_2D);
            shader.set1i(gl3,"useTexture",0);
        } else {
            gl3.glEnable(GL3.GL_TEXTURE_2D);
            texture.bind(gl3);
            shader.set1i(gl3,"useTexture",1);
            shader.set1i(gl3,"diffuseTexture",0);
        }

        // turn on texture wrapping
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT);
        // turn on mipmapping
        //gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR);
        //gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
        // turn on anisotropic filtering
        float[] maxAnisotropy = new float[1];
        gl3.glGetFloatv(GL3.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
        gl3.glTexParameterf(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy[0]);
    }

    /**
     * Must only be called when there is a valid OpenGL viewport context, likely from within
     * a {@link com.jogamp.opengl.GLAutoDrawable}.
     */
    public void unload() {
        if(texture==null) return;
        texture.destroy(GLContext.getCurrentGL().getGL3());
        texture = null;
    }

    public int getWidth() {
        if(image==null) return 0;
        return image.getWidth();
    }

    public int getHeight() {
        if(image==null) return 0;
        return image.getHeight();
    }

    public boolean isDoNotExport() {
    	return doNotExport;
    }

    public void setDoNotExport(boolean value) {
    	doNotExport=value;
    }
}
