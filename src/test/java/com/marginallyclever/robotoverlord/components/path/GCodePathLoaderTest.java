package com.marginallyclever.robotoverlord.components.path;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class GCodePathLoaderTest {
    @Test
    void testLoad() {
        GCodePathLoader loader = new GCodePathLoader();
        GCodePath pathModel = new GCodePath();

        Path filePath = Paths.get("src", "test", "resources", "Rocket_Engine_0.2mm_PLA_MINI_1h26m.gcode");

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
            loader.load(inputStream, pathModel);
            assertTrue(pathModel.getElements().size() > 0, "G-code path elements list should not be empty");

            // Add any additional assertions to test the correctness of the loaded G-code data
            // For example, you can test specific elements in the GCodePath model for expected values
            // ...
/*
            // Check the min and max values for the X, Y, and Z axes
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxZ = Double.MIN_VALUE;

            for (GCodePathElement element : pathModel.getElements()) {
                double x = element.getX();
                double y = element.getY();
                double z = element.getZ();

                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
            }

            double xRange = maxX - minX;
            double yRange = maxY - minY;
            double zRange = maxZ - minZ;

            // Add assertions for the expected dimensions
            assertEquals(34.19, xRange, 0.1, "X dimension should be close to 34.19 mm");
            assertEquals(30.07, yRange, 0.1, "Y dimension should be close to 30.07 mm");
            assertEquals(60.55, zRange, 0.1, "Z dimension should be close to 60.55 mm");

            // might also be 99.88 x 87.84 x 176.89 mm
*/
        } catch (IOException e) {
            fail("Failed to read G-code file", e);
        } catch (Exception e) {
            fail("Failed to load G-code data", e);
        }
    }
}
