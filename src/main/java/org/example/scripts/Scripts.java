package org.example.scripts;

import java.awt.*;
import org.example.ScreenPoint;
import org.example.EdgeActivator;

public class Scripts {
    private static Scripts instance;
    private Robot robot;
    private EdgeActivator edgeActivator;

    public Scripts() throws AWTException {
        this.robot = new Robot();
        this.edgeActivator = new EdgeActivator();
    }

    public static Scripts getInstance() throws AWTException {
        if (instance == null) {
            instance = new Scripts();
        }
        return instance;
    }

    public void moveMouseToPoint(ScreenPoint point) {
        robot.mouseMove(point.x(), point.y());
    }

    public void clickPoint(ScreenPoint point) {
        robot.mouseMove(point.x(), point.y());
        robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
    }

    public void startUpSequence() throws InterruptedException {
        // Two-second grace period to allow user to switch to the game window
        Thread.sleep(2000);
        moveMouseToPoint(ScreenPoint.CENTER_SCREEN);
    }

    public void begin() {
        try {
            startUpSequence();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
