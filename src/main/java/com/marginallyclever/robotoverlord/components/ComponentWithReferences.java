package com.marginallyclever.robotoverlord.components;

import java.util.Map;

/**
 * This interface is used to mark components that have reference to an entity.
 *
 * @author Dan Royer
 * @since 2.6.0
 */
public interface ComponentWithReferences {
    void updateReferences(Map<String,String> oldToNewIDMap);
}
