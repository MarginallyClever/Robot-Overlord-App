package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads a Prusa Slic3r GCode file into a {@link GCodePath}.
 * @author Dan Royer
 * @since 2.5.0
 */
public class Slic3rGCodePathLoader implements PathLoader {
    @Override
    public String getEnglishName() {
        return "Slic3r GCode (gcode)";
    }

    @Override
    public String[] getValidExtensions() {
        return new String[] { "gcode" };
    }

    public void load(BufferedInputStream inputStream, GCodePath model) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Pattern pattern = Pattern.compile("([GMT])(\\d+)|([XYZUVWFE])(-?\\d*(\\.\\d+)?)");
        final double MM_TO_CM = 0.1; // convert mm to cm

        model.clear();

        GCodePathElement memory = new GCodePathElement("G0");

        String line;
        while ((line = reader.readLine()) != null) {
            if(line.contains(";")) {
                // throw away comments
                line = line.substring(0,line.indexOf(";"));
            }
            line = line.trim();
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("(") || line.startsWith(")") || line.startsWith("%")) {
                continue; // Skip comments and empty lines
            }

            GCodePathElement element = null;
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String commandGroup = matcher.group(1);
                String commandValue = matcher.group(2);
                String axisGroup = matcher.group(3);
                String axisValue = matcher.group(4);

                if (commandGroup != null && commandValue != null) {
                    element = new GCodePathElement(memory);
                    element.setCommand(commandGroup + commandValue);
                    element.setExtrusion(null);
                } else if (axisGroup != null && axisValue != null) {
                    if (element == null) {
                        throw new Exception("Invalid GCode: Axis value without command");
                    }
                    double value = Double.parseDouble(axisValue)*MM_TO_CM;
                    switch (axisGroup) {
                        case "X" -> element.setX(value);
                        case "Y" -> element.setY(value);
                        case "Z" -> element.setZ(value);
                        case "F" -> element.setFeedrate(value);
                        case "E" -> element.setExtrusion(value);
                    }
                }
            }

            if (element != null) {
                model.addElement(element);
                memory = element;
            } else {
                throw new Exception("Invalid GCode: Unrecognized command or format");
            }
        }
    }
}
