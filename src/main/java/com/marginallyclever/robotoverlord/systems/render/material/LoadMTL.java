package com.marginallyclever.robotoverlord.systems.render.material;

import com.marginallyclever.robotoverlord.components.MaterialComponent;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Loads MTL Material Format files into {@link MaterialComponent}s.  If more than one material is found, all will
 * be loaded, overwriting each other. See also <a href="http://paulbourke.net/dataformats/mtl/">Paul Bourke</a>.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class LoadMTL implements MaterialLoader {
    @Override
    public String getEnglishName() {
        return "Wavefront Material (MTL)";
    }

    @Override
    public String[] getValidExtensions() {
        return new String[]{"mtl"};
    }

    @Override
    public void load(BufferedInputStream inputStream, MaterialComponent material) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        // load the data and store it until we're finished.  that way we only process the alpha once.
        double alpha = 1.0;
        double [] ambient = new double[3];
        double [] diffuse = new double[3];
        double [] specular = new double[3];

        String line;
        while( ( line = br.readLine() ) != null ) {
            if (line.startsWith("Ns")) {
                // Ns is from 0...1000 and opengl is 0...128
                material.setShininess((int) (Float.parseFloat(line.substring(3)) * 128.0f / 1000.0f));
            } else if (line.startsWith("Ka")) {
                String[] parts = line.split(" ");
                for(int i=0;i<3;++i) ambient[i] = Double.parseDouble(parts[i+1]);
            } else if (line.startsWith("Kd")) {
                String[] parts = line.split(" ");
                for(int i=0;i<3;++i) diffuse[i] = Double.parseDouble(parts[i+1]);
            } else if (line.startsWith("Ks")) {
                String[] parts = line.split(" ");
                for(int i=0;i<3;++i) specular[i] = Double.parseDouble(parts[i+1]);
            } else if (line.startsWith("map_Kd")) {
                // diffuse texture map
                material.setTextureFilename(line.substring(7));
            }
            // else if(line.startsWith("Ni")) {
            // index of refraction 0.001 to 10.  1 means light does not bend.
            //} else if(line.startsWith("d")) {
            // material transparency.  1.0 is opaque, 0.0 is transparent.
            //} else if(line.startsWith("illum")) {
            // illumination model.  0=constant, 1=diffuse, 2=specular, 3=diffuse+specular, 4=reflection, 5=diffuse+reflection, 6=specular+reflection, 7=diffuse+specular+reflection
            //}
        }

        material.setAmbientColor(ambient[0],ambient[1],ambient[2],alpha);
        material.setDiffuseColor(diffuse[0],diffuse[1],diffuse[2],alpha);
        material.setSpecularColor(specular[0],specular[1],specular[2],alpha);
    }
}
