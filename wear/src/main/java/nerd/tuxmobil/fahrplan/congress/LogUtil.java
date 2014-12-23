package nerd.tuxmobil.fahrplan.congress;

import android.util.Log;

public class LogUtil {

    private static final String TAG = "CampFahrplan:Wear";

    public static void debug(String message) {
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, message);
//        }
    }

    public static void info(String message) {
//        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, message);
//        }
    }
}
