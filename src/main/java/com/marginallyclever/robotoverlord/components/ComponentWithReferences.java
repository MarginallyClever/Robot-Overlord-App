package com.marginallyclever.robotoverlord.components;

import java.util.Map;

/**
 * This interface is used to mark components that have reference to an entity.
 *
 */
@Deprecated
public interface ComponentWithReferences {
    void updateReferences(Map<String,String> oldToNewIDMap);
}
