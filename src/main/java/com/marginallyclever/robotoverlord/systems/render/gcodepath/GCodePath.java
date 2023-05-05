package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import java.util.LinkedList;
import java.util.List;

/**
 * A path made of GCode commands.
 *
 * @author Dan Royer
 * @since 2.5.6
 */
public class GCodePath {
    private String sourceName;
    private final LinkedList<GCodePathElement> elements = new LinkedList<>();
    private boolean dirty = false;

    public GCodePath() {}

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void clear() {
        elements.clear();
    }

    public void setDirty(boolean b) {
        dirty = b;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void addElement(GCodePathElement element) {
        elements.add(element);
    }

    public List<GCodePathElement> getElements() {
        return elements;
    }
}
