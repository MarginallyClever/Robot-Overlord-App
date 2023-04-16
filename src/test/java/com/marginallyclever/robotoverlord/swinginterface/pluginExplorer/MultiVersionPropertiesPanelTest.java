package com.marginallyclever.robotoverlord.swinginterface.pluginExplorer;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.jupiter.api.*;

import javax.swing.*;

public class MultiVersionPropertiesPanelTest {

    private FrameFixture window;
    private Robot robot;

    @BeforeEach
    public void setUp() {
        robot = BasicRobot.robotWithNewAwtHierarchy();
        MultiVersionPropertiesPanel panel = new MultiVersionPropertiesPanel("https://github.com/MarginallyClever/Sixi-"); // Replace OWNER and REPO with the GitHub repository you want to test
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.pack();
        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame to test
    }

    @Test
    public void shouldDisplayTagsAndProperties() {
        window.panel("pageEnd").requireVisible();
        window.panel("pageEnd").comboBox("tagComboBox").requireVisible();
        window.panel("propertiesPanel").requireVisible();
    }

    @AfterEach
    public void tearDown() {
        window.cleanUp();
    }
}