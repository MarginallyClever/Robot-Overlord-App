package com.marginallyclever.robotoverlord.parameters;

/**
 * A {@link StringParameter} that can only be set to the uniqueID of an {@link com.marginallyclever.robotoverlord.Entity}.
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
}
