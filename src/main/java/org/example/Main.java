package org.example;

import org.example.peripherals.GlobalKeyboardExample;

import java.awt.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() throws Exception {
//        Scripts scripts = new Scripts();
//        scripts.begin();
//
//        ClickLogger clickLogger = new ClickLogger();
//        clickLogger.startClickLogging();

//        ScreenSample screenSample = new ScreenSample();
//        Thread.sleep(2000);
//        screenSample.determineGardenOrientation(new Robot());
        GlobalKeyboardExample globalKeyboardExample = new GlobalKeyboardExample();
        GlobalKeyboardExample.startGlobalKeyboardHook();
//       EdgeActivator activator = new EdgeActivator();
//        boolean success = activator.bringEdgeToFront();
//
//        if (success) {
//            System.out.println("Microsoft Edge was successfully activated.");
//        } else {
//            System.out.println("Microsoft Edge window not found.");
//        }
    }
}
