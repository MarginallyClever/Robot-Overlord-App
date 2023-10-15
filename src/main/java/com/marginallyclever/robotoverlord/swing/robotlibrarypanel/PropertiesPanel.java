package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Displays the properties of a robot as provided by a map of properties.
 * @author Dan Royer
 * @since 2.5.0
 */
public class PropertiesPanel extends JPanel {
    public PropertiesPanel(Map<String, String> properties) {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2,2,2,2);

        addPropertyLabel(properties, "name", constraints,"name:");
        constraints.gridy++;
        addPropertyLabel(properties, "author", constraints,"author:");
        constraints.gridy++;
        addPropertyLabel(properties, "maintainer", constraints,"maintainer:");
        constraints.gridy++;
        addPropertyLabel(properties, "sentence", constraints,"");
        constraints.gridy++;
        addPropertyTextArea(properties, "paragraph", constraints,"");
        constraints.gridy++;

        String urlString = properties.get("url");
        if (urlString != null) {
            JLabel urlLabel = new JLabel("<html><a href=''>Read more</a></html>");
            urlLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            urlLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(urlString));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
                }
            });
            add(new JLabel(), constraints);
            constraints.gridx++;
            add(urlLabel, constraints);
            constraints.gridx--;
        }

        String imgURLString = properties.get("image_url");
        if (imgURLString != null && !imgURLString.trim().isEmpty()) {
            try {
                BufferedImage myPicture = ImageIO.read(new File(imgURLString));
                JLabel image = new JLabel(new ImageIcon(myPicture));
                constraints.gridheight = constraints.gridy;
                constraints.gridy = 0;
                constraints.gridx++;
                constraints.weightx = 1;
                add(image, constraints);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Show a key/value pair as a {@link JLabel} if the key exists in the map.
     * @param properties the map of properties
     * @param key the key to look for
     * @param constraints the layout constraints
     * @param label the label to show
     */
    private void addPropertyLabel(Map<String, String> properties, String key, GridBagConstraints constraints,String label) {
        String value = properties.get(key);
        if (value != null) {
            JLabel keyLabel = new JLabel(label);
            JLabel  valueLabel = new JLabel(value);
            add(keyLabel, constraints);
            constraints.weightx=1;
            constraints.gridx++;
            add(valueLabel, constraints);
            constraints.weightx=0;
            constraints.gridx--;
        }
    }

    /**
     * Show a key/value pair as a {@link JTextArea} if the key exists in the map.
     * @param properties the map of properties
     * @param key the key to look for
     * @param constraints the layout constraints
     * @param label the label to show
     */
    private void addPropertyTextArea(Map<String, String> properties, String key, GridBagConstraints constraints,String label) {
        String value = properties.get(key);
        if (value != null) {
            JLabel keyLabel = new JLabel(label);
            JTextArea  valueLabel = new JTextArea(value);
            valueLabel.setLineWrap(true);
            valueLabel.setWrapStyleWord(true);
            valueLabel.setColumns(40);
            valueLabel.setEditable(false);
            valueLabel.setOpaque(false);
            add(keyLabel, constraints);
            constraints.gridx++;
            add(valueLabel, constraints);
            constraints.gridx--;
        }
    }
}
