package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

import javax.vecmath.Matrix4d;

/**
 * Adjusts this Entity's pose to be relative to the parent's pose.
 */
@ComponentDependency(components={PoseComponent.class})
public class OriginAdjustComponent extends Component {}
