package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClickLogger implements NativeMouseListener {

    public static void startClickLogging() {
        // Disable JNativeHook logging spam
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Failed to register native hook.");
            return;
        }

        GlobalScreen.addNativeMouseListener(new ClickLogger());

        System.out.println("Click anywhere on the screen...");
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        System.out.println("Clicked at X=" + e.getX() + " Y=" + e.getY());
    }

    @Override public void nativeMouseClicked(NativeMouseEvent e) {}
    @Override public void nativeMouseReleased(NativeMouseEvent e) {}
}