package com.marginallyclever.robotOverlord.makelangeloRobot.converters;

import java.io.IOException;
import java.io.Writer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.makelangeloRobot.ImageManipulator;
import com.marginallyclever.robotOverlord.makelangeloRobot.TransformedImage;
import com.marginallyclever.robotOverlord.makelangeloRobot.MakelangeloRobotDecorator;
import com.marginallyclever.robotOverlord.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * Converts a BufferedImage to gcode
 * Don't forget http://www.reverb-marketing.com/wiki/index.php/When_a_new_style_has_been_added_to_the_Makelangelo_software
 * @author Dan Royer
 *
 */
public abstract class ImageConverter extends ImageManipulator implements MakelangeloRobotDecorator {
	/**
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 * @param out where to write the converted image.
	 * @return true if conversion succeeded.
	 * @throws IOException if write fails
	 */
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		return false;
	}

	/**
	 * live preview as the system is converting pictures.
	 * draw the results as the calculation is being performed.
	 */
	public void render(GL2 gl2,MakelangeloRobotSettings settings) {}
}
