/**
 * 
 */
package com.marginallyclever.opencv;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import nu.pattern.OpenCV;

/**
 * Much thanks to https://opencv-java-tutorials.readthedocs.io/en/stable/05%20-%20Camera%20Calibration.html
 * @author Dan Royer
 *
 */
public class CameraCalibration {
	static final int NUM_CORNERS_VER = 6;
	static final int NUM_CORNERS_HOR = 9
			;
	/**
	 * 
	 */
	public CameraCalibration() {}

    // swing components
    private JFrame frame;
    // swing components
    private JPanel panel;
    // swing components
    private ImageIcon icon;
    // swing components
    private JButton snapshotButton;
    
    // to capture images from the camera.
	private VideoCapture capture = new VideoCapture();
    // timing of image captures.
    private Timer timer;
    // the saved chessboard image
    private Mat savedImage = new Mat();

    private int successes=0;
    private int boardsNumber=10;
    private boolean isCalibrated=false;

    // calibration data
	private MatOfPoint3f obj = new MatOfPoint3f();
	private MatOfPoint2f imageCorners = new MatOfPoint2f();
    private List<Mat> imagePoints = new ArrayList<>();
    private List<Mat> objectPoints = new ArrayList<>();
    private Mat intrinsic = new Mat(3, 3, CvType.CV_32FC1);
    private Mat distCoeffs = new Mat();

    /**
     * Find and draws the points needed for the calibration on the chessboard
     *
     * @param frame
     *            the current frame
     * @return the current number of successfully identified chessboards as an
     *         int
     */
    private void findAndDrawPoints(Mat frame)
    {
    	// init
    	Mat grayImage = new Mat();

    	// I would perform this operation only before starting the calibration
    	// process
    	if (this.successes < this.boardsNumber)
    	{
    		// convert the frame in gray scale
    		Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);
    		// the size of the chessboard
    		Size boardSize = new Size(NUM_CORNERS_HOR, NUM_CORNERS_VER);
    		// look for the inner chessboard corners
    		boolean found = Calib3d.findChessboardCorners(grayImage, boardSize, imageCorners,
    				Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
    		// all the required corners have been found...
    		if (found) {
    			// optimization
    			TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
    			Imgproc.cornerSubPix(grayImage, imageCorners, new Size(11, 11), new Size(-1, -1), term);
    			// save the current frame for further elaborations
    			grayImage.copyTo(this.savedImage);
    			// show the chessboard inner corners on screen
    			Calib3d.drawChessboardCorners(frame, boardSize, imageCorners, found);

    			// enable the option for taking a snapshot
    			snapshotButton.setEnabled(true);
    		}
    		else
    		{
    			snapshotButton.setEnabled(false);
    		}
    	}
    }

	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the buffer
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(buffer.toArray()));
			return img;
		}
		catch(IOException e) {
			return null;
		}
	}
	
    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Image} to show
     */
    private Image grabFrame() {
        // init everything
        Image imageToShow = null;
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
        	try {
        		// read the current frame
        		this.capture.read(frame);

        		// if the frame is not empty, process it
        		if (!frame.empty())
        		{
        			// show the chessboard pattern
        			this.findAndDrawPoints(frame);

        			if (this.isCalibrated) {
        				// prepare the undistored image
        				Mat undistored = new Mat();
        				Calib3d.undistort(frame, undistored, intrinsic, distCoeffs);
        				imageToShow = mat2Image(undistored);
        			}

        			// convert the Mat object (OpenCV) to Image (JavaFX)
        			imageToShow = mat2Image(frame);
        		}

        	}
        	catch (Exception e)
        	{
        		// log the (full) error
        		System.err.print("ERROR");
        		e.printStackTrace();
        	}
        }

        return imageToShow;
    }
    
    protected void startCamera() {
		snapshotButton.setEnabled(false);
		
        System.out.println("startCamera() open device 0");
    	//capture.open(0,Videoio.CAP_DSHOW);
    	capture.open("http://sixi.ddns.net:8081/?action=stream",Videoio.CAP_FFMPEG);
    	
    	if(!capture.isOpened()) {
    		System.out.println("Capture open failed.");
    	} else {
    		System.out.println("Capture open OK.");
    	}
    	
        System.out.println("startCamera() start timer");

        // grab a frame every 33 ms (30 frames/sec)
        TimerTask frameGrabber = new TimerTask() {
            @Override
            public void run() {
                //System.out.println("startCamera() grab");
                Image camStream=grabFrame();
                if(camStream==null) return;
                // show the original frames
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        icon.setImage(camStream);
                        panel.invalidate();
                        panel.repaint();
                    }
                });

            }
        };
        this.timer = new Timer();
        this.timer.schedule(frameGrabber, 0, 33);

		snapshotButton.setEnabled(true);
        panel.repaint();
    }
    
    protected void run() {
    	createWindow();
    	
		int numSquares = NUM_CORNERS_HOR * NUM_CORNERS_VER;
        for (int j = 0; j < numSquares; j++) {
        	obj.push_back(new MatOfPoint3f(new Point3(j / NUM_CORNERS_HOR, j % NUM_CORNERS_VER, 0.0f)));
        }

        System.out.println("startCamera() begin");
        startCamera();
        System.out.println("startCamera() end");
    }
    
    protected void createWindow() {
    	 frame = new JFrame("OpenCV Camera calibration");
    	 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	 panel = new JPanel();
    	 panel.setMinimumSize(new Dimension(500,500));
    	 panel.setPreferredSize(new Dimension(500,500));
    	 frame.getContentPane().add(panel);

    	 icon=new ImageIcon();
         panel.add(new JLabel(icon));
         snapshotButton = new JButton("Take snapshot"); 
         panel.add(snapshotButton);
         snapshotButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (successes < boardsNumber) {
                    // save all the needed values
                    imagePoints.add(imageCorners);
                    objectPoints.add(obj);
                    successes++;
                }

                // reach the correct number of images needed for the calibration
                if (successes == boardsNumber) {
                	calibrateCamera();
                }
			}
		});
         
    	 frame.pack();
    	 frame.setVisible(true);
    }

    /**
     * The effective camera calibration, to be performed once in the program
     * execution
     */
    private void calibrateCamera() {
    	System.out.println("Calibrating...");
        // init needed variables according to OpenCV docs
        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();
        intrinsic.put(0, 0, 1);
        intrinsic.put(1, 1, 1);
        // calibrate!
        Calib3d.calibrateCamera(objectPoints, imagePoints, savedImage.size(), intrinsic, distCoeffs, rvecs, tvecs);
        this.isCalibrated = true;

        System.out.println("intrinsic="+intrinsic);
        System.out.println("coefficients="+distCoeffs);
        // you cannot take other snapshot, at this point...
        snapshotButton.setEnabled(false);
    }
    
	protected void printBuildInformatipn() {
		System.out.println(Core.getBuildInformation());
	}
	
    /**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("START");
		// load shared OpenCV stuff.
		OpenCV.loadShared();

		CameraCalibration cc = new CameraCalibration();
		cc.run();
		
		System.out.println("END");
	}

}
