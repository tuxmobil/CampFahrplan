package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WearHelper {

    private static final String TAG = "CampFahrplan:WearHelper";

    public static String[] getLectures() {
        List<Lecture> lectures = MyApp.lectureList;
        if (lectures == null) {
            Log.w(TAG, "no lectures found");
            return null;
        }

        // filter out lectures which are completed
        // nevertheless, the wear app still has to do filtering then (but just on a reduced dataset)
        return buildArrayFromLectures(filterLectures(lectures));
    }

    private static List<Lecture> filterLectures(List<Lecture> lectures) {
        List<Lecture> results = new ArrayList<Lecture>();
        for (Lecture lecture : lectures) {
            // TODO implement something here
            results.add(lecture);
        }

        return results;
    }

    private static String[] buildArrayFromLectures(List<Lecture> lectures) {
        String[] results = new String[lectures.size()];
        int indexCounter = 0;

        for (Lecture lecture : lectures) {
            JSONObject lectureAsJson = new JSONObject();
            try {
                lectureAsJson.put("title", lecture.title);
                lectureAsJson.put("speakers", lecture.speakers);
                lectureAsJson.put("highlight", lecture.highlight);
                lectureAsJson.put("start_time", lecture.relStartTime);
                lectureAsJson.put("end_time", lecture.relStartTime + lecture.duration);
                lectureAsJson.put("room", lecture.room);
                lectureAsJson.put("room_index", lecture.room_index);

                results[indexCounter] = lectureAsJson.toString();
                ++indexCounter;
            } catch (JSONException e) {
                Log.e(TAG, "failed building array from lectures (current: " + lecture.lecture_id
                        + ")", e);
            }
        }

        return results;
    }

}
