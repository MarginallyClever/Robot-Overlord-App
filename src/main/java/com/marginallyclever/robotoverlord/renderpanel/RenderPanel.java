package com.marginallyclever.robotoverlord.renderpanel;

import com.marginallyclever.robotoverlord.Entity;

import javax.swing.*;
import java.util.List;

public interface RenderPanel {
    JPanel getPanel();

    void startAnimationSystem();

    void stopAnimationSystem();

    void updateSubjects(List<Entity> list);
}
