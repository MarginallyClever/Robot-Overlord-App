package com.marginallyclever.robotoverlord.components.path;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads a GCode file into a {@link GCodePath}.
 * @author Dan Royer
 * @since 2.5.0
 */
class GCodePathLoader implements TurtlePathLoader {
    @Override
    public String getEnglishName() {
        return "GCode (gcode,nc,ngc)";
    }

    @Override
    public String[] getValidExtensions() {
        return new String[] { "gcode", "nc", "ngc" };
    }

    public void load(BufferedInputStream inputStream, GCodePath model) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Pattern pattern = Pattern.compile("([GMT])(\\d+)|([XYZUVWFE])(-?\\d*(\\.\\d+)?)");

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
                    double value;
                    try {
                        value = Double.parseDouble(axisValue);
                    }
                    catch(NumberFormatException ex) {
                        throw ex;
                    }
                    switch (axisGroup) {
                        case "X" -> element.setX(value);
                        case "Y" -> element.setY(value);
                        case "Z" -> element.setZ(value);
                        case "U" -> element.setU(value);
                        case "V" -> element.setV(value);
                        case "W" -> element.setW(value);
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
