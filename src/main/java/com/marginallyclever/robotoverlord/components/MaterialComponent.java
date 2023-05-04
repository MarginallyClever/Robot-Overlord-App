package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.ComponentWithDiskAsset;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import org.json.JSONException;
import org.json.JSONObject;

public class MaterialComponent extends Component implements ComponentWithDiskAsset {
    public final ColorParameter ambient    = new ColorParameter("Ambient" ,1,1,1,1);
    public final ColorParameter diffuse    = new ColorParameter("Diffuse" ,1,1,1,1);
    public final ColorParameter specular   = new ColorParameter("Specular",1,1,1,1);
    public final ColorParameter emission   = new ColorParameter("Emission",0,0,0,1);
    public final IntParameter shininess    = new IntParameter("Shininess",10);
    public final BooleanParameter isLit    = new BooleanParameter("Lit",true);
    public final TextureParameter texture  = new TextureParameter("Texture",null);

    public MaterialComponent() {
        super();
    }

    public void render(GL2 gl2) {
        gl2.glColor4d(diffuse.getR(),diffuse.getG(),diffuse.getB(),diffuse.getA());
        gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse.getFloatArray(),0);
        gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular.getFloatArray(),0);
        gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emission.getFloatArray(),0);
        gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient.getFloatArray(),0);
        gl2.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, shininess.get().floatValue());
        gl2.glColorMaterial(GL2.GL_FRONT,GL2.GL_AMBIENT_AND_DIFFUSE );

        boolean isColorEnabled = gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
        gl2.glDisable(GL2.GL_COLOR_MATERIAL);

        gl2.glShadeModel(GL2.GL_SMOOTH);

        if(isLit()) gl2.glEnable(GL2.GL_LIGHTING);
        else gl2.glDisable(GL2.GL_LIGHTING);

        texture.render(gl2);

        if(isColorEnabled) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
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
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("isLit",isLit.toJSON());
        jo.put("emission",emission.toJSON());
        jo.put("ambient",ambient.toJSON());
        jo.put("diffuse",diffuse.toJSON());
        jo.put("specular",specular.toJSON());
        jo.put("shininess",shininess.toJSON());
        jo.put("texture",texture.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        isLit.parseJSON(jo.getJSONObject("isLit"));
        emission.parseJSON(jo.getJSONObject("emission"));
        ambient.parseJSON(jo.getJSONObject("ambient"));
        diffuse.parseJSON(jo.getJSONObject("diffuse"));
        specular.parseJSON(jo.getJSONObject("specular"));
        shininess.parseJSON(jo.getJSONObject("shininess"));
        texture.parseJSON(jo.getJSONObject("texture"));
    }

    /**
     * Returns true if the diffuse color has an alpha value different from 1.0
     * @return true if the diffuse color has an alpha value different from 1.0
     */
    public boolean isAlpha() {
        return diffuse.getA()!=1.0;
    }

    /**
     * adjust the path of the disk assets in the component.
     *
     * @param originalPath the original path to the asset
     * @param newPath      the new path to the asset
     */
    @Override
    public void adjustPath(String originalPath, String newPath) {
        String oldPath = this.getTextureFilename();
        String adjustedPath = oldPath;
        if(oldPath.startsWith(originalPath)) {
            adjustedPath = newPath + oldPath.substring(originalPath.length());
        }
        this.setTextureFilename(adjustedPath);
    }
}
