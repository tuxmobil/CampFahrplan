package nerd.tuxmobil.fahrplan.congress;

import android.os.AsyncTask;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcessorTask extends AsyncTask<ArrayList<DataMap>, Void, DataProcessorTask.ProcessorResult> {

    @Override
    protected ProcessorResult doInBackground(ArrayList<DataMap>... lists) {
        ArrayList<DataMap> lectures = lists[0];
        List<Lecture> runningLectures = new ArrayList<Lecture>();
        List<Lecture> nextHighlightLectures = new ArrayList<Lecture>();
        Map<String, Lecture> nextRoomLectures = new HashMap<String, Lecture>();

        long now = Calendar.getInstance().getTimeInMillis();

        for (DataMap lectureDataMap : lectures) {
            Lecture lecture = Lecture.createFromDataMap(lectureDataMap);
            if (now > lecture.endTime) {
                // skip as we don't display old lectures
                continue;
            }

            if (now >= lecture.startTime && now <= lecture.endTime) {
                runningLectures.add(lecture);
                continue;
            }

            // running lectures are out of the game by now, all others are sorted by time
            // so just get the next few highlights
            if (lecture.highlight && nextHighlightLectures.size() < 5) {
                nextHighlightLectures.add(lecture);
            }

            // for each room, get one next lecture in general
            if (!nextRoomLectures.containsKey(lecture.room)) {
                nextRoomLectures.put(lecture.room, lecture);
            }
        }

        return new ProcessorResult(runningLectures, nextHighlightLectures,
                new ArrayList<Lecture>(nextRoomLectures.values()));
    }

    public static class ProcessorResult {

        public List<Lecture> now;

        public List<Lecture> nextHighlights;

        public List<Lecture> nextAllRooms;

        public ProcessorResult(List<Lecture> now, List<Lecture> nextHighlights, List<Lecture> nextAllRooms) {
            this.now = now;
            this.nextHighlights = nextHighlights;
            this.nextAllRooms = nextAllRooms;
        }
    }

    public static class Lecture {

        public String title;

        public String speakers;

        public String room;

        public boolean highlight;

        public long startTime;

        public long endTime;

        public int trackColor;

        public static Lecture createFromDataMap(DataMap map) {
            Lecture lecture = new Lecture();

            lecture.title = map.getString("title");
            lecture.speakers = map.getString("speakers");
            lecture.room = map.getString("room");
            lecture.highlight = map.getBoolean("highlight");
            lecture.startTime = map.getLong("start_time");
            lecture.endTime = map.getLong("end_time");
            lecture.trackColor = map.getInt("track_color");

            return lecture;
        }

    }
}
