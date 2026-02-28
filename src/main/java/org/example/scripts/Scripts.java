package org.example.scripts;

import java.awt.*;
import org.example.ScreenPoint;
import org.example.EdgeActivator;
import org.example.ScreenSample;

public class Scripts {
    private static Scripts instance;
    private Robot robot;
    private EdgeActivator edgeActivator;
    private ScreenSample screenSample;

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

    public void startUpSequence() throws Exception {
        boolean success = edgeActivator.bringEdgeToFront();
        if (success) {
            /*
             * After activating Edge, we wait 2 seconds to ensure the window is fully
             * in the foreground and ready to receive input. Then we move the mouse to
             * the center of the screen to prepare for interaction with the game.
             */
            System.out.println("Microsoft Edge was successfully activated. Waiting 2 seconds before moving mouse to center of screen...");
            Thread.sleep(2000);
            boolean isGardenAbove = ScreenSample.determineGardenOrientation(robot);
            navigateToStartingPoint(isGardenAbove, robot);

        } else {
            System.err.println("ERROR: Attempt to open Edge was unsuccessful. Please make sure Microsoft Edge is open with the Magic Garden game and try again.");
            System.exit(1);
        }

    }

    public void begin() {
        try {
            System.out.println("Beginning startup sequence. Attempting to activate Microsoft Edge and switch to the game window...");
            startUpSequence();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void navigateToStartingPoint(boolean isGardenAbove, Robot robot) {
        if (isGardenAbove) {
            for (int i = 0; i < 10; i++) {
                // Walk up
                robot.keyPress(38);
            }
            for (int i = 0; i < 10; i++) {
                // Walk left
                robot.keyPress(37);
            }
        } else {
            // Walk down
            robot.keyPress(40);
            for (int i = 0; i < 10; i++) {
                // Walk left
                robot.keyPress(37);
            }
        }

    }



}
