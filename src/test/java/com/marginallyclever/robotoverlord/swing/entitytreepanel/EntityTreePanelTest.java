package com.marginallyclever.robotoverlord.swing.entitytreepanel;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.entity.EntityManagerTest;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;


@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class EntityTreePanelTest {
    private FrameFixture window;
    private EntityManager entityManager;
    private EntityTreePanel panel;

    @BeforeEach
    public void setUp() {
        Translator.start();
        UndoSystem.start();
        Robot robot = BasicRobot.robotWithNewAwtHierarchy();
        entityManager = new EntityManager();
        panel = new EntityTreePanel(entityManager);

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
    public void testMovingEntities() {
        EntityManagerTest emt = new EntityManagerTest();
        emt.moveEntityWithEntityManager(entityManager);
        window.panel("EntityTreePanel").requireVisible();
    }
}
