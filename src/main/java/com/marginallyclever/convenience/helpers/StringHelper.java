package com.marginallyclever.convenience.helpers;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A collection of static methods to help with strings.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class StringHelper {
	static public String shortenNumber(String s) {
		if(!s.contains(".")) return s;
		while(s.endsWith("0")) s=s.substring(0,s.length()-1);
		if(s.endsWith(".")) s=s.substring(0,s.length()-1);
		return s;
	}
	
	static public String formatFloat(float arg0) {
		//return Float.toString(roundOff(arg0));
		return shortenNumber(String.format("%.3f", arg0));
	}
	
	static public String formatDouble(double arg0) {
		//return Float.toString(roundOff(arg0));
		return shortenNumber(String.format("%.3f", arg0));
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
	 * @param bytes byte array
	 * @return double value
	 */
	public static double byteArrayToFloat(byte[] bytes) {
	    int intBits = 
	      bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	    return (double)Float.intBitsToFloat(intBits);  
	}

	/**
	 * 
	 * @param seconds time in seconds
	 * @return formatted string
	 */
	public static String formatTime(double seconds) {
		double p1 = seconds % 60;
		int p2 = (int)(seconds / 60);
		int p3 = p2 % 60;
        p2 = p2 / 60;
        
        String value ="";
        if(p2>0) value += p2 + ":";
        if(p2>0 || p3>0) value += p3 + ":";
        value += formatDouble(p1);
		
		return value;
	}
	
	// expects "[a,b,c]" where a,b,c are doubles 
	public static Tuple3d parseTuple3d(String s) throws IOException {
		if(!s.startsWith("(") || !s.endsWith(")")) throw new IOException("Invalid format, start and end.");
		
		s=s.substring(1, s.length()-1);
		String [] pieces = s.split(",");
		if(pieces.length != 3) throw new IOException("Invalid format, wrong number of values."); 

		double x = Double.parseDouble(pieces[0].trim());
		double y = Double.parseDouble(pieces[1].trim());
		double z = Double.parseDouble(pieces[2].trim());
		return new Vector3d(x,y,z);
	}

	public static Matrix3d parseMatrix3d(String s) throws Exception {
		String [] pieces = s.split("[,\n]");		
		double [] mArray = new double[9];
		for(int i=0;i<mArray.length;++i) mArray[i] = Double.parseDouble(pieces[i].trim());
		return new Matrix3d(mArray);
	}

	public static Matrix4d parseMatrix4d(String s) throws Exception {
		String [] pieces = s.split("[,\n]");		
		double [] mArray = new double[16];
		for(int i=0;i<mArray.length;++i) mArray[i] = Double.parseDouble(pieces[i].trim());
		return new Matrix4d(mArray);
	}
}
