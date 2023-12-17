package com.marginallyclever.ro3.texture;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;

import java.awt.image.BufferedImage;

public class TextureWithMetadata {
    private final BufferedImage image;
    private Texture texture;
    private final String source;

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
     * Must only be called when there is a valid OpenGL render context, likely from within
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
    }

    /**
     * Must only be called when there is a valid OpenGL render context, likely from within
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
}
