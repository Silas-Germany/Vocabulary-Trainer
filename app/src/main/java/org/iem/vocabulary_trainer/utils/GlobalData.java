package org.iem.vocabulary_trainer.utils;

import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

public class GlobalData extends Application {
    private static final String LOG_TAG = "VT_" + GlobalData.class.getSimpleName();

    // utils
    public static UtilsContract.Database sDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Init global variables");

        // init classes
        sDatabase = new Database(getApplicationContext());

        // init uncaught exception handler
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.w(LOG_TAG, "Uncaught error: ", e);
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(t, e);
                }
            }
        });

        if (!sDatabase.resetEvaluation()) {
            Log.e(LOG_TAG, "Error reseting data evaluation");
        }
    }

    // returns a minute-exact timestamp according to the system running time - that many minutes
    public static int getTimestamp() {
        return (int) (SystemClock.elapsedRealtime() / 1000 / 60);
    }
}
