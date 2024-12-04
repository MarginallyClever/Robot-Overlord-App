package com.marginallyclever.ro3.apps.editor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Clear the text area of the {@link EditorPanel}
 */
public class NewAction extends AbstractAction {
    private final EditorPanel editorPanel;

    public NewAction(EditorPanel editorPanel) {
        super();
        this.editorPanel = editorPanel;
        putValue(Action.NAME,"New");
        putValue(SHORT_DESCRIPTION,"Clear the text area.");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-new-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editorPanel.reset();
    }
}
