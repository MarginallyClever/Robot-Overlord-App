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
        Dimension size = WebcamResolution.QVGA.getSize();
        webcam.setViewSize(size);

        panel = new WebcamPanel(webcam, size, false);
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
