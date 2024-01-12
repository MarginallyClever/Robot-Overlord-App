package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.convenience.helpers.JSONHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.UndoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.FlavorEvent;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

/**
 * {@link PasteNode} is an action that pastes the selected node(s) from the clipboard.
 */
public class PasteNode extends AbstractAction {
    private final Logger logger = LoggerFactory.getLogger(PasteNode.class);
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public PasteNode() {
        super();
        putValue(Action.NAME,"Paste");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-paste-16.png"))));
        putValue(SHORT_DESCRIPTION,"Paste the selected node(s).");

        clipboard.addFlavorListener(this::clipboardChanged);
        setEnabled(false);
    }

    private void clipboardChanged(FlavorEvent flavorEvent) {
        // if the clipboard has changed, update the menu item
        if(flavorEvent.getSource()==clipboard) {
            if(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() == null) {
                setEnabled(false);
                return;
            }
            try {
                setEnabled(clipboard.isDataFlavorAvailable(JSONHelper.JSON_FLAVOR));
            } catch (IllegalStateException ignored) {
                // if this clipboard is currently unavailable
            }
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var selection = new ArrayList<>(Registry.selection.getList());
        UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.PasteNode(selection));
    }
}
