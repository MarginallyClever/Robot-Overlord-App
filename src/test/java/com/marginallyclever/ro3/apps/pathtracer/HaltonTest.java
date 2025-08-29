package com.marginallyclever.ro3.apps.pathtracer;

public class HaltonTest {
    public static void main(String[] args) {
        // make a BufferedImage, 100x100
        var image = new java.awt.image.BufferedImage(250, 250, java.awt.image.BufferedImage.TYPE_INT_RGB);
        // fill it with halton samples
        var halton = new HaltonWithMemory();
        halton.resetMemory(0xDEADBEEFL);
        for(int i=0;i<image.getWidth()*image.getHeight();i++) {
            int x = (int)(image.getWidth()  * halton.nextDouble(PathTracer.CHANNEL_VIEWPORT_U));
            int y = (int)(image.getHeight() * halton.nextDouble(PathTracer.CHANNEL_VIEWPORT_V));
            //int c = image.getRGB(x,y);
            image.setRGB(x,y,0xFFFFFF);
        }
        // save it to png.
        try {
            javax.imageio.ImageIO.write(image, "png", new java.io.File("halton.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
