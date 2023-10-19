package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class RobotLibraryPanelTest {
    private FrameFixture window;

    @BeforeEach
    public void setUp() {
        Robot robot = BasicRobot.robotWithNewAwtHierarchy();
        RobotLibraryPanel panel = new RobotLibraryPanel();
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.pack();
        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame to test
    }

    @AfterEach
    public void tearDown() {
        window.cleanUp();
    }

    @Test
    public void shouldDisplayMultiVersionPropertiesPanels() {
        window.panel("multiVersionPropertiesPanel_0").requireVisible();
        //window.panel("multiVersionPropertiesPanel_1").requireVisible();
    }
}
