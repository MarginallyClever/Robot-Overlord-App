package com.marginallyclever.ro3.apps.viewport.renderpass;

import com.jogamp.opengl.GLEventListener;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.Viewport;

/**
 * <p>Classes which implement {@link RenderPass} are drawn as part of - or on top of - the 3D scene.  They should be
 * registered to the {@link Registry}.  The order of registration controls the order in which they are rendered.
 * They are rendered by the {@link Viewport}.</p>
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
     * @return the localized name
     */
    String getName();

    /**
     * Draw this viewport pass.
     * @param viewport the viewport to draw into
     */
    void draw(Viewport viewport);
}
