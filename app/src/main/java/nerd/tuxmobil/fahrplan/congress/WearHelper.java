package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WearHelper {

    private static final String TAG = "CampFahrplan:WearHelper";

    public static List<Lecture> filterLectures(List<Lecture> lectures) {
        long now = Calendar.getInstance().getTimeInMillis();

        List<Lecture> results = new ArrayList<Lecture>();
        for (Lecture lecture : lectures) {
            // has the lecture already ended?
            if (now > lecture.dateUTC + (lecture.duration * 60000)) {
                continue;
            }

            results.add(lecture);
        }

        return results;
    }

    public static String[] buildArrayFromLectures(List<Lecture> lectures) {
        String[] results = new String[lectures.size()];
        int indexCounter = 0;

        for (Lecture lecture : lectures) {
            JSONObject lectureAsJson = new JSONObject();
            try {
                lectureAsJson.put("title", lecture.title);
                lectureAsJson.put("speakers", lecture.speakers);
                lectureAsJson.put("highlight", lecture.highlight);
                lectureAsJson.put("start_time", lecture.dateUTC);
                lectureAsJson.put("end_time", lecture.dateUTC + (lecture.duration * 60000));
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

    public static List<DataMap> buildDataMapListFromLectures(List<Lecture> lectures) {
        List<DataMap> result = new ArrayList<DataMap>(lectures.size());

        for (Lecture lecture : lectures) {
            DataMap lectureMap = new DataMap();

            lectureMap.putString("title", lecture.title);
            lectureMap.putString("speakers", lecture.speakers);
            lectureMap.putBoolean("highlight", lecture.highlight);
            lectureMap.putLong("start_time", lecture.dateUTC);
            lectureMap.putLong("end_time", lecture.dateUTC + (lecture.duration * 60000));
            lectureMap.putString("room", lecture.room);
            lectureMap.putInt("room_index", lecture.room_index);

            result.add(lectureMap);
        }

        return result;
    }

}
