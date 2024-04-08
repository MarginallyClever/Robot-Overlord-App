package com.marginallyclever.ro3.apps.webcampanel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;

public class FeatureDetectionAndMatching {
    private final ORB detector;
    private final DescriptorMatcher matcher;

    public FeatureDetectionAndMatching() {
        detector = ORB.create();
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    }

    public MatOfDMatch matchFeatures(Mat img1, Mat img2) {
        MatOfKeyPoint keyPoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

        detector.detectAndCompute(img1, new Mat(), keyPoints1, descriptors1);
        detector.detectAndCompute(img2, new Mat(), keyPoints2, descriptors2);

        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors1, descriptors2, matches);

        return matches;
    }
}