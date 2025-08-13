package com.marginallyclever.ro3.mesh.load;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads a MTL Material Format file into a {@link Material}.  If more than one material is found, all will
 * be loaded, overwriting each other. See also <a href="http://paulbourke.net/dataformats/mtl/">Paul Bourke</a>.
 *
 */
public class LoadMTL {
    private static final Logger logger = LoggerFactory.getLogger(LoadMTL.class);

    public String getEnglishName() {
        return "Wavefront Material (MTL)";
    }

    public String[] getValidExtensions() {
        return new String[]{"mtl"};
    }

    public void load(BufferedInputStream inputStream, Material material) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        // load the data and store it until we're finished.  that way we only process the alpha once.
        int alpha = 255;
        Color diffuse = Color.WHITE;
        Color specular = Color.WHITE;
        Color ambient = Color.BLACK;

        String line;
        while( ( line = br.readLine() ) != null ) {
            if (line.startsWith("Ns")) {
                // Ns is from 0...1000 and opengl is 0...128
                material.setShininess((int) (Float.parseFloat(line.substring(3)) * 128.0f / 1000.0f));
            } else if (line.startsWith("Ka")) {
                //ambient = readColorFromString(line);
            } else if (line.startsWith("Kd")) {
                diffuse = readColorFromString(line);
            } else if (line.startsWith("Ks")) {
                specular = readColorFromString(line);
            } else if (line.startsWith("map_Kd")) {
                // diffuse texture map
                material.setDiffuseTexture(Registry.textureFactory.load(line.substring(7)));
            } else if(line.startsWith("Ni")) {
                // index of refraction 0.001 to 10.  1 means light does not bend.
                material.setIOR(Double.parseDouble(line.substring(4)));
            } else if(line.startsWith("d")) {
                // material transparency.  1.0 is opaque, 0.0 is transparent.
                var d = Double.parseDouble(line.substring(3)) * 255;
                alpha = (int)Math.clamp(d,0,255);
            } else if(line.startsWith("Tr")) {
                // material transparency.  1.0 is opaque, 0.0 is transparent.
                var d = Double.parseDouble(line.substring(4)) * 255;
                alpha = (int)Math.clamp(d,0,255);
            }else if(line.startsWith("illum")) {
                // illumination model.  0=constant, 1=diffuse, 2=specular, 3=diffuse+specular, 4=reflection, 5=diffuse+reflection, 6=specular+reflection, 7=diffuse+specular+reflection
                logger.warn("illum model not supported: {}", line);
            }
        }

        //material.setAmbientColor(new Color(ambient.getRed(),ambient.getGreen(),ambient.getBlue(),alpha));
        material.setDiffuseColor(new Color(diffuse.getRed(),diffuse.getGreen(),diffuse.getBlue(),alpha));
        material.setSpecularColor(new Color(specular.getRed(),specular.getGreen(),specular.getBlue(),alpha));
    }

    private Color readColorFromString(String line) {
        String[] parts = line.split(" ");
        int [] components = new int[3];
        for(int i=0;i<3;++i) {
            var d = Double.parseDouble(parts[i+1]) * 255.0;
            components[i] = (int)Math.clamp(d,0,255);
        }
        return new Color(components[0],components[1],components[2]);
    }
}
