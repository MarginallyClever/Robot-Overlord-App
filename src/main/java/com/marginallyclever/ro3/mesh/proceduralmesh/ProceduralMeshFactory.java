package com.marginallyclever.ro3.mesh.proceduralmesh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * <p>Factory for procedural meshes.</p>
 */
public class ProceduralMeshFactory {
    private static final Logger logger = LoggerFactory.getLogger(ProceduralMeshFactory.class);
    private static final String [] proceduralNames = {
            "Box",
            "Capsule",
            "CircleXY",
            "Cylinder",
            "Decal",
            "Grid",
            "Sphere",
            "Waldo"
    };

    /**
     * @return the array of meshes that extend {@link ProceduralMesh}.
     */
    public static String [] getListOfProceduralMeshes() {
        return proceduralNames;
    }

    /**
     * Create a mesh for the given shape name.
     * @param shapeName the name of the shape to create.
     * @return a new mesh or null if the shape name is not found.
     */
    public static ProceduralMesh createMesh(String shapeName) {
        try {
            String className = "com.marginallyclever.ro3.mesh.proceduralmesh." + shapeName;
            Class<?> clazz = Class.forName(className);
            if (ProceduralMesh.class.isAssignableFrom(clazz)) {
                return (ProceduralMesh) clazz.getDeclaredConstructor().newInstance();
            } else {
                logger.error("Class {} is not a ProceduralMesh", className);
            }
        } catch (Exception e) {
            logger.error("Failed to create ProceduralMesh for shape: {}", shapeName, e);
        }
        return null;
    }

    /**
     * @param mesh the {@link ProceduralMesh} to create a panel for.
     * @return the settings panel for the given {@link ProceduralMesh} or null.
     */
    public static JPanel createPanel(ProceduralMesh mesh) {
        if(mesh==null) return null;

        var shapeName = mesh.getEnglishName();
        try {
            String className = "com.marginallyclever.ro3.mesh.proceduralmesh." + shapeName + "Panel";
            Class<?> clazz = Class.forName(className);
            if (JPanel.class.isAssignableFrom(clazz)) {
                var list = clazz.getDeclaredConstructors();
                // find constructor that takes the mesh
                for (var constructor : list) {
                    var types = constructor.getParameterTypes();
                    if (types.length == 1 && types[0].isAssignableFrom(mesh.getClass())) {
                        //logger.debug("Creating panel for mesh: {}", shapeName);
                        return (JPanel) constructor.newInstance(mesh);
                    }
                }
            } else {
                logger.error("Class {} is not a ProceduralMesh", className);
            }
        } catch (Exception e) {
            logger.error("Failed to create JPanel for ProceduralMesh: {}", shapeName, e);
        }
        return null;
    }
}
