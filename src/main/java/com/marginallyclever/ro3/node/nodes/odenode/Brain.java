package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.Registry;

import javax.vecmath.Matrix4d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Brain {
    // build a list of all body pose matrices, relative to the world
    private final List<Matrix4d> matrices = new ArrayList<>();
    // build a list of contacts for each body
    private final List<Boolean> isTouching = new ArrayList<>();
    private final static int memoryLength = 30*3;  // fps * seconds
    private int k=0;

    private final List<Neuron> neurons = new ArrayList<>();
    private BufferedImage image;

    public Brain() {
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

        // every matrix is 4 inputs
        // every is touching is 1 input.
        image = new BufferedImage(size * 5, memoryLength, BufferedImage.TYPE_INT_ARGB);
        eraseBrainScan();
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
        return 0;
    }

    void update(double dt) {
        if(Registry.getPhysics().isPaused()) return;

        // move every row of the image down one.
        updateImage();
        writeInputsToTopLineOfImage();
    }

    private void writeInputsToTopLineOfImage() {
        // every matrix value is copied to a pixel in the first row of the image.
        // every isTouching value is copied to a pixel in the first row of the image.
        k = 0;
        int i=0;
        for (Matrix4d m : matrices) {
            addPixelToTopRow(m.m00, m.m10, m.m20, 1, -1);
            addPixelToTopRow(m.m01, m.m11, m.m21, 1, -1);
            addPixelToTopRow(m.m02, m.m12, m.m22, 1, -1);
            addPixelToTopRow(m.m03, m.m13, m.m23, 20, -20);
        }

        for (Matrix4d m : matrices) {
            image.setRGB(k++, 0, isTouching.get(i++) ? 0xffff0000:0 );
        }
    }

    private void addPixelToTopRow(double x,double y,double z,int max,int min) {
        double f = 255.0 / (max-min);
        int r = (int)( (Math.max(Math.min(x,max),min) - min) * f );
        int g = (int)( (Math.max(Math.min(y,max),min) - min) * f );
        int b = (int)( (Math.max(Math.min(z,max),min) - min) * f );
        image.setRGB(k++, 0, 0xff000000 | (g << 16) | (r << 8) | b);
    }

    public BufferedImage getImage() {
        return image;
    }

    // move every row of the image down one.
    void updateImage() {
        // Copy each row from the original image to itself, one row lower.  start from the bottom.
        for (int y=memoryLength-1;y>0;--y) {
            for (int x=0;x<image.getWidth();++x) {
                image.setRGB(x,y,image.getRGB(x,y-1));
            }
        }
    }
}
