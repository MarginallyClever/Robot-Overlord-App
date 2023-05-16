package com.marginallyclever.robotoverlord.components;

import java.util.List;
import java.awt.*;

/**
 * This interface is used to mark components that have an asset on disk such as a mesh or a texture.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public interface ComponentWithDiskAsset {
    /**
     * adjust the path of the disk assets in the component.
     * @param originalPath the original path to the asset
     * @param newPath the new path to the asset
     */
    void adjustPath(String originalPath, String newPath);

    List<String> getAssetPaths();
}
