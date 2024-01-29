package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Behavior;
import com.marginallyclever.ro3.node.nodes.behavior.Decorator;
import com.marginallyclever.ro3.node.nodes.behavior.actions.LimbMoveToTargetPanel;
import org.json.JSONObject;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;

/**
 * <p>{@link Repeat} is a {@link Decorator} that repeats its child a fixed number of times.</p>
 * <p>Tick the child up to {@link #count} times (within one of its tick), as long as the child returns SUCCESS.
 * Return SUCCESS after the N repetitions in the case that the child always returned SUCCESS.</p>
 * <p>Interrupt the loop if the child returns FAILURE and, in that case, return FAILURE too.</p>
 * <p>If the child returns RUNNING, this node returns RUNNING too and the repetitions will continue without
 * incrementing on the next tick of the Repeat node.</p>
 */
public class Repeat extends Decorator {
    private int count = 1;
    private int current = 0;

    public Repeat() {
        this("Repeat");
    }

    public Repeat(String name) {
        super(name);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
    }

    @Override
    public Status tick() {
        while(current < count) {
            Status result = super.tick();
            if(result == Status.FAILURE) return Status.FAILURE;
            if(result == Status.RUNNING) return Status.RUNNING;
            resetChildren();
            setCurrent(getCurrent()+1);
        }
        return Status.SUCCESS;
    }

    private void resetChildren() {
        for(var child : getChildren()) {
            if(child instanceof Behavior b) b.reset();
        }
    }

    public void reset() {
        super.reset();
        setCurrent(0);
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("count", count);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("count")) count = from.getInt("count");
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource(
                "/com/marginallyclever/ro3/node/nodes/behavior/decorators/icons8-repeat-16.png")));
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new RepeatPanel(this));
        super.getComponents(list);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;

        firePropertyChange(new PropertyChangeEvent(this,"count",null,count));
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
        firePropertyChange(new PropertyChangeEvent(this,"current",null,current));
    }

    private void firePropertyChange(PropertyChangeEvent event) {
        for(PropertyChangeListener listener : listeners.getListeners(PropertyChangeListener.class)) {
            listener.propertyChange(event);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(PropertyChangeListener.class,listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(PropertyChangeListener.class,listener);
    }
}
