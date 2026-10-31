package com.svu.firstaidhero;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.svu.firstaidhero.ui.MainActivity;

/**
 * App
 * ---
 * Custom Application class.
 * Installs a global uncaught exception handler to prevent sudden app crashes
 * and gracefully restart the main screen instead (Zero Crashes policy).
 */
public class App extends Application {

    private static final String TAG = "FirstAidHero";

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the global crash handler when the app starts
        installCrashHandler();
    }

    /**
     * Sets up a global UncaughtExceptionHandler.
     * If an unexpected exception occurs anywhere in the app, this handler intercepts it,
     * logs the error, and attempts to restart the MainActivity rather than letting the app close abruptly.
     */
    private void installCrashHandler() {
        // Keep a reference to the default handler in case we need to fallback
        final Thread.UncaughtExceptionHandler defaultHandler =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Log the exception for debugging purposes
            Log.e(TAG, "Uncaught exception on thread: " + thread.getName(), throwable);

            try {
                // Attempt to restart the MainActivity instead of closing the app
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                // Clear the activity stack so the user starts fresh
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } catch (Exception ignored) {
                // If even the restart fails, delegate to the default system crash handler
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
            } finally {
                // Terminate the current process after preparing the restart
                // to ensure a clean state
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
    }
}