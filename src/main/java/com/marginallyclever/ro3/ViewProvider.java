package com.marginallyclever.ro3;

import javax.swing.*;

/**
 * Classes implementing this interface are saying that they can provide a Swing component that can be used to view
 * or manipulate the given object.
 */
public interface ViewProvider<T> {
    /**
     * @param object The object to be viewed or manipulated.
     * @return A Swing component that can be used to view or manipulate the given object.
     */
    JComponent getView(T object);
}
