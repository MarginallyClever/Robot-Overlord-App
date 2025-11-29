package com.marginallyclever.ro3.apps.viewport;

/**
 * Associates layer enums, names, and indexes for easy global reference.
 * Shaders should use these indexes to access the correct texture layers.
 */
public enum TextureLayerIndex {
    ALBEDO("Albedo",0),  // aka the diffuse layer
    NORMAL("Normal",1),
    METALLIC("Metallic",2),  // aka reflectance
    ROUGHNESS("Roughness",3),  // aka the bump map
    AO("AO",4);

    private final String name;
    private final int index;

    TextureLayerIndex(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static void get(int index) {
        for( var i : values() ) {
            if( i.getIndex() == index ) {
                return;
            }
        }
        throw new IllegalArgumentException("No TextureLayerIndex with index "+index);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
