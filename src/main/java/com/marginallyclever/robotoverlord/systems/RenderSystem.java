package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.systems.render.gcodepath.PathFactory;
import com.marginallyclever.robotoverlord.components.shapes.Grid;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.components.shapes.Sphere;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;

/**
 * A system that decorates and manages various {@link RenderComponent}s.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class RenderSystem implements EntitySystem {
    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if(component instanceof RenderComponent) decorateRender(view, component);
        if(component instanceof GCodePathComponent) decorateGCodePath(view, component);
        if(component instanceof MeshFromFile) decorateMeshFromFile(view, component);
        if(component instanceof Sphere) decorateSphere(view,component);
        if(component instanceof Grid) decorateGrid(view,component);
        if(component instanceof LightComponent) decorateLight(view,component);
        if(component instanceof MaterialComponent) decorateMaterial(view,component);
        if(component instanceof LinearPatternComponent) decorateLinearPattern(view,component);

        if(component instanceof PathComponent) decoratePath(view, component);
        else if(component instanceof ShapeComponent) decorateShape(view, component);
    }

    private void decorateRender(ComponentPanelFactory view, Component component) {
        RenderComponent renderComponent = (RenderComponent) component;
        view.add(renderComponent.isVisible);
    }

    private void decorateShape(ComponentPanelFactory view, Component component) {
        ShapeComponent shapeComponent = (ShapeComponent) component;
        view.add(shapeComponent.numVertices).setReadOnly(true);
        view.add(shapeComponent.hasNormals).setReadOnly(true);
        view.add(shapeComponent.hasColors).setReadOnly(true);
        view.add(shapeComponent.hasUVs).setReadOnly(true);
    }

    public void decorateGCodePath(ComponentPanelFactory view,Component component) {
        GCodePathComponent pathComponent = (GCodePathComponent)component;
        ArrayList<FileFilter> filters = PathFactory.getAllExtensions();
        view.addFilename(pathComponent.filename,filters);
        view.addButton("Reload").addActionEventListener(e -> pathComponent.reload());
        view.add(pathComponent.numCommands).setReadOnly(true);
        view.add(pathComponent.distanceMeasured).setReadOnly(true);
        view.add(pathComponent.getCommand).addPropertyChangeListener((e)->pathComponent.updateLocation());
    }

    public void decoratePath(ComponentPanelFactory view,Component component) {
        PathComponent pathComponent = (PathComponent) component;
        ArrayList<FileFilter> filters = PathFactory.getAllExtensions();
        view.add(pathComponent.moveSpeed);
        view.addComboBox(pathComponent.moveType, PathComponent.MOVE_TYPE_NAMES);
    }

    public void decorateLinearPattern(ComponentPanelFactory view,Component component) {
        LinearPatternComponent patternComponent = (LinearPatternComponent) component;
        view.addComboBox(patternComponent.spacingType, LinearPatternComponent.SPACING_TYPE_NAMES);
        view.add(patternComponent.measure);
        view.add(patternComponent.quantity);
    }

    private void decorateMeshFromFile(ComponentPanelFactory view, Component component) {
        MeshFromFile meshComponent = (MeshFromFile) component;
        view.addFilename(meshComponent.filename,MeshFactory.getAllExtensions());
        view.addButton("Reload").addActionEventListener(e -> meshComponent.reload());
    }

    private void decorateSphere(ComponentPanelFactory view, Component component) {
        Sphere sphereComponent = (Sphere) component;
        view.add(sphereComponent.detail);
    }

    private void decorateGrid(ComponentPanelFactory view, Component component) {
        Grid sphereComponent = (Grid) component;
        view.add(sphereComponent.width);
        view.add(sphereComponent.length);
        view.add(sphereComponent.snap);
    }

    private void decorateLight(ComponentPanelFactory view,Component component) {
        LightComponent light = (LightComponent)component;
        view.add(light.isDirectional);
        view.addComboBox(light.preset, LightComponent.PRESET_NAMES);
        view.add(light.diffuse);
        view.add(light.specular);
        view.add(light.ambient);
        view.add(light.cutoff);
        view.add(light.exponent);
        view.add(light.attenuationConstant);
        view.add(light.attenuationLinear);
        view.add(light.attenuationQuadratic);
    }

    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    public void decorateMaterial(ComponentPanelFactory view, Component component) {
        MaterialComponent material = (MaterialComponent)component;
        view.add(material.isLit  );
        view.add(material.emission);
        view.add(material.ambient );
        view.add(material.diffuse );
        view.add(material.specular);
        view.addRange(material.shininess, 128, 0);

        view.addFilename(material.texture, TextureParameter.getFilters());
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {}
}
