package com.marginallyclever.convenience;

public class StringHelper {
	
	static public String formatFloat(float arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format("%.3f", arg0);
	}
	
	static public String formatDouble(double arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format("%.3f", arg0);
	}

	static public double parseNumber(String str) {
		float f=0;
		
		try {
			f = Float.parseFloat(str);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return f;
	}

	// @return "*"+ the binary XOR of every byte in the msg.
	static public String generateChecksum(String msg) {
		byte checksum = 0;

		for (int i = 0; i < msg.length(); ++i) {
			checksum ^= msg.charAt(i);
		}

		return "*" + Integer.toString(checksum);
	}

	static public boolean confirmChecksumOK(String msg) {
		// confirm there IS a checksum
		int starPos = msg.lastIndexOf("*");
		if (starPos == -1)
			return false; // no checksum
		int deliveredChecksum = 0;
		try {
			deliveredChecksum = Integer.parseInt(msg.substring(starPos + 1));
		} catch (NumberFormatException e) {
			return false;
		}

		byte calculatedChecksum = 0;

		for (int i = 0; i < starPos; ++i) {
			calculatedChecksum ^= msg.charAt(i);
		}

		return calculatedChecksum == deliveredChecksum;
	}
}
