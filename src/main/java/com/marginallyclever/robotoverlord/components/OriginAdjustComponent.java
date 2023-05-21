package com.marginallyclever.robotoverlord.components;

/**
 * Adjusts this Entity's pose to be relative to the parent's pose.
 */
@ComponentDependency(components={PoseComponent.class})
public class OriginAdjustComponent extends Component {}
