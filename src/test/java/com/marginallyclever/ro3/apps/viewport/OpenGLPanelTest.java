package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.ro3.Registry;

public class OpenGLPanelTest {
    public static void main(String[] args) {
        Registry.start();

        OpenGLPanel panel = new OpenGLPanel();

        // create and display a frame
        javax.swing.JFrame frame = new javax.swing.JFrame("OpenGLPanel Test");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
