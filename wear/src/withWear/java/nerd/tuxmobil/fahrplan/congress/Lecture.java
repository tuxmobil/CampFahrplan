package nerd.tuxmobil.fahrplan.congress;

import com.google.android.gms.wearable.DataMap;

public class Lecture {

    public String title;

    public String speakers;

    public String room;

    public boolean highlight;

    public long startTime;

    public long endTime;

    public int day;

    public int trackColor;

    public static Lecture createFromDataMap(DataMap map) {
        Lecture lecture = new Lecture();

        lecture.title = map.getString("title");
        lecture.speakers = map.getString("speakers");
        lecture.room = map.getString("room");
        lecture.highlight = map.getBoolean("highlight", false);
        lecture.startTime = map.getLong("start_time");
        lecture.endTime = map.getLong("end_time");
        lecture.day = map.getInt("day");
        lecture.trackColor = map.getInt("track_color");

        return lecture;
    }

}