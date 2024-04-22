package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.PanelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Manages the ODE4J physics world, space, and contact handling.  There must be exactly one of these in the scene
 * for physics to work.
 */
public class ODEWorldSpacePanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ODEWorldSpacePanel.class);

    public ODEWorldSpacePanel() {
        this(new ODEWorldSpace());
    }

    public ODEWorldSpacePanel(ODEWorldSpace worldSpace) {
        super(new GridLayout(0,2));
        this.setName(ODEWorldSpace.class.getSimpleName());

        // toggle button to pause/unpause the simulation
        JButton pauseButton = new JButton();
        pauseButton.addActionListener(e -> {
            worldSpace.setPaused(!worldSpace.isPaused());
            updatePauseButton(pauseButton,worldSpace);
        });
        updatePauseButton(pauseButton,worldSpace);

        PanelHelper.addLabelAndComponent(this,"Active",pauseButton);
    }

    private void updatePauseButton(JButton pauseButton, ODEWorldSpace worldSpace) {
        if (worldSpace.isPaused()) {
            pauseButton.setToolTipText("Play");
            pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-play-16.png"))));
        } else {
            pauseButton.setToolTipText("Pause");
            pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-pause-16.png"))));
        }
    }
}
