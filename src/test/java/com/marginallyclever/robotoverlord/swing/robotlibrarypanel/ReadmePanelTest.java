package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class ReadmePanelTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = new URL("https://github.com/marginallyclever/Robot-Overlord-App");
                ReadmePanel readmePanel = new ReadmePanel(url);

                JFrame frame = new JFrame("README.md Viewer");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(readmePanel);
                frame.setSize(800, 600);
                frame.setVisible(true);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.err.println("Invalid URL");
            }
        });
    }
}
