package com.marginallyclever.convenience.swing;

public class DialFitTest {
    // put a dial in a frame that can be resized by the user.
    // this is to confirm visually that the dial will resize with the frame and stay within the frame bounds.
    public static void main(String[] args) {
        Dial dial = new Dial();
        dial.setOpaque(true);
        dial.setBackground(java.awt.Color.WHITE);
        javax.swing.JFrame frame = new javax.swing.JFrame();
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(dial);
        frame.pack();
        frame.setVisible(true);
    }
}
