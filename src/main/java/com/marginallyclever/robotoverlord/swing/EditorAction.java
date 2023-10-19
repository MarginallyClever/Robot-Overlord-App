package com.marginallyclever.robotoverlord.swing;

import javax.swing.*;

/**
 * {@link AbstractAction}s implement this interface to update their own
 * {@link AbstractAction#setEnabled(boolean)} status.
 * @author Dan Royer
 * @since 2022-02-23
 */
public interface EditorAction {
    /**
     * Called when the editor believes it is time to confirm enable status.
     */
    void updateEnableStatus();
}
