package com.marginallyclever.opencv;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.opencv.global.opencv_calib3d;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import org.bytedeco.opencv.opencv_imgproc.*;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_calib3d.*;


public class JavaCVDemo {

	public JavaCVDemo() {}
	
    public static void main(String[] args) throws Exception {
        String classifierName = null;
        if (args.length > 0) {
            classifierName = args[0];
        } else {
            URL url = new URL("https://raw.github.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml");
            File file = Loader.cacheResource(url);
            classifierName = file.getAbsolutePath();
        }

        // We can "cast" Pointer objects by instantiating a new object of the desired class.
        CascadeClassifier classifier = new CascadeClassifier(classifierName);
        if (classifier == null) {
            System.err.println("Error loading classifier file \"" + classifierName + "\".");
            System.exit(1);
        }

        // The available FrameGrabber classes include OpenCVFrameGrabber (opencv_videoio),
        // DC1394FrameGrabber, FlyCapture2FrameGrabber, OpenKinectFrameGrabber, OpenKinect2FrameGrabber,
        // RealSenseFrameGrabber, RealSense2FrameGrabber, PS3EyeFrameGrabber, VideoInputFrameGrabber, and FFmpegFrameGrabber.
        //FrameGrabber grabber = FrameGrabber.createDefault(0);
        FrameGrabber grabber = FFmpegFrameGrabber.createDefault("http://sixi.ddns.net:8081/?action=stream");
        grabber.start();

        // CanvasFrame, FrameGrabber, and FrameRecorder use Frame objects to communicate image data.
        // We need a FrameConverter to interface with other APIs (Android, Java 2D, JavaFX, Tesseract, OpenCV, etc).
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        // FAQ about IplImage and Mat objects from OpenCV:
        // - For custom raw processing of data, createBuffer() returns an NIO direct
        //   buffer wrapped around the memory pointed by imageData, and under Android we can
        //   also use that Buffer with Bitmap.copyPixelsFromBuffer() and copyPixelsToBuffer().
        // - To get a BufferedImage from an IplImage, or vice versa, we can chain calls to
        //   Java2DFrameConverter and OpenCVFrameConverter, one after the other.
        // - Java2DFrameConverter also has static copy() methods that we can use to transfer
        //   data more directly between BufferedImage and IplImage or Mat via Frame objects.
        Mat grabbedImage = converter.convert(grabber.grab());
        int height = grabbedImage.rows();
        int width = grabbedImage.cols();

        // Objects allocated with `new`, clone(), or a create*() factory method are automatically released
        // by the garbage collector, but may still be explicitly released by calling deallocate().
        // You shall NOT call cvReleaseImage(), cvReleaseMemStorage(), etc. on objects allocated this way.
        Mat grayImage = new Mat(height, width, CV_8UC1);
        //Mat rotatedImage = grabbedImage.clone();

        // The OpenCVFrameRecorder class simply uses the VideoWriter of opencv_videoio,
        // but FFmpegFrameRecorder also exists as a more versatile alternative.
        //FrameRecorder recorder = FrameRecorder.createDefault("output.avi", width, height);
        //recorder.start();

        // CanvasFrame is a JFrame containing a Canvas component, which is hardware accelerated.
        // It can also switch into full-screen mode when called with a screenNumber.
        // We should also specify the relative monitor/camera response for proper gamma correction.
        CanvasFrame frame = new CanvasFrame("Dan was here", CanvasFrame.getDefaultGamma()/grabber.getGamma());

        // We can allocate native arrays using constructors taking an integer as argument.
        Point hatPoints = new Point(3);
        JavaCVDemo demo = new JavaCVDemo ();

        while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {
            demo.findAndDrawPoints(grabbedImage);

            Frame rotatedFrame = converter.convert(grabbedImage);
            frame.showImage(rotatedFrame);
            //recorder.record(rotatedFrame);
        }
        hatPoints.close();
        classifier.close();
        frame.dispose();
        //recorder.stop();
        grabber.stop();
	}

	static final int NUM_CORNERS_VER = 6;
	static final int NUM_CORNERS_HOR = 9;
	private int successes=0;
	private  int boardsNumber=10;
	private  boolean isCalibrated=false;

    // calibration data
	private Mat imageCorners = new Mat();
	private  List<Mat> imagePoints = new ArrayList<>();
	private  List<Mat> objectPoints = new ArrayList<>();
	private  Mat intrinsic = new Mat(3, 3, opencv_core.CV_32FC1);
	private  Mat distCoeffs = new Mat();
    
    /**
     * Find and draws the points needed for the calibration on the chessboard
     *
     * @param frame
     *            the current frame
     * @return the current number of successfully identified chessboards as an
     *         int
     */
    private void findAndDrawPoints(Mat grabbedImage)
    {
    	// init
    	Mat grayImage = new Mat();

    	// I would perform this operation only before starting the calibration
    	// process
    	if (successes < boardsNumber)
    	{
            // Let's try to detect some faces! but we need a grayscale image...
            cvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
            
    		// the size of the chessboard
    		Size boardSize = new Size(NUM_CORNERS_HOR, NUM_CORNERS_VER);
    		// look for the inner chessboard corners
    		boolean found = opencv_calib3d.findChessboardCorners(grayImage, boardSize, imageCorners,
    				opencv_calib3d.CALIB_CB_ADAPTIVE_THRESH + opencv_calib3d.CALIB_CB_NORMALIZE_IMAGE + opencv_calib3d.CALIB_CB_FAST_CHECK);
    		// all the required corners have been found...
    		if (found) {
    			// optimization
    			TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
    			opencv_imgproc.cornerSubPix(grayImage, imageCorners, new Size(11, 11), new Size(-1, -1), term);
    			// save the current frame for further elaborations
    			//grayImage.copyTo(this.savedImage);
    			// show the chessboard inner corners on screen
    			opencv_calib3d.drawChessboardCorners(grabbedImage, boardSize, imageCorners, found);
    		}
    	}
    }

}
