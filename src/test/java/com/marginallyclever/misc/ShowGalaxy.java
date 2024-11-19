package com.marginallyclever.misc;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.util.Random;

public class ShowGalaxy extends JPanel {
    // initial window width
    public static final int WIDTH = 800;
    // initial window height
    public static final int HEIGHT = 600;
    // desired frames per second of animation
    public static final int FPS = 30;
    // gravitational constant
    public static double G = 6.67430e-11;
    // The multiplier for the initial velocity of the stars.
    public double mul = 0.04;
    // The maximum mass of a star in the galaxy.
    public double maxMass = 3000;

    public boolean isPaused = false;
    public BufferedImage starImage = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);


    static public class Star {
        public final Vector2d pos = new Vector2d();
        public final Vector2d vel = new Vector2d();
        public final Vector2d acc = new Vector2d();
        public double mass;
        public Color c;
    }

    static public class GUI extends JPanel {
        ShowGalaxy galaxy;
        public GUI(ShowGalaxy gal) {
            super(new GridBagLayout());
            galaxy = gal;

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx=0;
            c.gridy=0;
            c.anchor = GridBagConstraints.NORTHWEST;

            JButton reset = new JButton("Reset");
            reset.addActionListener(e->{
                galaxy.reset();
            });
            add(new JLabel("Reset"),c);
            c.gridx++;
            add(reset,c);
            c.gridx=0;
            c.gridy++;

            JButton pause = new JButton("Yes");
            pause.addActionListener((e)->{
                galaxy.isPaused = !galaxy.isPaused;
                pause.setText(galaxy.isPaused?"No":"Yes");
            });
            add(new JLabel("Run"),c);
            c.gridx++;
            add(pause,c);
            c.gridx=0;
            c.gridy++;

            JFormattedTextField g = addNumberField("G",G);
            g.addPropertyChangeListener("value",(e)->{
                G = ((Number)g.getValue()).doubleValue();
            });
            add(new JLabel("Gravitational Constant"),c);
            c.gridx++;
            add(g,c);
            c.gridx=0;
            c.gridy++;
        }
        private JFormattedTextField addNumberField(String label, double value) {
            var formatter = new DecimalFormat("0.###E0");
            JFormattedTextField field = new JFormattedTextField(formatter);
            field.setValue(value);
            field.setToolTipText(label);
            field.setColumns(15);
            field.setMinimumSize(new Dimension(0,20));
            return field;
        }
    }


    List<Star> stars = new ArrayList<>();

    public ShowGalaxy() {
        super();
        createStarImage();
        reset();
    }

    private void createStarImage() {
        // fill star image with a gaussian circle, white in the center and transparent at the edges.
        Graphics2D g = starImage.createGraphics();
        int w = starImage.getWidth();
        int h = starImage.getHeight();
        for(int y=0;y<h;++y) {
            for(int x=0;x<w;++x) {
                double dx = x-w/2;
                double dy = y-h/2;
                double r = Math.sqrt(dx*dx+dy*dy);
                double v = Math.exp(-r*r/100);
                g.setColor(new Color(1,1,1,(float)v));
                g.fillRect(x,y,1,1);
            }
        }
        g.dispose();
    }

    public void reset() {
        stars.clear();

        Random random = new Random();
        int min = Math.min(WIDTH,HEIGHT)/5;
        // add 10k stars in the galaxy.
        for(int i=0;i<2000;++i) {
            Star s = new Star();
            // the distribution from the center should follow a bell curve.
            double radius = random.nextGaussian()*min;
            // the angle should be random
            double angle = random.nextDouble()*Math.PI*2;

            s.pos.set(
                    Math.cos(angle)*radius,
                    Math.sin(angle)*radius);
            // the velocity should be higher in the center and lower at the edges.

            s.vel.set(s.pos.y*mul,-s.pos.x*mul);

            var v = min*4-radius;
            s.mass = Math.random()*(maxMass-v)+v;
            stars.add(s);
            // color is related to mass.  heavier stars are red and light stars are blue.
            int r = (int)(Math.min(255,Math.max(0,s.mass/maxMass*255)));
            s.c = new Color(
                    r,
                    r,
                    (int)(Math.min(255,Math.max(0,(maxMass-s.mass)/maxMass*255))));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(!isPaused) {
            updateEverything();
        }

        drawEverything(g);
    }

    private void updateEverything() {
        // update the star accelerations in parallel
        stars.stream().parallel().forEach(s -> {
            Vector2d delta = new Vector2d();
            s.acc.set(0, 0);
            for (Star other : stars) {
                if (s != other) {
                    delta.sub(other.pos,s.pos);
                    // F = G * m1*m2 / (r*r)
                    double r = delta.lengthSquared();
                    if (r > 0) {
                        delta.normalize();
                        delta.scale(G * s.mass * other.mass / r);
                        s.acc.add(delta);
                    }
                }
            }
        });

        double dt = 1.0/FPS;
        // move stars in parallel
        stars.stream().parallel().forEach(s -> {
            // acc is now force.  F=ma -> a=F/m
            s.vel.scaleAdd(dt / s.mass, s.acc, s.vel);
            s.pos.scaleAdd(dt, s.vel, s.pos);
        });
    }

    private void drawEverything(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        // Smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // clear screen
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.translate( getWidth()/2, getHeight()/2);

        // draw stars
        for(Star s : stars) {
            g2d.setColor(s.c);
            int r = (int)Math.ceil(s.mass*15/maxMass);
            int r2 = r*2;
            //g2d.fillOval((int)s.pos.x-r, (int)s.pos.y-r, r2, r2);
            g2d.drawImage(starImage,(int)s.pos.x-r, (int)s.pos.y-r, r2, r2,null);
        }
        g2d.dispose();
    }

    public static void main(String[] args) {
        // create a new JFrame
        var frame = new JFrame("Show Galaxy");
        // set the size of the JFrame
        frame.setSize(800, 600);
        // add the JPanel to the JFrame
        var galaxy = new ShowGalaxy();

        JPanel container = new JPanel(new BorderLayout());
        container.add(galaxy, BorderLayout.CENTER);
        container.add(new GUI(galaxy), BorderLayout.EAST);
        frame.add(container, BorderLayout.CENTER);
        // set the JFrame to exit the application when it is closed
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        // make the JFrame visible
        frame.setVisible(true);

        // Set up the Timer to repaint at 30 FPS
        int delay = 1000 / FPS; // 30 frames per second -> 1000 ms / 30
        Timer timer = new Timer(delay, e -> {
                //galaxy.invalidate();
                //galaxy.revalidate();
                galaxy.repaint();
        });
        timer.start();
    }
}
