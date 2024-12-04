package com.marginallyclever.ro3.apps.viewport.viewporttool.move;

import javax.swing.*;
import java.awt.*;

public class TranslateToolPanel extends JPanel {
    private final TranslateToolMulti tool;

    public TranslateToolPanel() {
        this(new TranslateToolMulti());
    }

    public TranslateToolPanel(TranslateToolMulti tool) {
        super(new GridBagLayout());
        setName("Translate");
        this.tool = tool;
    }
}
