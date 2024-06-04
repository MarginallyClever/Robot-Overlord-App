package com.marginallyclever.ro3.node.nodes.odenode.brain;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.odenode.brain.v2.Brain;
import com.marginallyclever.ro3.node.nodes.odenode.brain.v2.CortisolSimulator;
import com.marginallyclever.ro3.node.nodes.odenode.brain.v2.DopamineSimulator;

import javax.vecmath.Matrix4d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BrainManager {
    // build a list of all body pose matrices, relative to the world
    private final List<Matrix4d> matrices = new ArrayList<>();
    // build a list of contacts for each body
    private final List<Boolean> isTouching = new ArrayList<>();
    public static int FPS = 30;
    public static int MEMORY_SECONDS = 3;
    private final static int MEMORY_FRAMES = FPS * MEMORY_SECONDS;  // fps * seconds
    private int k=0;
    private final Brain brain = new Brain(new DopamineSimulator(),new CortisolSimulator());
    private int inputIndex=0;

    private BufferedImage image;

    public BrainManager() {
        // create a list of neurons
        // create a list of synapses
    }

    public void setNumInputs(int size) {
        // make isTouching size equal to bodies.size();
        // make matrices size equal to bodies.size();
        isTouching.clear();
        matrices.clear();
        for (int i = 0; i < size; ++i) {
            isTouching.add(false);
            matrices.add(new Matrix4d());
        }

        // every matrix is 12 inputs and can be expressed as 4 RGB pixels with 3 colors each.
        // every is touching is 1 input.  use a single color chanel of each pixel.
        // force data from the hinges is 1 matrix each.
        image = new BufferedImage(size * 5, MEMORY_FRAMES, BufferedImage.TYPE_INT_RGB);
        eraseBrainScan();
        brain.setNumInputs(size * 12 + size);
    }

    public void setNumOutputs(int size) {
        brain.setNumOutputs(size);
    }

    private void eraseBrainScan() {
        // Fill the image with black color
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
    }

    public void setMatrix(int i, Matrix4d world) {
        matrices.get(i).set(world);
    }

    public void setTouching(int i, boolean touching) {
        isTouching.set(i, touching);
    }

    public double getOutput(int i) {
        return brain.getOutput(i);
    }

    public void update(double dt) {
        if(Registry.getPhysics().isPaused()) return;

        // move every row of the image down one.
        updateImage();
        // write new data to the top row.
        writeInputsToTopLineOfImage();

        // update brain
        brain.propagate();

        inputIndex=0;
        brain.resetConnections();
    }

    private void writeInputsToTopLineOfImage() {
        // every matrix value is copied to a pixel in the first row of the image.
        // every isTouching value is copied to a pixel in the first row of the image.
        k = 0;
        for(Matrix4d m : matrices) {
            addPixelToTopRow(m.m00, m.m10, m.m20, 1, -1);
            addPixelToTopRow(m.m01, m.m11, m.m21, 1, -1);
            addPixelToTopRow(m.m02, m.m12, m.m22, 1, -1);
            addPixelToTopRow(m.m03, m.m13, m.m23, 1, -1);
        }

        for(int i=0; i<matrices.size();++i) {
            var touching = isTouching.get(i++);
            image.setRGB(k++, 0, touching ? 0xff0000:0 );
            brain.setInput(inputIndex++,touching?1:0);
        }
    }

    private void addPixelToTopRow(double x,double y,double z,int max,int min) {
        double f = 255.0 / (max-min);
        int r = (int)( (Math.max(Math.min(x,max),min) - min) * f );
        int g = (int)( (Math.max(Math.min(y,max),min) - min) * f );
        int b = (int)( (Math.max(Math.min(z,max),min) - min) * f );
        image.setRGB(k++, 0, (r << 16) | (g << 8) | b);
        brain.setInput(inputIndex++,r);
        brain.setInput(inputIndex++,g);
        brain.setInput(inputIndex++,b);
    }

    public BufferedImage getImage() {
        return image;
    }

    // move every row of the image down one.
    void updateImage() {
        // Copy each row from the original image to itself, one row lower.  start from the bottom.
        for (int y = MEMORY_FRAMES -1; y>0; --y) {
            for (int x=0;x<image.getWidth();++x) {
                image.setRGB(x,y,image.getRGB(x,y-1));
            }
        }
    }

    public void createInitialConnections() {
        brain.createInitialConnections();
    }
}
