package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;
import org.json.JSONObject;

public class Delay extends Decorator {
    private long delay = 0;
    private long current = 0;

    public Delay() {
        this("Delay");
    }

    public Delay(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        if(current < delay) {
            current++;
            return Status.RUNNING;
        }
        current = 0;
        return super.tick();
    }

    public long getDelay() {
        return delay;
    }

    /**
     * Set the number of ticks to wait before ticking the child.
     * @param delay number of ticks to wait.  Must be >= 0.
     * @throws IllegalArgumentException if delay < 0
     */
    public void setDelay(int delay) {
        if(delay<0) throw new IllegalArgumentException("Delay must be >= 0");
        this.delay = delay;
    }

    @Override
    public JSONObject toJSON() {
        var json= super.toJSON();
        json.put("delay",delay);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        delay = from.getLong("delay");
    }
}
