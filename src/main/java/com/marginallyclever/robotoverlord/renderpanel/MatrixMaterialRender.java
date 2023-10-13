package com.marginallyclever.robotoverlord.renderpanel;

import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.RenderComponent;

import javax.vecmath.Matrix4d;

/**
 * A matrix, a material, and a render component.
 */
public class MatrixMaterialRender {
    public Matrix4d matrix = new Matrix4d();
    public RenderComponent renderComponent;
    public MaterialComponent materialComponent;

    public MatrixMaterialRender(Matrix4d matrix, RenderComponent renderComponent, MaterialComponent materialComponent) {
        this.matrix.set(matrix);
        this.renderComponent = renderComponent;
        this.materialComponent = materialComponent;
    }
}