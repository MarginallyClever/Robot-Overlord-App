package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import com.marginallyclever.robotoverlord.systems.render.gcodepath.GCodePath;
import com.marginallyclever.robotoverlord.systems.render.gcodepath.Slic3rGCodePathLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

class Slic3rGCodePathLoaderTest {
    @Test
    public void testLoadGcode() {
        Slic3rGCodePathLoader loader = new Slic3rGCodePathLoader();
        GCodePath pathModel = new GCodePath();

        try(InputStream stream = this.getClass().getResourceAsStream("Rocket_Engine_0.2mm_PLA_MINI_1h26m.gcode")) {
            try (BufferedInputStream inputStream = new BufferedInputStream(stream)) {
                loader.load(inputStream, pathModel);
                Assertions.assertTrue(pathModel.getElements().size() > 0, "G-code gcodepath elements list should not be empty");

                // Add any additional assertions to test the correctness of the loaded G-code data
                // For example, you can test specific elements in the GCodePath model for expected values
                // ...
            } catch (IOException e) {
                Assertions.fail("Failed to read G-code file", e);
            } catch (Exception e) {
                Assertions.fail("Failed to load G-code data", e);
            }
        } catch (Exception e) {
            Assertions.fail("Failed to open G-code file", e);
        }
    }
}
