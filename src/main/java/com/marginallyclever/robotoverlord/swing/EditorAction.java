package com.marginallyclever.robotoverlord.swing;

import javax.swing.*;

/**
 * {@link AbstractAction}s implement this interface to update their own
 * {@link AbstractAction#setEnabled(boolean)} status.
 */
@Deprecated
public interface EditorAction {
    /**
     * Called when the editorpanel believes it is time to confirm enable status.
     */
    void updateEnableStatus();
}
