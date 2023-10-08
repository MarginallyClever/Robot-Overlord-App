package com.marginallyclever.robotoverlord.components;

import com.github.sarxos.webcam.Webcam;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ProjectorComponent  extends ShapeComponent implements PropertyChangeListener {
    private boolean hasCapturedFrame = false;
    private int width, height;
    private Texture texture;
    private MaterialComponent myMaterial;

    public ProjectorComponent() {
        super();

        myMesh = new Mesh();
    }

    @Override
    public void render(GL3 gl) {
        if(myMaterial == null) {
            myMaterial = this.getEntity().getComponent(MaterialComponent.class);
        }
        updateTexture(gl);
        if(myMaterial != null && texture!=null) {
            myMaterial.texture.setTexture(texture);
        }

        myMesh.setRenderStyle(GL3.GL_QUADS);
        int v = 50;
        myMesh.addVertex(-v, -v, 0);
        myMesh.addVertex(v, -v, 0);
        myMesh.addVertex(v, v, 0);
        myMesh.addVertex(-v, v, 0);
        myMesh.addTexCoord(0, 0);
        myMesh.addTexCoord(1, 0);
        myMesh.addTexCoord(1, 1);
        myMesh.addTexCoord(0, 1);

        super.render(gl);
    }

    private void updateTexture(GL3 gl) {
        BufferedImage image = captureFrame();

        if (texture == null) {
            texture = AWTTextureIO.newTexture(gl.getGLProfile(), image, true);
        } else {
            texture.updateImage(gl, AWTTextureIO.newTextureData(gl.getGLProfile(), image, true));
        }
    }

    private BufferedImage captureFrame() {
        // get default webcam and open it
        Webcam webcam = Webcam.getDefault();
        webcam.open();
        BufferedImage image = webcam.getImage();
        width = image.getWidth();
        height = image.getHeight();
        hasCapturedFrame = true;
        return image;
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
