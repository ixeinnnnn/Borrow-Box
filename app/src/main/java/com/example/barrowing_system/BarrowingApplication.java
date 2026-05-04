package com.example.barrowing_system;

import android.app.Application;
import android.util.Log;

/**
 * Custom Application class to handle Google Play Services errors
 * and suppress them from affecting user experience
 */
public class BarrowingApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set default exception handler to suppress Google Play Services errors
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                // Filter out Google Play Services errors
                if (throwable.getMessage() != null && 
                    throwable.getMessage().contains("Failed to get service from broker")) {
                    Log.w("BarrowingApp", "Suppressed Google Play Services error: " + throwable.getMessage());
                    return; // Don't crash the app
                }
                
                // Handle other exceptions normally
                Log.e("BarrowingApp", "Uncaught exception: " + throwable.getMessage(), throwable);
                System.exit(1);
            }
        });
    }
}
