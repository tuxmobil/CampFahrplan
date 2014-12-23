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

    public static ArrayList<DataMap> buildDataMapListFromLectures(List<Lecture> lectures) {
        ArrayList<DataMap> result = new ArrayList<DataMap>(lectures.size());

        for (Lecture lecture : lectures) {
            DataMap lectureMap = new DataMap();

            lectureMap.putString("title", lecture.title);
            lectureMap.putString("speakers", lecture.speakers);
            lectureMap.putBoolean("highlight", lecture.highlight);
            lectureMap.putLong("start_time", lecture.dateUTC);
            lectureMap.putLong("end_time", lecture.dateUTC + (lecture.duration * 60000));
            lectureMap.putInt("track_color", LectureColorHelper.getBackgroundColor(lecture.track));

            result.add(lectureMap);
        }

        return result;
    }

}
