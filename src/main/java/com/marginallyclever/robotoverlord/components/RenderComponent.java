package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

public abstract class RenderComponent extends Component {
    protected transient final BooleanParameter isVisible = new BooleanParameter("Visible",true);

    public RenderComponent() {
        super();
    }

    public boolean getVisible() {
        return isVisible.get();
    }

    public void setVisible(boolean arg0) {
        isVisible.set(arg0);
    }

    public abstract void render(GL2 gl2);

    @Override
    public void getView(ComponentPanelFactory view) {
        super.getView(view);
        view.add(isVisible);
    }
}
