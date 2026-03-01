package org.example;

import org.example.peripherals.GlobalKeyboardExample;

import java.awt.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() throws Exception {
//        Scripts scripts = new Scripts();
//        scripts.begin();

//        ClickLogger clickLogger = new ClickLogger();
//        ClickLogger.startClickLogging();

        ScreenSample screenSample = new ScreenSample();
        Thread.sleep(2000);
        ScreenSample.determineRBGValue(new Robot());


    }
}
