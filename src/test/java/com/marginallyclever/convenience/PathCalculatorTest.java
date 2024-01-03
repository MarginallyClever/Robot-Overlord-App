package com.marginallyclever.convenience;
import org.junit.jupiter.api.Test;

public class PathCalculatorTest {
    @Test
    public void test() {
        // Example usage
        String origin = "/path/to/origin";
        String goal = "/path/to/goal/subgoal";

        System.out.println(PathCalculator.getRelativePath(origin, goal));
    }
}
