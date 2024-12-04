package com.marginallyclever.ro3.node.nodes.behavior;

import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * <p>{@link BehaviorTreeRunner} is a node that runs a behavior tree.</p>
 * <p>version 1, behave like a {@link Sequence}.</p>
 */
public class BehaviorTreeRunner extends Node {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorTreeRunner.class);

    private boolean isRunning = false;

    public BehaviorTreeRunner() {
        this("BehaviorTreeRunner");
    }

    public BehaviorTreeRunner(String name) {
        super(name);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(isRunning) {
            //logger.debug("running");
            var child = findFirstChild(Behavior.class);
            if(child==null) {
                logger.warn("BehaviorTreeRunner has no children!");
                setRunning(false);
                return;
            }
            Behavior.Status status = child.tick();
            if(status != Behavior.Status.RUNNING) {
                logger.warn("BehaviorTreeRunner has ended with {}",status == Behavior.Status.SUCCESS ? "SUCCESS" : "FAILURE");
                setRunning(false);
            }
        }
    }

    public void resetAll() {
        for(Node n : getChildren()) {
            if(n instanceof Behavior b) b.reset();
        }
    }

    @Override
    public Icon getIcon() {
        return super.getIcon();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        logger.debug("setRunning({})",running);
        isRunning = running;
        fireActionEvent(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,isRunning? "start" : "stop"));
    }

    private void fireActionEvent(ActionEvent evt) {
        for(ActionListener listener : listeners.getListeners(ActionListener.class)) {
            listener.actionPerformed(evt);
        }
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(ActionListener.class,listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(ActionListener.class,listener);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new BehaviorTreeRunnerPanel(this));
        super.getComponents(list);
    }
}
