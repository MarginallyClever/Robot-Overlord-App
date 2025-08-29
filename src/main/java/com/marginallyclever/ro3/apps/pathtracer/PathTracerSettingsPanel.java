package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class PathTracerSettingsPanel extends JPanel {
    private final PathTracer pathTracer;

    public PathTracerSettingsPanel(PathTracer pathTracer) {
        super(new GridBagLayout());
        this.pathTracer = pathTracer;
        setName(getClass().getSimpleName());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;

        var spp = PanelHelper.addNumberFieldInt("Samples per pixel",pathTracer.getSamplesPerPixel());
        spp.addPropertyChangeListener("value",e->pathTracer.setSamplesPerPixel(((Number)e.getNewValue()).intValue()));
        PanelHelper.addLabelAndComponent(this, "Samples per pixel", spp, c);
        c.gridy++;

        var md = PanelHelper.addNumberFieldInt("Max Depth",pathTracer.getMaxDepth());
        md.addPropertyChangeListener("value",e->pathTracer.setMaxDepth(((Number)e.getNewValue()).intValue()));
        PanelHelper.addLabelAndComponent(this, "Max Depth", md, c);

        // max contribution
        var maxContribution = PanelHelper.addNumberFieldDouble("Max contribution",pathTracer.getMaxContribution());
        maxContribution.addPropertyChangeListener("value",e->pathTracer.setMaxContribution(((Number)e.getNewValue()).intValue()));
        PanelHelper.addLabelAndComponent(this, "Max contribution per sample", maxContribution, c);
        c.gridy++;

        // min bounces before russian roulette termination
        var minBounces = PanelHelper.addNumberFieldInt(
                "Min bounces",
                pathTracer.getMinBounces());
        PanelHelper.addLabelAndComponent(this, "Min bounces before RR", minBounces, c);
        c.gridy++;

        // light sampling probability
        var lightSamplingProbability = PanelHelper.createSlider(
                1,0,pathTracer.getLightSamplingProbability(),
                pathTracer::setLightSamplingProbability);
        PanelHelper.addLabelAndComponent(this, "Light sampling %", lightSamplingProbability, c);
        c.gridy++;
    }
}
