package com.marginallyclever.robotoverlord.renderpanel;

import com.marginallyclever.convenience.SphericalMap;
import com.marginallyclever.convenience.helpers.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Run this to visually test
 */
public class VisuallyTestSphereMap extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(VisuallyTestSphereMap.class);
    private final BufferedImage image;
    private final BufferedImage remapped;

    private final MyTopPanel topPanel = new MyTopPanel();
    private final MyBottomPanel bottomPanel = new MyBottomPanel();

    public class MyBottomPanel extends JPanel {
        int cx, cy;
        public MyBottomPanel() {
            super();

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    super.mouseMoved(e);
                    int face = e.getX() / 256;
                    double x = (double)(e.getX() % 256) / 256;
                    double y = (double)e.getY() / 256;

                    // repaint the target with a crosshair based on the SphericalMap.
                    double [] sphere = SphericalMap.cubeToSphere(face,x,y);
                    double [] uv = SphericalMap.sphereToPlane(sphere);
                    topPanel.cx = (int)(uv[0]*(image.getWidth()-1));
                    topPanel.cy = (int)(uv[1]*(image.getHeight()-1));
                    topPanel.repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(remapped,0,0,null);
            g.setColor(Color.BLUE);
            g.drawLine(cx-10,cy,cx+10,cy);
            g.drawLine(cx,cy-10,cx,cy+10);
        }
    };

    public class MyTopPanel extends JPanel {
        public int cx,cy;

        public MyTopPanel() {
            super();

            // add a mouse motion listener to the source.  remember the mouse position.
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    super.mouseMoved(e);
                    double pan = e.getX() / (double)image.getWidth();
                    double tilt = e.getY() / (double)image.getHeight();

                    // repaint the target with a cross-hair based on the SphericalMap.
                    SphericalMap.CubeCoordinate cube = SphericalMap.planeToCube(pan,tilt);
                    bottomPanel.cx = (int)(cube.position.x*256) + cube.face*256;
                    bottomPanel.cy = (int)(cube.position.y*256);
                    bottomPanel.repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(image,0,0,null);
            g.setColor(Color.BLACK);
            g.drawLine(cx-10,cy,cx+10,cy);
            g.drawLine(cx,cy-10,cx,cy+10);
        }
    }

    public static void main(String[] args) throws IOException {
        // make a frame
        JFrame frame = new JFrame( VisuallyTestSphereMap.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        VisuallyTestSphereMap opengl = new VisuallyTestSphereMap();
        frame.setContentPane(opengl);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public VisuallyTestSphereMap() throws IOException {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        File f = new File(FileHelper.getUserDirectory()+"/src/main/resources/skybox/industrial_sunset_02_puresky_4k.png");

        if(!f.exists()) throw new RuntimeException("File not found: "+f.getAbsolutePath());
        image = ImageIO.read(f);
        // paint source with image, filling the entire panel.

        remapped = new BufferedImage(256*6,256,BufferedImage.TYPE_INT_RGB);
        makeMap();

        // display a 256 tall and (256*6) wide BufferedImage in the target.
        topPanel.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
        bottomPanel.setPreferredSize(new Dimension(256*6,256));  // 1536 = 256*6
        add(topPanel);
        add(bottomPanel);
    }

    /**
     * Remap the image from a sphere to a cube.
     * This is done by sampling the image at the cube's UV coordinates.
     * If the sampling is done from the sphere to the cube some pixels will be missed.
     */
    private void makeMap() {
        for(int face=0;face<6;++face) {
            for(int v=0;v<256;++v) {
                for(int u=0;u<256;++u) {
                    double [] sphere = SphericalMap.cubeToSphere(face,u/256.0,v/256.0);
                    double [] xy = SphericalMap.sphereToPlane(sphere);
                    int cx = (int)(xy[0]*(image.getWidth()-1));
                    int cy = (int)(xy[1]*(image.getHeight()-1));
                    int color = image.getRGB(cx,cy);
                    remapped.setRGB(u+face*256,v,color);
                }
            }
        }
    }
}
