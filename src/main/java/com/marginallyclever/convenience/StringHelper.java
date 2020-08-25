package com.marginallyclever.convenience;

import java.text.NumberFormat;
import java.util.Locale;

public class StringHelper {
	
	static public String formatFloat(float arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format("%.3f", arg0);
	}
	
	static public String formatDouble(double arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format("%.3f", arg0);
	}

	/**
	 * Parse a number sent from a US format system
	 * @param str
	 * @return
	 */
	static public double parseNumber(String str) {
		double d=0;
		
		try {
			NumberFormat nf = NumberFormat.getInstance(Locale.US);
			Number n = nf.parse(str);
			d = n.doubleValue();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return d;
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
	
	/**
	 * 
	 * @param value 
	 * @return byte array
	 */
	public static byte[] floatToByteArray(double value) {
	    int intBits =  Float.floatToIntBits((float)value);
	    return new byte[] {
	      (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
	}

	/**
	 * 
	 * @param value byte array
	 * @return double value
	 */
	public static double byteArrayToFloat(byte[] bytes) {
	    int intBits = 
	      bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	    return (double)Float.intBitsToFloat(intBits);  
	}
}
