package com.marginallyclever.ro3.apps.viewport.renderpass;

import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.ro3.apps.viewport.Viewport;

/**
 * {@link AbstractRenderPass} handles common methods for all {@link RenderPass}.
 */
public abstract class AbstractRenderPass implements RenderPass {
    private String name;
    private int activeStatus = ALWAYS;
    protected int canvasWidth, canvasHeight;

    protected AbstractRenderPass() {
        this("RenderPass");
    }

    protected AbstractRenderPass(String name) {
        super();
        setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the localized name of this pass.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public int getActiveStatus() {
        return activeStatus;
    }

    /**
     * @param status NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public void setActiveStatus(int status) {
        activeStatus = status;
    }

    @Override
    public void draw(Viewport viewport) {}

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }
}
