package com.marginallyclever.robotoverlord.components.path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

class GCodePathLoaderTest {
    @Test
    public void testLoadGcode() {
        GCodePathLoader loader = new GCodePathLoader();
        GCodePath pathModel = new GCodePath();

        InputStream stream;
        try {
            stream = this.getClass().getResourceAsStream("Rocket_Engine_0.2mm_PLA_MINI_1h26m.gcode");
        } catch (Exception e) {
            Assertions.fail("Failed to open G-code file", e);
            return;
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(stream)) {
            loader.load(inputStream, pathModel);
            Assertions.assertTrue(pathModel.getElements().size() > 0, "G-code path elements list should not be empty");

            // Add any additional assertions to test the correctness of the loaded G-code data
            // For example, you can test specific elements in the GCodePath model for expected values
            // ...
        } catch (IOException e) {
            Assertions.fail("Failed to read G-code file", e);
        } catch (Exception e) {
            Assertions.fail("Failed to load G-code data", e);
        }
    }
}
