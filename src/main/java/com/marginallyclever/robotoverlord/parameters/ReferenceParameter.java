package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.entity.Entity;

import java.util.Map;

/**
 * A {@link StringParameter} that can only be set to the uniqueID of an {@link Entity}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ReferenceParameter extends StringParameter {
    public ReferenceParameter(String name, String path) {
        super(name, path);
    }

    public ReferenceParameter(String name) {
        this(name, null);
    }

    public ReferenceParameter() {
        this("", null);
    }

    public void updateReferences(Map<String, String> oldToNewIDMap) {
        String id = get();
        if(oldToNewIDMap.containsKey(id)) {
            set(oldToNewIDMap.get(id));
        }
    }

    public void set(Entity entity) {
        set(entity.getUniqueID());
    }
}
