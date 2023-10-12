package com.marginallyclever.robotoverlord.renderpanel;

import com.marginallyclever.robotoverlord.systems.render.SphericalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
                    double pan = e.getX() / (double)getWidth();
                    double tilt = e.getY() / (double)getHeight();

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
        JFrame frame = new JFrame( OpenGLTestPerspective.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        VisuallyTestSphereMap opengl = new VisuallyTestSphereMap();
        frame.setContentPane(opengl);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public VisuallyTestSphereMap() throws IOException {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        image = ImageIO.read(new File("C:\\Users\\aggra\\Desktop\\whiteRoomSphericalProjection2.png"));
        // paint source with image, filling the entire panel.

        remapped = new BufferedImage(1536,256,BufferedImage.TYPE_INT_RGB);
        makeMap();

        // display a 256 tall and (256*6) wide BufferedImage in the target.
        topPanel.setPreferredSize(new Dimension(1536,768));
        bottomPanel.setPreferredSize(new Dimension(1536,256));  // 1536 = 256*6
        add(topPanel);
        add(bottomPanel);
    }

    private void makeMap() {
        for(int v=0;v<image.getHeight();++v) {
            for(int u=0;u<image.getWidth();++u) {
                double pan = u / (double)image.getWidth();
                double tilt = v / (double)image.getHeight();
                SphericalMap.CubeCoordinate cube = SphericalMap.planeToCube(pan,tilt);
                int cx = (int)(cube.position.x*256) + cube.face*256;
                int cy = (int)(cube.position.y*256);

                cx = Math.max(0,Math.min(cx,remapped.getWidth()-1));
                cy = Math.max(0,Math.min(cy,remapped.getHeight()-1));

                int color = image.getRGB(u,v);
                remapped.setRGB(cx,cy,color);
            }
        }
    }
}
