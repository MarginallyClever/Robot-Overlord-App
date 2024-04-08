package com.marginallyclever.ro3.apps.webcampanel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.DMatch;
import org.opencv.calib3d.Calib3d;

import java.util.ArrayList;
import java.util.List;

/**
 * EstimateMotion class to estimate camera motion from feature matches.
 */
public class EstimateMotion {
    /**
     * Estimate camera motion from feature matches.
     *
     * @param matches  Matches between two frames
     * @param points1  Points from the first frame
     * @param points2  Points from the second frame
     * @return Rotation matrix representing camera motion.
     */
    public Mat estimateCameraMotion(MatOfDMatch matches, MatOfPoint2f points1, MatOfPoint2f points2) {
        List<DMatch> matchList = matches.toList();
        List<Point> pts1 = new ArrayList<>();
        List<Point> pts2 = new ArrayList<>();

        for (DMatch match : matchList) {
            pts1.add(points1.toList().get(match.queryIdx));
            pts2.add(points2.toList().get(match.trainIdx));
        }

        MatOfPoint2f pts1Mat = new MatOfPoint2f();
        pts1Mat.fromList(pts1);
        MatOfPoint2f pts2Mat = new MatOfPoint2f();
        pts2Mat.fromList(pts2);

        Mat essentialMatrix = Calib3d.findEssentialMat(pts1Mat, pts2Mat);
        Mat R = new Mat();
        Mat t = new Mat();
        Calib3d.recoverPose(essentialMatrix, pts1Mat, pts2Mat, R, t);

        return R; // Return rotation matrix, modify as needed
    }
}