package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

public abstract class RenderComponent extends Component {
    protected transient final BooleanEntity isVisible = new BooleanEntity("Visible",true);

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
    public void getView(ViewPanel view) {
        super.getView(view);
        view.add(isVisible);
    }
}
