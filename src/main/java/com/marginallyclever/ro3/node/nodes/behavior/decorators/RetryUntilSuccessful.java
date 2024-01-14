package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;
import org.json.JSONObject;

/**
 * <p>{@link Repeat} is a {@link Decorator} that repeats its child a fixed number of times.</p>
 * <p>Tick the child up to {@link #count} times, as long as the child returns {@link Status#FAILURE}.  Return
 * {@link Status#FAILURE} after the {@link #count} attempts in the case that the child always returned
 * {@link Status#FAILURE}.</p>
 * <p>Interrupt the loop if the child returns {@link Status#SUCCESS} and, in that case, return {@link Status#SUCCESS}
 * too.</p>
 * <p>If the child returns {@link Status#RUNNING}, this node returns {@link Status#RUNNING} too and the attempts will
 * continue without incrementing on the next tick of the {@link RetryUntilSuccessful} node.</p>
 */
public class RetryUntilSuccessful extends Decorator {
    private int count = 1;
    private int current = 0;

    public RetryUntilSuccessful() {
        this("RetryUntilSuccessful");
    }

    public RetryUntilSuccessful(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        Status result = Status.FAILURE;
        while(current < count) {
            result = super.tick();
            if(result != Status.FAILURE) break;
            current++;
        }
        return result;
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
