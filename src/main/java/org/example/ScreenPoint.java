package org.example;

import java.awt.Point;

public enum ScreenPoint {
    SHOP_BUTTON(766, 159),
    MY_GARDEN(963, 163),
    SELL_BUTTON(1172, 165),
    CENTER_SCREEN(960, 540),
    WEATHER_CONDITIONS(1400, 810);

    private final int x;
    private final int y;
    private final Point point; // optional cached object

    ScreenPoint(int x, int y) {
        this.x = x;
        this.y = y;
        this.point = new Point(x, y); // cached so you don't allocate repeatedly
    }

    public int x() { return x; }
    public int y() { return y; }
    public Point point() { return point; }
}
