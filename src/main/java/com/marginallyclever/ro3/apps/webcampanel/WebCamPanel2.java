package com.marginallyclever.ro3.apps.webcampanel;

/**
 * <p>Capture Video Stream: Use OpenCV to access the USB camera. You can create a VideoCapture object and specify the
 * device index or the name of the video file you want to capture.</p>
 * <p>Read Frames: Continuously read frames from the camera using the read() method on the Mat object. This will be
 * used for processing.</p>
 */
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.apps.App;
import nu.pattern.OpenCV;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Point;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

public class WebCamPanel2 extends App {
    private VideoCapture camera = null;
    private Mat intrinsicMatrix;
    private Mat distortionCoefficients;

    private FeatureDetectionAndMatching featureMatcher;

    private Mat frame;
    private Mat prevFrame;

    public Matrix4d cameraMatrix = MatrixHelper.createIdentityMatrix4();
    public final List<Vector3d> points3DList = new ArrayList<>();
    public final List<Color> colorList = new ArrayList<>();

    static {
        try {
            System.out.println("Loading OpenCV library...");
            OpenCV.loadLocally();
            System.out.println("OpenCV library loaded successfully.");
        } catch (Exception e) {
            System.err.println("OpenCV library failed to load: " + e);
        }
    }

    public WebCamPanel2() {
        super(new BorderLayout());
        setName("webcam 2");
        intrinsicMatrix = new Mat();
        distortionCoefficients = new Mat();
        frame = new Mat();
        prevFrame = new Mat();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        camera = new VideoCapture();
        try {
            System.out.println("Opening camera...");
            camera.open(0); // Use 0 to access the default camera
            System.out.println("Camera opened successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        if (!camera.isOpened()) {
            System.out.println("Error: Camera is not available");
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // Release the camera after use
        camera.release();
    }

    public Mat getFrame() {
        Mat newFrame = new Mat();
        if (camera.read(newFrame)) {
            // Frame has been captured
            // Process the frame as needed
            prevFrame = frame;
            frame = newFrame;

            if(!prevFrame.empty() && !frame.empty()) {
                featureMatcher = new FeatureDetectionAndMatching();
                MatOfDMatch matches = featureMatcher.matchFeatures(prevFrame, frame);

                // Extract points from matches
                List<Point> points1 = new ArrayList<>();
                List<Point> points2 = new ArrayList<>();
                for (DMatch match : matches.toList()) {
                    points1.add(new Point(match.queryIdx, match.queryIdx));
                    points2.add(new Point(match.trainIdx, match.trainIdx));
                }
                MatOfPoint2f matchedPoints1 = new MatOfPoint2f();
                matchedPoints1.fromList(points1);
                MatOfPoint2f matchedPoints2 = new MatOfPoint2f();
                matchedPoints2.fromList(points2);

                // Estimate the camera motion to get the rotation and translation vectors
                Mat rotationVector = new Mat();
                Mat translationVector = new Mat();
                Calib3d.estimateAffine3D(matchedPoints1, matchedPoints2, rotationVector, translationVector);

                // Construct the projection matrices for the two camera views
                Mat projectionMatrix1 = getProjectionMatrix(rotationVector, translationVector);
                Mat projectionMatrix2 = getProjectionMatrix(rotationVector, translationVector);

                // Triangulate the matched points to get the 3D points
                Mat points4D = new Mat();
                Calib3d.triangulatePoints(projectionMatrix1, projectionMatrix2, matchedPoints1, matchedPoints2, points4D);

                // Convert the 4D points to 3D points
                points3DList.clear();
                colorList.clear();
                for (int i = 0; i < points4D.cols(); i++) {
                    double w = points4D.get(3, i)[0];
                    double x = points4D.get(0, i)[0] / w;
                    double y = points4D.get(1, i)[0] / w;
                    double z = points4D.get(2, i)[0] / w;
                    points3DList.add(new Vector3d(x, y, z));

                    // Get the color of the corresponding 2D point in the original image
                    Point point2D = points1.get(i);
                    double[] color = frame.get((int) point2D.y, (int) point2D.x);
                    colorList.add(new Color((int)color[0],(int)color[1],(int)color[2]));
                }

                // Construct the 4x4 camera matrix
                double [] m = new double[16];
                for (int i = 0; i < 3; i++) {
                    m[i*4  ] = translationVector.get(i, 0)[0];
                    m[i*4+1] = translationVector.get(i, 1)[0];
                    m[i*4+2] = translationVector.get(i, 2)[0];
                    m[i*4+3] = translationVector.get(i, 3)[0];
                }
                cameraMatrix.set(m);
            }
        }
        return frame;
    }

    private Mat getProjectionMatrix(Mat rotationVector, Mat translationVector) {
        Mat rotationMatrix = new Mat();
        Calib3d.Rodrigues(rotationVector, rotationMatrix);

        Mat projectionMatrix = new Mat(3, 4, CvType.CV_64F);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                projectionMatrix.put(i, j, rotationMatrix.get(i, j)[0]);
            }
            projectionMatrix.put(i, 3, translationVector.get(i, 0)[0]);
        }

        Core.gemm(intrinsicMatrix, projectionMatrix, 1, new Mat(), 0, projectionMatrix);

        return projectionMatrix;
    }

    /**
     * Get the camera matrix.
     * @return 4x4 camera matrix
     */
    public Matrix4d getCameraMatrix() {
        return cameraMatrix;
    }

    /**
     * Get the 3D points list.  These are the estimated positions of the points shared by each frame of video.
     * @return List of 3D points
     */
    public List<Vector3d> getPoints3DList() {
        return points3DList;
    }

    public static void main(String[] args) {
        WebCamPanel2 panel = new WebCamPanel2();
        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);

        JFrame frame = new JFrame("WebCamPanel2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(container);
        frame.setSize(640, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Create a Swing Timer to continuously update the frame
        Timer timer = new Timer(1000 / 30, e -> {
            panel.getFrame(); // Update the frame
            panel.repaint(); // Repaint the panel
        });
        timer.start(); // Start the timer
    }
}
