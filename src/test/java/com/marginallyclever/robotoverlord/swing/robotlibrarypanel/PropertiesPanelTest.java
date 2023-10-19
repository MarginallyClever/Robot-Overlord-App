package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class PropertiesPanelTest {
    public static void main(String[] args) {
        Map<String, String> properties = new HashMap<>();
        properties.put("name", "My Robot");
        properties.put("version", "1.2.0");
        properties.put("author", "John Doe");
        properties.put("maintainer", "Jane Doe");
        properties.put("sentence", "An example robot.");
        properties.put("paragraph", "This is an example of a robot with a longer description.");
        properties.put("image_url", "https://example.com/image.png");
        properties.put("url", "https://github.com/username/repository");

        JFrame frame = new JFrame("Properties Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setContentPane(new PropertiesPanel(properties));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
