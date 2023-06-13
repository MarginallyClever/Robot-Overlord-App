package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;

/**
 * A component that should render a child component in a linear pattern.
 *
 * @since 2.6.0
 * @author Dan Royer
 */
public class LinearPatternComponent extends RenderComponent {
    public static final String[] SPACING_TYPE_NAMES = {"Spacing","Distance"};
    public static int SPACING = 0;
    public static int DISTANCE = 1;
    public final IntParameter spacingType = new IntParameter("type", LinearPatternComponent.SPACING);
    public final DoubleParameter measure = new DoubleParameter("", 1);
    public final IntParameter quantity = new IntParameter("quantity", 1);

    @Override
    public void render(GL3 gl) {
        // draw each child at each pattern location
        // TODO finish me!
    }
}
