package nerd.tuxmobil.fahrplan.congress;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WearHelper {

    private static final String TAG = "CampFahrplan:WearHelper";

    public static List<Lecture> filterLectures(List<Lecture> lectures) {
        long now = Calendar.getInstance().getTimeInMillis();

        List<Lecture> results = new ArrayList<Lecture>(lectures.size());
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
            result.add(buildDataMapFromLecture(lecture));
        }

        return result;
    }

    public static DataMap buildDataMapFromLecture(Lecture lecture) {
        DataMap lectureMap = new DataMap();

        lectureMap.putString("id", lecture.lecture_id);
        lectureMap.putString("title", lecture.title);
        lectureMap.putString("speakers", lecture.speakers);
        lectureMap.putString("room", lecture.room);
        lectureMap.putBoolean("highlight", lecture.highlight);
        lectureMap.putLong("start_time", lecture.dateUTC);
        lectureMap.putLong("end_time", lecture.dateUTC + (lecture.duration * 60000));
        lectureMap.putInt("day", lecture.day);
        lectureMap.putInt("track_color", LectureColorHelper.getBackgroundColor(lecture.track));

        return lectureMap;
    }

}
