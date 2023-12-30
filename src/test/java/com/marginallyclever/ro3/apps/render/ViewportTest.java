package com.marginallyclever.ro3.apps.render;

import com.marginallyclever.ro3.Registry;

import java.util.List;

public class ViewportTest {
    public static void main(String[] args) {
        Registry.start();

        Viewport panel = new Viewport();
        List<RenderPass> list = panel.renderPasses.getList();
        for(RenderPass rp : list) {
            rp.setActiveStatus(RenderPass.NEVER);
        }

        // create and display a frame
        javax.swing.JFrame frame = new javax.swing.JFrame("Viewport Test");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
