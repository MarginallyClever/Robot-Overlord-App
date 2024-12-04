package com.marginallyclever.ro3.apps.viewport.viewporttool;

import java.util.EventListener;

/**
 * Listens for changes to the active {@link ViewportTool}
 */
public interface ViewportToolChangeListener extends EventListener {
    /**
     * Called when the active {@link ViewportTool} changes
     * @param tool the new active {@link ViewportTool}
     */
    void onViewportToolChange(ViewportTool tool);
}
