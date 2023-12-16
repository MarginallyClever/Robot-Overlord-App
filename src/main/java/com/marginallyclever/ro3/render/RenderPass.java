package com.marginallyclever.ro3.render;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;

/**
 * Classes which implement {@link RenderPass} are drawn on top of the 3D scene.  They should be registered to the
 * {@link Registry}, which remembers the order in which they should be drawn.
 */
public interface RenderPass {
    int NEVER = 0;
    int SOMETIMES = 1;
    int ALWAYS = 2;

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

    void draw(ShaderProgram shader);
}
