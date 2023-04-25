package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

import com.marginallyclever.robotoverlord.Component;

import javax.swing.*;

/**
 * A pair of a {@link Component} and a {@link JComponent} with the UI for that {@link Component}.
 * This is needed so that the {@link JComponent} can be rendered in a {@link JList} and the
 * {@link ComponentManagerPanel} can find the associated {@link Component} when the user selects.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
class ComponentPanelPair {
    public Component component;
    public JComponent panel;

    public ComponentPanelPair(Component component, JComponent panel) {
        this.component = component;
        this.panel = panel;
    }
}
