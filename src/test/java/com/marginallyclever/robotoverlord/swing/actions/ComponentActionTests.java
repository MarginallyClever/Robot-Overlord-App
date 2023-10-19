package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

/**
 * Test the {@link ComponentCopyAction} and {@link ComponentPasteAction} classes.
 * Do not run in a headless environment.
 * @author Dan Royer
 * @since 2.4.0
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class ComponentActionTests {
    /**
     * Test copying a {@link Component} from one {@link Entity} and pasting to another.
     */
    @Test
    public void testComponentCopyAction() {
        // translator is needed by actions.
        Translator.start();
        UndoSystem.start();

        Entity entityA = new Entity();
        Entity entityB = new Entity();
        entityA.addComponent(new PoseComponent());

        // select the first entity and copy the first component
        Clipboard.setSelectedEntity(entityA);
        ComponentCopyAction copyAction = new ComponentCopyAction();
        copyAction.setComponent(entityA.getComponent(0));
        copyAction.actionPerformed(null);

        // select the second entity and paste.
        Clipboard.setSelectedEntity(entityB);
        ComponentPasteAction pasteAction = new ComponentPasteAction();
        pasteAction.actionPerformed(null);

        // confirm paste was ok.
        Assertions.assertNotEquals(0,entityB.getComponentCount());
        Assertions.assertNotEquals(null,entityB.getComponent(0));
        Assertions.assertEquals(entityA.getComponent(0).toString(), entityB.getComponent(0).toString());

        // test undo
        Assertions.assertTrue(UndoSystem.getCommandUndo().isEnabled());
        UndoSystem.getCommandUndo().actionPerformed(null);
        Assertions.assertEquals(1,entityB.getComponentCount());

        // test redo
        Assertions.assertTrue(UndoSystem.getCommandRedo().isEnabled());
        UndoSystem.getCommandRedo().actionPerformed(null);
        Assertions.assertEquals(1,entityB.getComponentCount());
    }
}
