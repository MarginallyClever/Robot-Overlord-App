package com.marginallyclever.robotOverlord;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class InputManagerTest {
	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	
    // Output the current java.library.path and the current working directory.
    @Test
    public void reportJavaLibraryPath() {
        String property = System.getProperty("java.library.path");
        Log.message("java.library.path=" + property.replace(";", "\n  "));

        Log.message("Working Directory = " + System.getProperty("user.dir"));
    }


    // Figure out which input just changed.  Requires human input.
    @Test
    public void detectSingleInputChange() {
        Log.message("detectSingleInputChange() start");
        int i, j, k;

        // count all devices and components
        int totalComponents = 0;
        Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
        int totalControllers = ca.length;
        for (i = 0; i < totalControllers; i++) {
            totalComponents += ca[i].getComponents().length;
        }
        float[] stateOld = new float[totalComponents];
        float[] stateNew = new float[totalComponents];

        // get initial state
        InputManager.update(true);
        k = 0;
        for (i = 0; i < totalControllers; i++) {
            Component[] components = ca[i].getComponents();
            for (j = 0; j < components.length; j++) {
                stateOld[k] = components[j].getPollData();
                k++;
            }
        }

        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < 10000) {  // 10 seconds
            // get the latest state
            InputManager.update(true);
            ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
            if (ca.length != totalControllers) {
                // uh oh!  devide added/removed!  restart!
                break;
            }
            k = 0;
            for (i = 0; i < totalControllers; i++) {
                Component[] components = ca[i].getComponents();
                for (j = 0; j < components.length; j++) {
                    stateNew[k] = components[j].getPollData();
                    float diff = stateNew[k] - stateOld[k];
                    if (Math.abs(diff) > 0.5) {
                        // change to state
                        Log.message("Found " + ca[i].getName() + "." + components[j].getName() + "=" + diff);
                        //return;
                    }
                    k++;
                }
            }
        }
        Log.message("detectSingleInputChange() end");
    }

    // Figure out which input just changed.  Requires human input.
    @Test
    public void detectSingleInputChangeB() {
        int i, j;

        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < 10000) {  // 10 seconds
            // get the latest state
            InputManager.update(true);
            Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
            for (i = 0; i < ca.length; i++) {
                Component[] components = ca[i].getComponents();
                for (j = 0; j < components.length; j++) {
                    float diff = components[j].getPollData();
                    if (diff > 0.5) {
                        // change to state
                        Log.message("Found " + ca[i].getName() + "." + components[j].getName() + "=" + diff);
                        //return;
                    }
                }
            }
        }
    }
}
