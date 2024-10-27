package com.marginallyclever.ro3.apps.donatello;

import com.marginallyclever.ro3.node.Node;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Vector2d;

/**
 * {@link Donatello} uses {@link DonatelloNode} to associate a {@link com.marginallyclever.ro3.node.Node} with its
 * Swing {@link javax.swing.JPanel} and remember the 2D position for graphing.
 */
public class DonatelloNode {
    final Vector2d position = new Vector2d();
    JPanel panel;

    public DonatelloNode(JPanel panel) {
        this.panel = panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }
    
    public JPanel getPanel() {
        return panel;
    }

    public Vector2d getPosition() {
        return position;
    }

    public void setPosition(Vector2d position) {
        this.position.set(position);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("x",position.x);
        json.put("y",position.y);
        return json;
    }

    public void fromJSON(JSONObject json) {
        position.x = json.getDouble("x");
        position.y = json.getDouble("y");
    }
}
