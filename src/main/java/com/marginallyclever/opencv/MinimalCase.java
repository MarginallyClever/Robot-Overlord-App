package com.marginallyclever.opencv;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import nu.pattern.OpenCV;

public class MinimalCase {

	public MinimalCase() {}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// load shared OpenCV stuff.
		OpenCV.loadShared();

		VideoCapture vc = new VideoCapture();
    	vc.open(0, Videoio.CAP_DSHOW);
    	if(!vc.isOpened()) {
    		System.out.println("Capture open failed.");
    	} else {
    		System.out.println("Capture open OK.");
    	}
	}
}
