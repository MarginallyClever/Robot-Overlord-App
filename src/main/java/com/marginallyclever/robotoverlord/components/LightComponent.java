package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A light {@link Component}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@ComponentDependency(components={PoseComponent.class})
public class LightComponent extends Component {

    private final static ColorRGB[] PRESET_BLACK = {
            new ColorRGB(0,0,0),  // ambient
            new ColorRGB(0,0,0),  // specular
            new ColorRGB(0,0,0),  // diffuse
    };

    private final static ColorRGB [] PRESET_NOON = {
            new ColorRGB(   0,   0,   0),
            new ColorRGB( 255, 255, 251),
            new ColorRGB(   1,   1,   1),
    };

    private final static ColorRGB [] PRESET_METAL_HALIDE = {
            new ColorRGB(   0,   0,   0),
            new ColorRGB( 242, 252, 255),
            new ColorRGB(   0,   0,   0),
    };

    public final static String [] PRESET_NAMES = {
            "custom/unknown",
            "Noon",
            "Metal halide",
            "Black",
    };

    private final float[] position={0,0,1,0};
    private final float[] spotDirection={0,0,1};
    public final BooleanParameter isDirectional = new BooleanParameter("Spotlight",false);
    public final IntParameter preset = new IntParameter("Preset",0);
    public final ColorParameter diffuse = new ColorParameter("Diffuse" ,1,1,1,1);
    public final ColorParameter specular= new ColorParameter("Specular",0,0,0,1);
    public final ColorParameter ambient = new ColorParameter("Ambient" ,0,0,0,1);
    public final DoubleParameter cutoff = new DoubleParameter("Spot cone (0...90)",180);
    public final DoubleParameter exponent = new DoubleParameter("Spot Exponent",0);
    public final DoubleParameter attenuationConstant = new DoubleParameter("Constant attenuation",1.0);
    public final DoubleParameter attenuationLinear = new DoubleParameter("Linear attenuation",0.014);
    public final DoubleParameter attenuationQuadratic = new DoubleParameter("Quadratic attenuation",0.0007);

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

    public void setPreset(int i) {
        ColorRGB [] choice = switch (i) {
            case 1 -> PRESET_NOON;
            case 2 -> PRESET_METAL_HALIDE;
            case 3 -> PRESET_BLACK;
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
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("diffuse",diffuse.toJSON(context));
        jo.put("ambient",ambient.toJSON(context));
        jo.put("specular",specular.toJSON(context));
        jo.put("attenuationConstant",attenuationConstant.toJSON(context));
        jo.put("attenuationLinear",attenuationLinear.toJSON(context));
        jo.put("attenuationQuadratic",attenuationQuadratic.toJSON(context));
        jo.put("isDirectional",isDirectional.toJSON(context));
        jo.put("cutoff",cutoff.toJSON(context));
        jo.put("exponent",exponent.toJSON(context));
        jo.put("preset",preset.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        diffuse.parseJSON(jo.getJSONObject("diffuse"),context);
        ambient.parseJSON(jo.getJSONObject("ambient"),context);
        specular.parseJSON(jo.getJSONObject("specular"),context);
        attenuationConstant.parseJSON(jo.getJSONObject("attenuationConstant"),context);
        attenuationLinear.parseJSON(jo.getJSONObject("attenuationLinear"),context);
        attenuationQuadratic.parseJSON(jo.getJSONObject("attenuationQuadratic"),context);
        isDirectional.parseJSON(jo.getJSONObject("isDirectional"),context);
        cutoff.parseJSON(jo.getJSONObject("cutoff"),context);
        exponent.parseJSON(jo.getJSONObject("exponent"),context);
        preset.parseJSON(jo.getJSONObject("preset"),context);
    }
}
