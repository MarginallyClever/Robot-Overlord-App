package com.marginallyclever.robotoverlord.systems.render.material;

import com.marginallyclever.robotoverlord.components.MaterialComponent;

import java.io.BufferedInputStream;

public interface MaterialLoader {
    String getEnglishName();
    String[] getValidExtensions();

    /**
     * Load data from stream
     * @param inputStream source of data
     * @param material material into which data will be loaded
     * @throws Exception if something goes wrong
     */
    void load(BufferedInputStream inputStream, MaterialComponent material) throws Exception;
}
