package com.marginallyclever.robotoverlord.preferences;

import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;

import java.util.prefs.Preferences;

/**
 * Container for interaction preferences
 * @since 2.7.0
 * @author Dan Royer
 */
public class InteractionPreferences {
    private static final Preferences preferences = Preferences.userRoot().node("RobotOverlord").node("Interaction");

    public static final IntParameter cursorSize = new IntParameter("cursor size",10);
    public static final DoubleParameter toolScale = new DoubleParameter("tool scale",0.035);
    public static final DoubleParameter compassSize = new DoubleParameter("compass size",25);

    public static void save() {
        preferences.putInt("cursorSize", cursorSize.get());
        preferences.putDouble("toolScale", toolScale.get());
        preferences.putDouble("compassSize", compassSize.get());
    }

    public static void load() {
        cursorSize.set(preferences.getInt("cursorSize", cursorSize.get()));
        toolScale.set(preferences.getDouble("toolScale", toolScale.get()));
        compassSize.set(preferences.getDouble("compassSize", compassSize.get()));
    }
}
