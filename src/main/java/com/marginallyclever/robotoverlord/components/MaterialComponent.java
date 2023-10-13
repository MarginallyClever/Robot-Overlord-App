package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * A material is a collection of parameters that describe how a surface should be rendered.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class MaterialComponent extends Component {
    public final ColorParameter ambient    = new ColorParameter("Ambient" ,1,1,1,1);
    public final ColorParameter diffuse    = new ColorParameter("Diffuse" ,1,1,1,1);
    public final ColorParameter specular   = new ColorParameter("Specular",1,1,1,1);
    public final ColorParameter emission   = new ColorParameter("Emission",0,0,0,1);
    public final IntParameter shininess    = new IntParameter("Shininess",10);
    public final BooleanParameter isLit    = new BooleanParameter("Lit",true);
    public final TextureParameter texture  = new TextureParameter("Texture",null);
    public final BooleanParameter drawOnTop = new BooleanParameter("Draw on top",false);
    public final BooleanParameter drawOnBottom = new BooleanParameter("Draw on bottom",false);

    public MaterialComponent() {
        super();
    }

    public void render(GL3 gl) {
        texture.render(gl);
    }

    public void setShininess(int arg0) {
        arg0 = Math.min(Math.max(arg0, 0), 128);
        shininess.set(arg0);
    }

    public double getShininess() {
        return shininess.get();
    }

    /**
     *
     * @param r 0...1
     * @param g 0...1
     * @param b 0...1
     * @param a 0...1
     */
    public void setDiffuseColor(double r,double g,double b,double a) {
        diffuse.set(r,g,b,a);
    }

    /**
     *
     * @param r 0...1
     * @param g 0...1
     * @param b 0...1
     * @param a 0...1
     */
    public void setSpecularColor(double r,double g,double b,double a) {
        specular.set(r,g,b,a);
    }

    /**
     *
     * @param r 0...1
     * @param g 0...1
     * @param b 0...1
     * @param a 0...1
     */
    public void setEmissionColor(double r,double g,double b,double a) {
        emission.set(r,g,b,a);
    }

    /**
     *
     * @param r 0...1
     * @param g 0...1
     * @param b 0...1
     * @param a 0...1
     */
    public void setAmbientColor(double r,double g,double b,double a) {
        ambient.set(r,g,b,a);
    }

    public double[] getDiffuseColor() {
        return diffuse.getDoubleArray();
    }

    public double[] getAmbientColor() {
        return ambient.getDoubleArray();
    }

    public double[] getSpecularColor() {
        return specular.getDoubleArray();
    }

    public void setTextureFilename(String arg0) {
        texture.set(arg0);
    }

    public String getTextureFilename() {
        return texture.get();
    }

    public boolean isLit() {
        return isLit.get();
    }

    public void setLit(boolean isLit) {
        this.isLit.set(isLit);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("isLit",isLit.toJSON(context));
        jo.put("emission",emission.toJSON(context));
        jo.put("ambient",ambient.toJSON(context));
        jo.put("diffuse",diffuse.toJSON(context));
        jo.put("specular",specular.toJSON(context));
        jo.put("shininess",shininess.toJSON(context));
        jo.put("texture",texture.toJSON(context));
        jo.put("drawOnTop",drawOnTop.toJSON(context));
        jo.put("drawOnBottom",drawOnBottom.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        isLit.parseJSON(jo.getJSONObject("isLit"),context);
        emission.parseJSON(jo.getJSONObject("emission"),context);
        ambient.parseJSON(jo.getJSONObject("ambient"),context);
        diffuse.parseJSON(jo.getJSONObject("diffuse"),context);
        specular.parseJSON(jo.getJSONObject("specular"),context);
        shininess.parseJSON(jo.getJSONObject("shininess"),context);
        texture.parseJSON(jo.getJSONObject("texture"),context);
        if(jo.has("drawOnTop")) drawOnTop.parseJSON(jo.getJSONObject("drawOnTop"),context);
        if(jo.has("drawOnBottom")) drawOnBottom.parseJSON(jo.getJSONObject("drawOnBottom"),context);
    }

    @Override
    public String toString() {
        return super.toString()
                + ",isLit=" + isLit.get()
                + ",emission=" + Arrays.toString(emission.get())
                + ",ambient=" + Arrays.toString(ambient.get())
                + ",diffuse=" + Arrays.toString(diffuse.get())
                + ",specular=" + Arrays.toString(specular.get())
                + ",shininess=" + shininess.get()
                + ",texture=" + texture.get()
                + ",drawOnTop=" + drawOnTop.get()
                + ",drawOnBottom=" + drawOnBottom.get()
                +",\n";
    }

    /**
     * Returns true if the diffuse color has an alpha value different from 1.0
     * @return true if the diffuse color has an alpha value different from 1.0
     */
    public boolean isAlpha() {
        return diffuse.getA()!=1.0;
    }

    public void reloadTextures(GL3 gl) {
        texture.loadTexture(gl);
    }
}
