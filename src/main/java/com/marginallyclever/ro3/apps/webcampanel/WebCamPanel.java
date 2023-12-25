package com.marginallyclever.ro3.apps.webcampanel;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.swing.*;
import java.awt.*;

public class WebCamPanel extends JPanel {
    private final Webcam webcam;
    private final WebcamPanel panel;

    public WebCamPanel() {
        super(new BorderLayout());
        setName("webcam");

        webcam = Webcam.getDefault();
        var list = webcam.getViewSizes();
        webcam.setViewSize(list[list.length-1]);  // probably the biggest

        panel = new WebcamPanel(webcam,false);
        panel.setDrawMode(WebcamPanel.DrawMode.FIT);  // fit, fill, or none
        panel.setFPSDisplayed(true);
        add(panel, BorderLayout.CENTER);
        panel.start();
        panel.pause();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        panel.resume();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        panel.pause();
    }
}
