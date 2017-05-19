package org.iem.vocabulary_trainer.utils;

import android.app.Application;
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
    }
}
