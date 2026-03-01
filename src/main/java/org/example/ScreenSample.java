package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import org.example.elements.ScreenElements;

public class ScreenSample {
    public static boolean determineGardenOrientation(Robot robot) {
        // Capture the anchor position area from the screen
        Rectangle anchorArea = new Rectangle(915, 328, 88, 87);
        BufferedImage img = robot.createScreenCapture(anchorArea);

        // Define the ROI relative to the captured image
        int x0 = 0, y0 = 0, w = img.getWidth(), h = img.getHeight();

        // Validate ROI bounds
        if (x0 < 0 || y0 < 0 || x0 + w > img.getWidth() || y0 + h > img.getHeight()) {
            throw new IllegalArgumentException("Requested ROI is outside of captured image bounds: " +
                    "imgWidth=" + img.getWidth() + ", imgHeight=" + img.getHeight() + ", roi=(" + x0 + "," + y0 + "," + w + "," + h + ")");
        }

        // Accumulate RGB values for all pixels in the ROI
        long r = 0, g = 0, b = 0;
        int count = 0;

        for (int y = y0; y < y0 + h; y++) {
            for (int x = x0; x < x0 + w; x++) {
                int rgb = img.getRGB(x, y);
                r += (rgb >> 16) & 0xFF; // red channel
                g += (rgb >> 8) & 0xFF;  // green channel
                b += rgb & 0xFF;         // blue channel
                count++;
            }
        }

        // Validate pixel count
        if (count == 0) {
            throw new IllegalStateException("No pixels sampled; ROI width/height may be zero.");
        }

        // Calculate average RGB values
        int avgR = (int) (r / count);
        int avgG = (int) (g / count);
        int avgB = (int) (b / count);

        // Create color objects for comparison
        ScreenElements.RGBColor sampledColor = new ScreenElements.RGBColor(avgR, avgG, avgB);
        ScreenElements.RGBColor expectedColor = ScreenElements.ANCHOR_POSITION.getExpectedColor();

        // Calculate color difference
        double colorDifference = sampledColor.colorDifference(expectedColor);
        double colorTolerance = 10.0;

        // Check if the sampled color matches the expected ANCHOR_POSITION color
        boolean isMatch = colorDifference <= colorTolerance;

        // Log the results
        System.out.println("Sampled Color: " + sampledColor);
        System.out.println("Expected Color: " + expectedColor);
        System.out.println("Color Difference: " + String.format("%.2f", colorDifference));
        System.out.println("Color Tolerance: " + colorTolerance);
        System.out.println("Match Result: " + (isMatch ? "MATCH" : "NO MATCH"));

        return isMatch;
    }

    public static void determineRBGValue(Robot robot) {
        // Capture the anchor position area from the screen
        Rectangle area = new Rectangle(952, 735, 1, 1);
        BufferedImage img = robot.createScreenCapture(area);

        // Define the ROI relative to the captured image
        int x0 = 0, y0 = 0, w = img.getWidth(), h = img.getHeight();

        // Validate ROI bounds
        if (x0 < 0 || y0 < 0 || x0 + w > img.getWidth() || y0 + h > img.getHeight()) {
            throw new IllegalArgumentException("Requested ROI is outside of captured image bounds: " +
                    "imgWidth=" + img.getWidth() + ", imgHeight=" + img.getHeight() + ", roi=(" + x0 + "," + y0 + "," + w + "," + h + ")");
        }

        // Accumulate RGB values for all pixels in the ROI
        long r = 0, g = 0, b = 0;
        int count = 0;

        for (int y = y0; y < y0 + h; y++) {
            for (int x = x0; x < x0 + w; x++) {
                int rgb = img.getRGB(x, y);
                r += (rgb >> 16) & 0xFF; // red channel
                g += (rgb >> 8) & 0xFF;  // green channel
                b += rgb & 0xFF;         // blue channel
                count++;
            }
        }

        // Validate pixel count
        if (count == 0) {
            throw new IllegalStateException("No pixels sampled; ROI width/height may be zero.");
        }

        // Calculate average RGB values
        int avgR = (int) (r / count);
        int avgG = (int) (g / count);
        int avgB = (int) (b / count);

        // Create color objects for comparison
        ScreenElements.RGBColor sampledColor = new ScreenElements.RGBColor(avgR, avgG, avgB);
        ScreenElements.RGBColor expectedColor = ScreenElements.ANCHOR_POSITION.getExpectedColor();

        // Calculate color difference
        double colorDifference = sampledColor.colorDifference(expectedColor);
        double colorTolerance = 10.0;

        // Check if the sampled color matches the expected ANCHOR_POSITION color
        boolean isMatch = colorDifference <= colorTolerance;

        // Log the results
        System.out.println("Sampled Color: " + sampledColor);
        System.out.println("Expected Color: " + expectedColor);
        System.out.println("Color Difference: " + String.format("%.2f", colorDifference));
        System.out.println("Color Tolerance: " + colorTolerance);
        System.out.println("Match Result: " + (isMatch ? "MATCH" : "NO MATCH"));
    }

}
