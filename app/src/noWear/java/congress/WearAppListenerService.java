package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.util.Log;

/**
 * Stub class, doing nothing in this configuration
 */
public class WearAppListenerService {

    private static final String TAG = "CampFahrplan:WearAppListenerService";

    public static void requestDeleteAllLectureData(Context context) {
        Log.d(TAG, "requestDeleteAllLectureData: stub");
    }

    public static void requestRefreshSingleLectureData(Context context, Lecture lecture) {
        Log.d(TAG, "requestRefreshSingleLectureData: stub");
    }

    public static void requestRefreshLectureData(Context context, LectureList lectureList) {
        Log.d(TAG, "requestRefreshLectureData: stub");
    }

}
