package com.marginallyclever.convenience.swing.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for graph data.  Many named maps, each of which contains numbers.
 */
public class GraphModel {
    private final Map<String,GraphLine> lines = new HashMap<>();

    public void addLine(String name,GraphLine line) {
        lines.put(name,line);
    }

    public GraphLine getLine(String name) {
        return lines.get(name);
    }

    public int getLineCount() {
        return lines.size();
    }

    public List<String> getLineNames() {
        return new ArrayList<>(lines.keySet());
    }

    public void clear() {
        lines.clear();
    }

    public void removeLine(String name) {
        lines.remove(name);
    }

    public boolean isEmpty() {
        if(lines.isEmpty()) return true;
        for(GraphLine line : lines.values()) {
            if(!line.isEmpty()) return false;
        }
        return true;
    }

    public GraphLine[] getLines() {
        return lines.values().toArray(new GraphLine[0]);
    }
}
