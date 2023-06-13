package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;

/**
 * A RenderComponent is a component that draws something into the 3D scene.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public abstract class RenderComponent extends Component {
    public transient final BooleanParameter isVisible = new BooleanParameter("Visible",true);

    public RenderComponent() {
        super();
    }

    public boolean getVisible() {
        return isVisible.get();
    }

    public void setVisible(boolean arg0) {
        isVisible.set(arg0);
    }

    public abstract void render(GL3 gl);
}
