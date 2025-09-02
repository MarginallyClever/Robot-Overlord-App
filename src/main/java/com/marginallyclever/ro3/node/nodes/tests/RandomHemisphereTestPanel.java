package com.marginallyclever.ro3.node.nodes.tests;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Panel for editing a {@link Sphere}.</p>
 */
public class RandomHemisphereTestPanel extends JPanel {
    public static final String [] options = new String[]{
            "Uniform Sphere",
            "Uniform Hemisphere",
            "Cosine-weighted Hemisphere",
    };

    public RandomHemisphereTestPanel() {
        this(new RandomHemisphereTest());
        setName("RandomHemisphereTestPanel");
    }

    public RandomHemisphereTestPanel(RandomHemisphereTest randomHemisphereTest) {
        super(new GridLayout(0, 2));
        setName(Sphere.class.getSimpleName());

        JComboBox<String> typeField = PanelHelper.createComboBox(options, randomHemisphereTest.type, randomHemisphereTest::setType);
        PanelHelper.addLabelAndComponent(this, "Type", typeField);
    }
}