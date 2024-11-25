package com.marginallyclever.ro3.apps.viewport.viewporttools.move;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.OpenGLPanel;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.awt.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class TranslateToolOneAxisTest {
    @BeforeAll
    public static void setUp() {
        Registry.start();
    }

    @Test
    public void constructor() {
        var pose = new Pose("test");
        Registry.getScene().addChild(pose);

        var tool = new TranslateToolOneAxis(new ColorRGB(Color.RED));
        tool.setViewport(new Viewport());
        tool.setFrameOfReference(FrameOfReference.WORLD);
        tool.setPivotMatrix(MatrixHelper.createIdentityMatrix4());
        tool.update(1.0);

        // tool requires a camera.  Registry has it.
        // tool requires a viewport.  Does not exist in headless mode.
    }
}
