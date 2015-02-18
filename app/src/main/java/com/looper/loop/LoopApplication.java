package com.looper.loop;

import android.app.Application;
import android.os.Looper;

/**
 * Created by rishabhpugalia on 04/02/15.
 */
public class LoopApplication extends Application {

    public static short SMS_PORT = 6743;
    public static String SERVER_URL = "http://192.168.1.3:3000";

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Keep checking in a background thread whether the user is logged in or not
    }

    public static boolean isMainThread() {
        // return Looper.myLooper() == Looper.getMainLooper();
        return Looper.getMainLooper().getThread() == Thread.currentThread(); // can also do this
    }
}
