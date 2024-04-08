package com.marginallyclever.ro3.apps.webcampanel;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;

import java.util.ArrayList;
import java.util.List;

/**
 * Triangulation class to triangulate points from two camera views.
 */
public class Triangulation {
    /**
     * Triangulate points from two camera views.
     * @param projMatr1
     * @param projMatr2
     * @param projPoints1
     * @param projPoints2
     * @return
     */
    public MatOfPoint3f triangulatePoints(Mat projMatr1, Mat projMatr2, MatOfPoint2f projPoints1, MatOfPoint2f projPoints2) {
        Mat points4D = new Mat();
        // Triangulate points from two camera views
        Calib3d.triangulatePoints(projMatr1, projMatr2, projPoints1, projPoints2, points4D);

        // Convert 4D points to 3D points
        List<Point3> points3DList = new ArrayList<>();
        for (int i = 0; i < points4D.cols(); i++) {
            double w = points4D.get(3, i)[0];
            double x = points4D.get(0, i)[0] / w;
            double y = points4D.get(1, i)[0] / w;
            double z = points4D.get(2, i)[0] / w;
            points3DList.add(new Point3(x, y, z));
        }

        MatOfPoint3f points3D = new MatOfPoint3f();
        points3D.fromList(points3DList);

        return points3D;
    }
}