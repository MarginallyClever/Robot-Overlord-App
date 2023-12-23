package com.marginallyclever.ro3.apps.render;

import com.jogamp.opengl.GLEventListener;
import com.marginallyclever.ro3.Registry;

/**
 * Classes which implement {@link RenderPass} are drawn on top of the 3D scene.  They should be registered to the
 * {@link Registry}, which remembers the order in which they should be drawn.
 */
public interface RenderPass extends GLEventListener {
    int NEVER = 0;
    int SOMETIMES = 1;
    int ALWAYS = 2;
    int MAX_STATUS = 3;

    /**
     * @return NEVER, SOMETIMES, or ALWAYS
     */
    int getActiveStatus();

    /**
     * @param status NEVER, SOMETIMES, or ALWAYS
     */
    void setActiveStatus(int status);

    /**
     * @return the localized name of this overlay
     */
    String getName();

    void draw();
}
