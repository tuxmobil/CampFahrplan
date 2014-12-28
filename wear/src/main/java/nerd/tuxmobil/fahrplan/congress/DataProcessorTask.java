package nerd.tuxmobil.fahrplan.congress;

import android.os.AsyncTask;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcessorTask extends AsyncTask<List<DataMap>, Void, DataProcessorTask.ProcessorResult> {

    @Override
    protected ProcessorResult doInBackground(List<DataMap>... lists) {
        List<DataMap> lectures = lists[0];

        List<Lecture> runningLectures = new ArrayList<Lecture>();
        List<Lecture> remainingLectures = new ArrayList<Lecture>();
        List<Lecture> nextHighlightLectures = new ArrayList<Lecture>();
        Map<String, Lecture> nextRoomLectures = new HashMap<String, Lecture>();

        long now = Calendar.getInstance().getTimeInMillis();

        for (DataMap lectureDataMap : lectures) {
            Lecture lecture = Lecture.createFromDataMap(lectureDataMap);

            if (now > lecture.endTime) {
                // skip as we don't display old lectures
                continue;
            }

            LogUtil.debug(String.format("Processor.doInBackground: %1$s: %2$d >= %3$d && %2$d <= %4$d",
                    lecture.title, now, lecture.startTime, lecture.endTime));
            if (now >= lecture.startTime && now <= lecture.endTime) {
                runningLectures.add(lecture);
                continue;
            }

            remainingLectures.add(lecture);
        }

        LectureSortingComparator comparator = new LectureSortingComparator();
        Collections.sort(runningLectures, comparator);
        // sort these now. we circumvent incorrect data (because of ordering) in the lists
        Collections.sort(remainingLectures, comparator);

        for (Lecture lecture : remainingLectures) {
            // running lectures are out of the game by now, so just get the next few highlights
            if (lecture.highlight && nextHighlightLectures.size() < 5) {
                nextHighlightLectures.add(lecture);
            }

            // for each room, get one next lecture in general
            if (!nextRoomLectures.containsKey(lecture.room)) {
                nextRoomLectures.put(lecture.room, lecture);
            }
        }

        // room order has killed the time order, make it correct
        List<Lecture> nextRoomLecturesList = new ArrayList<Lecture>(nextRoomLectures.values());
        Collections.sort(nextRoomLecturesList, comparator);

        return new ProcessorResult(runningLectures, nextHighlightLectures, nextRoomLecturesList);
    }

    private static class LectureSortingComparator implements Comparator<Lecture> {

        @Override
        public int compare(Lecture lhs, Lecture rhs) {
            if (lhs.startTime < rhs.startTime) {
                return -1;
            } else if (lhs.startTime > rhs.startTime) {
                return 1;
            }

            return 0;
        }

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
}
