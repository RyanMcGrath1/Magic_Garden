package org.example.elements;

public enum ScreenElements {
    // Define screen elements with their expected RGB values
    ANCHOR_POSITION(new RGBColor(191, 126, 28)),
    DAWN_ANCHOR_POSITION(new RGBColor(153, 101, 65));

    //For filters the plan would be to add together filter with above item to derive seasonal variation
//    SUSPECTED_DAWN_FILTER(new RGBColor(-38, -25, 37));

    private final RGBColor expectedColor;

    ScreenElements(RGBColor expectedColor) {
        this.expectedColor = expectedColor;
    }

    public RGBColor getExpectedColor() {
        return expectedColor;
    }

    /**
     * Inner class to represent RGB color values
     */
    public static class RGBColor {
        private final int red;
        private final int green;
        private final int blue;

        public RGBColor(int red, int green, int blue) {
            if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
                throw new IllegalArgumentException("RGB values must be between 0 and 255");
            }
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int getRed() {
            return red;
        }

        public int getGreen() {
            return green;
        }

        public int getBlue() {
            return blue;
        }

        @Override
        public String toString() {
            return "RGB(" + red + "," + green + "," + blue + ")";
        }

        /**
         * Calculate the difference between this color and another
         * Returns 0 if colors are identical, higher values indicate greater difference
         */
        public double colorDifference(RGBColor other) {
            int dR = this.red - other.red;
            int dG = this.green - other.green;
            int dB = this.blue - other.blue;
            return Math.sqrt(dR * dR + dG * dG + dB * dB);
        }
    }
}
