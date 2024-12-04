package com.marginallyclever.ro3.apps.viewport.viewporttool;

import com.marginallyclever.ro3.apps.viewport.Viewport;

import javax.swing.*;

/**
 * Displays the settings for the currently active {@link ViewportTool}
 */
public class ViewportToolPanel extends JPanel {
    private final Viewport viewport;

    public ViewportToolPanel() {
        this(new Viewport());
    }
    public ViewportToolPanel(Viewport viewport) {
        super();
        setName("Tool");
        this.viewport = viewport;

        viewport.add
    }
}
