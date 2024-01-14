package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;
import org.json.JSONObject;

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
    public Status tick() {
        while(current < count) {
            Status result = super.tick();
            if(result == Status.FAILURE) return Status.FAILURE;
            if(result == Status.RUNNING) return Status.RUNNING;
            current++;
        }
        return Status.SUCCESS;
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
        count = from.getInt("count");
    }
}
