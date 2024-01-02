package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.convenience.helpers.JSONHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

public class PasteNode extends AbstractAction {
    private final Logger logger = LoggerFactory.getLogger(PasteNode.class);

    public PasteNode() {
        super();
        putValue(Action.NAME,"Paste");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-paste-16.png"))));
        putValue(SHORT_DESCRIPTION,"Paste the selected node(s).");

        Registry.clipboard.addFlavorListener(this::clipboardChanged);
        setEnabled(false);
    }

    private void clipboardChanged(FlavorEvent flavorEvent) {
        // if the clipboard has changed, update the menu item
        if(flavorEvent.getSource()==Registry.clipboard) {
            if(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() == null) {
                setEnabled(false);
                return;
            }
            try {
                setEnabled(Registry.clipboard.isDataFlavorAvailable(JSONHelper.JSON_FLAVOR));
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
