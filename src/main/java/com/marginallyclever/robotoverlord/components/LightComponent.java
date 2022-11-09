package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.parameters.ColorEntity;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.parameters.IntEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;

@ComponentDependency(components={PoseComponent.class})
public class LightComponent extends Component {

    private final static ColorRGB[] presetBlack = {
            new ColorRGB(0,0,0),  // ambient
            new ColorRGB(0,0,0),  // specular
            new ColorRGB(0,0,0),  // diffuse
    };

    private final static ColorRGB [] presetNoon = {
            new ColorRGB(   0,   0,   0),
            new ColorRGB( 255, 255, 251),
            new ColorRGB(   1,   1,   1),
    };

    private final static ColorRGB [] presetMetalHalide = {
            new ColorRGB(   0,   0,   0),
            new ColorRGB( 242, 252, 255),
            new ColorRGB(   0,   0,   0),
    };

    private final static String [] presetNames = {
            "custom/unknown",
            "Noon",
            "Metal halide",
            "Black",
    };

    private final float[] position={0,0,1,0};
    private final float[] spotDirection={0,0,1};
    private final BooleanEntity isDirectional = new BooleanEntity("Spotlight",false);

    private final IntEntity preset = new IntEntity("Preset",0);
    private final ColorEntity diffuse = new ColorEntity("Diffuse" ,1,1,1,1);
    private final ColorEntity specular= new ColorEntity("Specular",0,0,0,1);
    private final ColorEntity ambient = new ColorEntity("Ambient" ,0,0,0,1);

    private final DoubleEntity cutoff = new DoubleEntity("Spot cone (0...90)",180);
    private final DoubleEntity exponent = new DoubleEntity("Spot Exponent",0);

    private final DoubleEntity attenuationConstant = new DoubleEntity("Constant attenuation",1.0);
    private final DoubleEntity attenuationLinear = new DoubleEntity("Linear attenuation",0.014);
    private final DoubleEntity attenuationQuadratic = new DoubleEntity("Quadratic attenuation",0.0007);

    public void setupLight(GL2 gl2, int lightIndex) {
        int i = GL2.GL_LIGHT0+lightIndex;

        gl2.glEnable(i);

        Matrix4d poseWorld = getEntity().findFirstComponent(PoseComponent.class).getWorld();
        position[0]=(float)poseWorld.m03;
        position[1]=(float)poseWorld.m13;
        position[2]=(float)poseWorld.m23;
        position[3]=isDirectional.get()?1:0;
        gl2.glLightfv(i, GL2.GL_POSITION, position,0);

        gl2.glLightfv(i, GL2.GL_AMBIENT, ambient.getFloatArray(),0);
        gl2.glLightfv(i, GL2.GL_DIFFUSE, diffuse.getFloatArray(),0);
        gl2.glLightfv(i, GL2.GL_SPECULAR, specular.getFloatArray(),0);

        // z axis of the matrix is the light direction
        spotDirection[0]=(float)poseWorld.m02;
        spotDirection[1]=(float)poseWorld.m12;
        spotDirection[2]=(float)poseWorld.m22;
        gl2.glLightfv(i, GL2.GL_SPOT_DIRECTION, spotDirection,0);

        gl2.glLightf(i, GL2.GL_SPOT_CUTOFF, cutoff.get().floatValue());
        gl2.glLightf(i, GL2.GL_SPOT_EXPONENT, exponent.get().floatValue());

        // falloff/fade out
        gl2.glLightf(i, GL2.GL_CONSTANT_ATTENUATION,attenuationConstant.get().floatValue());
        gl2.glLightf(i, GL2.GL_LINEAR_ATTENUATION,attenuationLinear.get().floatValue());
        gl2.glLightf(i, GL2.GL_QUADRATIC_ATTENUATION,attenuationQuadratic.get().floatValue());
    }

    /**
     *
     * @param arg0 true for directional light, false for point source light.
     */
    public void setDirectional(boolean arg0) {
        isDirectional.set(arg0);
    }

    public boolean isDirectional() {
        return isDirectional.get();
    }

    public void setDiffuse(float r,float g,float b,float a) {
        diffuse.set(r,g,b,a);
    }

    public float[] getDiffuse() {
        return diffuse.getFloatArray();
    }

    public void setAmbient(float r,float g,float b,float a) {
        ambient.set(r,g,b,a);
    }

    public float[] getAmbient() {
        return ambient.getFloatArray();
    }

    public void setSpecular(float r,float g,float b,float a) {
        specular.set(r,g,b,a);
    }

    public float[] getSpecular() {
        return specular.getFloatArray();
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        view.add(isDirectional);
        view.addComboBox(preset, presetNames);

        view.add(diffuse);
        view.add(specular);
        view.add(ambient);

        view.add(cutoff);
        view.add(exponent);

        view.add(attenuationConstant);
        view.add(attenuationLinear);
        view.add(attenuationQuadratic);
    }

    public void setPreset(int i) {
        ColorRGB [] choice = switch (i) {
            case 1 -> presetNoon;
            case 2 -> presetMetalHalide;
            case 3 -> presetBlack;
            default -> null;
        };

        if(choice!=null) {
            ColorRGB c;
            c= choice[0];	this.setAmbient (c.red/255f, c.green/255f, c.blue/255f, 1);
            c= choice[1];	this.setSpecular(c.red/255f, c.green/255f, c.blue/255f, 1);
            c= choice[2];	this.setDiffuse (c.red/255f, c.green/255f, c.blue/255f, 1);
        }
    }
    public void setAttenuationConstant(double d) {
        attenuationConstant.set(d);
    }
    public double getAttenuationConstant() {
        return attenuationConstant.get();
    }
    public void setAttenuationLinear(double d) {
        attenuationLinear.set(d);
    }
    public double getAttenuationLinear() {
        return attenuationLinear.get();
    }
    public void setAttenuationQuadratic(double d) {
        attenuationQuadratic.set(d);
    }
    public double getAttenuationQuadratic() {
        return attenuationQuadratic.get();
    }
    public double getExponent() {
        return exponent.get();
    }
    public void setExponent(double exponent) {
        this.exponent.set(exponent);
    }
    public double getCutoff() {
        return cutoff.get();
    }
    public void setCutoff(double cutoff) {
        this.cutoff.set(cutoff);
    }

    @Override
    public String toString() {
        return super.toString()+",\n"
            +diffuse.toString()+",\n"
            +ambient.toString()+",\n"
            +specular.toString()+",\n"
            +attenuationConstant.toString()+",\n"
            +attenuationLinear.toString()+",\n"
            +attenuationQuadratic.toString()+",\n"
            +isDirectional.toString()+",\n"
            +cutoff.toString()+",\n"
            +exponent.toString()+",\n"
            +preset.toString();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("diffuse",diffuse.toJSON());
        jo.put("ambient",ambient.toJSON());
        jo.put("specular",specular.toJSON());
        jo.put("attenuationConstant",attenuationConstant.toJSON());
        jo.put("attenuationLinear",attenuationLinear.toJSON());
        jo.put("attenuationQuadratic",attenuationQuadratic.toJSON());
        jo.put("isDirectional",isDirectional.toJSON());
        jo.put("cutoff",cutoff.toJSON());
        jo.put("exponent",exponent.toJSON());
        jo.put("preset",preset.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        diffuse.parseJSON(jo.getJSONObject("diffuse"));
        ambient.parseJSON(jo.getJSONObject("ambient"));
        specular.parseJSON(jo.getJSONObject("specular"));
        attenuationConstant.parseJSON(jo.getJSONObject("attenuationConstant"));
        attenuationLinear.parseJSON(jo.getJSONObject("attenuationLinear"));
        attenuationQuadratic.parseJSON(jo.getJSONObject("attenuationQuadratic"));
        isDirectional.parseJSON(jo.getJSONObject("isDirectional"));
        cutoff.parseJSON(jo.getJSONObject("cutoff"));
        exponent.parseJSON(jo.getJSONObject("exponent"));
        preset.parseJSON(jo.getJSONObject("preset"));
    }
}
