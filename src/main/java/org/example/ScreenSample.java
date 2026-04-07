package org.example;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import org.example.elements.ScreenElements;

public class ScreenSample {

    private static final double DEFAULT_COLOR_TOLERANCE = 10.0;

    private static final Rectangle GARDEN_ORIENTATION_REGION = new Rectangle(915, 328, 88, 87);
    private static final Rectangle RGB_PROBE_REGION = new Rectangle(952, 735, 1, 1);

    /**
     * Samples the garden anchor region and compares it to the expected palette color.
     *
     * @return true if the sampled color is within tolerance of the anchor color
     */
    public static boolean determineGardenOrientation(Robot robot) {
        ScreenElements.RGBColor sampled = sampleRegion(robot, GARDEN_ORIENTATION_REGION);
        return logColorComparison(sampled, ScreenElements.ANCHOR_POSITION.getExpectedColor(), DEFAULT_COLOR_TOLERANCE);
    }

    /** Samples a single-pixel probe region and logs comparison against the anchor color. */
    public static void determineRgbValue(Robot robot) {
        ScreenElements.RGBColor sampled = sampleRegion(robot, RGB_PROBE_REGION);
        logColorComparison(sampled, ScreenElements.ANCHOR_POSITION.getExpectedColor(), DEFAULT_COLOR_TOLERANCE);
    }

    private static ScreenElements.RGBColor sampleRegion(Robot robot, Rectangle screenRect) {
        BufferedImage img = robot.createScreenCapture(screenRect);
        return averageColor(img);
    }

    private static ScreenElements.RGBColor averageColor(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= 0 || h <= 0) {
            throw new IllegalStateException("Captured image has no pixels.");
        }

        long r = 0;
        long g = 0;
        long b = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                r += (rgb >> 16) & 0xFF;
                g += (rgb >> 8) & 0xFF;
                b += rgb & 0xFF;
            }
        }
        int count = w * h;
        return new ScreenElements.RGBColor((int) (r / count), (int) (g / count), (int) (b / count));
    }

    private static boolean logColorComparison(
            ScreenElements.RGBColor sampled,
            ScreenElements.RGBColor expected,
            double colorTolerance) {
        double colorDifference = sampled.colorDifference(expected);
        boolean isMatch = colorDifference <= colorTolerance;

        System.out.println("Sampled Color: " + sampled);
        System.out.println("Expected Color: " + expected);
        System.out.println("Color Difference: " + String.format("%.2f", colorDifference));
        System.out.println("Color Tolerance: " + colorTolerance);
        System.out.println("Match Result: " + (isMatch ? "MATCH" : "NO MATCH"));

        return isMatch;
    }
}
