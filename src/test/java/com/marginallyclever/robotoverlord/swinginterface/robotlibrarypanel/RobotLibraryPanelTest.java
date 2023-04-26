package com.marginallyclever.robotoverlord.swinginterface.robotlibrarypanel;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class RobotLibraryPanelTest {

    private FrameFixture window;
    private Robot robot;

    @BeforeEach
    public void setUp() {
        robot = BasicRobot.robotWithNewAwtHierarchy();

        List<String> repositories = Arrays.asList(
                "https://github.com/MarginallyClever/Sixi-",
                "https://github.com/marginallyclever/AR4",
                "https://github.com/marginallyclever/Mecademic-Meca500"
        );

        RobotLibraryPanel panel = new RobotLibraryPanel(repositories);
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.pack();
        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame to test
    }

    @Test
    public void shouldDisplayMultiVersionPropertiesPanels() {
        window.panel("multiVersionPropertiesPanel_0").requireVisible();
        //window.panel("multiVersionPropertiesPanel_1").requireVisible();
    }

    @AfterEach
    public void tearDown() {
        window.cleanUp();
    }
}
