package com.marginallyclever.robotoverlord.renderpanel;

import com.marginallyclever.robotoverlord.entity.Entity;

import javax.swing.*;
import java.util.List;

public interface RenderPanel {
    JPanel getPanel();

    void startAnimationSystem();

    void stopAnimationSystem();

    void updateSubjects(List<Entity> list);

    void setUpdateCallback(UpdateCallback updateCallback);
}
