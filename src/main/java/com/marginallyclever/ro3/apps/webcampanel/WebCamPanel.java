package com.marginallyclever.ro3.apps.webcampanel;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.marginallyclever.ro3.apps.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * {@link WebCamPanel} uses {@link Webcam} to display the default USB web camera.
 */
public class WebCamPanel extends App {
    private static final Logger logger = LoggerFactory.getLogger(WebCamPanel.class);
    private Webcam webcam;
    private WebcamPanel panel;
    private JButton snapshotButton;

    public WebCamPanel() {
        super(new BorderLayout());
        setName("webcam");
        addToolBar();
    }

    private void addToolBar() {
        snapshotButton = new JButton(new AbstractAction() {
            {
                putValue(NAME, "Snapshot");
                putValue(SHORT_DESCRIPTION, "Snapshot");
                putValue(SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-screenshot-16.png"))));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                takeSnapshot();
            }
        });

        var toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(snapshotButton);
        add(toolBar, BorderLayout.NORTH);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        logger.debug("webcam addNotify");
        try {
            Webcam.getWebcams().forEach(w->logger.debug("Checking {} {}",w.getName(),w.getDevice().isOpen()));

            webcam = Webcam.getDefault(1000);
            if(webcam==null) throw new TimeoutException("Soft timeout.");
            var list = webcam.getViewSizes();
            webcam.setViewSize(list[list.length - 1]);  // probably the biggest

            panel = new WebcamPanel(webcam, false);
            panel.setDrawMode(WebcamPanel.DrawMode.FIT);  // fit, fill, or none
            panel.setFPSDisplayed(true);
            add(panel, BorderLayout.CENTER);
            panel.start();
            snapshotButton.setEnabled(true);
        } catch (TimeoutException e) {
            logger.error("TimeoutException",e);
            var label=new JLabel("No webcam found.");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
            snapshotButton.setEnabled(false);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if(panel!=null) {
            panel.stop();
            remove(panel);
            panel = null;
        }
        if(webcam!=null) {
            webcam.close();
            webcam=null;
        }
        snapshotButton.setEnabled(false);
    }

    public void takeSnapshot() {
        if(panel==null) return;

        BufferedImage img = panel.getWebcam().getImage();
        if(img==null) return;
        logger.info("Snapshot {}x{}",img.getWidth(),img.getHeight());
        logger.error("Not implemented yet.");
    }
}
