package com.marginallyclever.ro3;

import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.apps.nodedetailview.NodeDetailView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * <p>Display a card layout of all the {@link JPanel}s in the project.  This would be handy for translators to see all
 * the panels in one place.</p>
 * <p>{@link Reflections} can only find classes that extend {@link JPanel}.  Therefore in a second pass create one
 * instance of every {@link Node} and add the components from {@link Node#getComponents(List)}.</p>
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class TestAllPanels {
    private static final Logger logger = LoggerFactory.getLogger(TestAllPanels.class);

    @Test
    public void testAllPanels() {
        Registry.start();

        // print the classpath
        System.out.println("Classpath:");
        for(var path : ClasspathHelper.forJavaClassPath()) {
            System.out.println("  "+path);
        }

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
            logger.error("Failed to set look and feel.");
        }

        var frame = AllPanels.showAllPanels();

        // version 1, pause for 5 seconds
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        frame.dispose();
    }
}
