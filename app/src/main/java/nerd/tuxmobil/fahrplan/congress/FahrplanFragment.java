package nerd.tuxmobil.fahrplan.congress;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable;

interface OnRefreshEventMarkers {

    public void refreshEventMarkers();
}

public class FahrplanFragment extends SherlockFragment implements
        OnClickListener,
        OnNavigationListener,
        OnParseCompleteListener {

    private MyApp global;

    private static String LOG_TAG = "Fahrplan";

    private float scale;

    private LayoutInflater inflater;

    private int firstLectureStart = 0;

    private int lastLectureEnd = 0;


    private int mDay = 1;

    private View dayTextView;

    public static Context context = null;

    public static String[] rooms = {
            "Saal 1",
            "Saal 2",
            "Saal G",
            "Saal 6",
            "Saal 17",
            "Lounge"
    };

    public static final String PREFS_NAME = "settings";

    private int screenWidth = 0;

    private Typeface boldCondensed;

    private Typeface light;

    private View contextMenuView;

    private int columnWidth;

    private String lecture_id;        // started with lecture_id

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        boldCondensed = Typeface.createFromAsset(
                getSherlockActivity().getAssets(), "Roboto-BoldCondensed.ttf");
        light = Typeface.createFromAsset(
                getSherlockActivity().getAssets(), "Roboto-Light.ttf");
        context = getSherlockActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        global = (MyApp) getSherlockActivity().getApplicationContext();
        scale = getResources().getDisplayMetrics().density;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        MyApp.LogDebug(LOG_TAG, "screen width = " + screenWidth);
        MyApp.LogDebug(LOG_TAG, "time width " + getResources().getDimension(R.dimen.time_width));
        screenWidth -= getResources().getDimension(R.dimen.time_width);
        int max_cols = HorizontalSnapScrollView.calcMaxCols(getResources(), screenWidth);
        MyApp.LogDebug(LOG_TAG, "max cols: " + max_cols);
        columnWidth = (int) ((float) screenWidth / max_cols); // Width for the row column
        HorizontalScrollView roomScroller =
                (HorizontalScrollView) view.findViewById(R.id.roomScroller);
        if (roomScroller != null) {
            HorizontalSnapScrollView snapScroller =
                    (HorizontalSnapScrollView) view.findViewById(R.id.horizScroller);
            if (snapScroller != null) {
                snapScroller.setChildScroller(roomScroller);
            }
            roomScroller.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }



        SharedPreferences prefs = getSherlockActivity().getSharedPreferences(PREFS_NAME, 0);
        mDay = prefs.getInt("displayDay", 1);

        inflater = (LayoutInflater) getSherlockActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Intent intent = getSherlockActivity().getIntent();
        lecture_id = intent.getStringExtra("lecture_id");

        if (lecture_id != null) {
            MyApp.LogDebug(LOG_TAG, "Open with lecture_id " + lecture_id);
            mDay = intent.getIntExtra("day", mDay);
            MyApp.LogDebug(LOG_TAG, "day " + mDay);
        }

        if (MyApp.numdays > 0) {
            build_navigation_menu();
        }

        switch (MyApp.task_running) {
            case FETCH:
                MyApp.LogDebug(LOG_TAG, "fetch was pending, restart");
                if (MyApp.numdays != 0) {
                    viewDay(false);
                }
                break;
            case PARSE:
                MyApp.LogDebug(LOG_TAG, "parse was pending, restart");
                break;
            case NONE:
                if (MyApp.numdays != 0) {
                    // auf jeden Fall reload, wenn mit Lecture ID gestartet
                    viewDay(lecture_id != null);
                }
                break;
        }

        if (lecture_id != null) {
            scrollTo(lecture_id);
        }
    }

    @Override
    public void onDestroy() {
        MyApp.LogDebug(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (MyApp.fetcher != null) {
            MyApp.fetcher.setListener(null);
        }
        if (MyApp.parser != null) {
            MyApp.parser.setListener(null);
        }
    }

    @Override
    public void onResume() {
        MyApp.LogDebug(LOG_TAG, "onResume");
        super.onResume();
        fillTimes();
        getSherlockActivity().supportInvalidateOptionsMenu();
    }

    private void viewDay(boolean reload) {
        // Log.d(LOG_TAG, "viewDay(" + reload + ")");

        if (loadLectureList(getSherlockActivity(), mDay, reload) == false) {
            MyApp.LogDebug(LOG_TAG, "fetch on loading empty lecture list");
            // FIXME
            // fetchFahrplan();
        }
        scanDayLectures();
        HorizontalSnapScrollView scroller =
                (HorizontalSnapScrollView) getView().findViewById(R.id.horizScroller);
        if (scroller != null) {
            scroller.scrollTo(0, 0);
        }
        HorizontalScrollView roomScroller =
                (HorizontalScrollView) getView().findViewById(R.id.roomScroller);
        if (scroller != null) {
            addRoomColumns(scroller);
        }
        if (roomScroller != null) {
            addRoomTitleViews(roomScroller);
        }

        for (int i = 0; i < MyApp.room_count; i++) {
            fillRoom((ViewGroup) scroller.getChildAt(0), i);
        }
        scrollToCurrent(mDay);
        ActionBar actionbar = getSherlockActivity().getSupportActionBar();
        if (actionbar != null) {
            actionbar.setSelectedNavigationItem(mDay - 1);
        }
    }

    private void addRoomColumns(HorizontalSnapScrollView scroller) {
        LinearLayout root = (LinearLayout) scroller.getChildAt(0);
        root.removeAllViews();
        if (scroller.getColumnWidth() != 0) {
            // update pre-calculated width with actual layout
            columnWidth = scroller.getColumnWidth();
        }
        for (int i = 0; i < MyApp.room_count; i++) {
            LinearLayout roomLayout = new LinearLayout(context);
            LinearLayout.LayoutParams p = new LayoutParams(
                    columnWidth, LayoutParams.MATCH_PARENT, 1);
            roomLayout.setOrientation(LinearLayout.VERTICAL);
            roomLayout.setLayoutParams(p);
            root.addView(roomLayout);
        }
    }

    private void addRoomTitleViews(HorizontalScrollView scroller) {
        LinearLayout root = (LinearLayout) scroller.getChildAt(0);
        root.removeAllViews();
        Set<Entry<String, Integer>> roomTitleSet = MyApp.roomsMap.entrySet();
        int textSize = getResources().getInteger(R.integer.room_title_size);
        for (int i = 0; i < MyApp.room_count; i++) {
            TextView roomTitle = new TextView(context);
            LinearLayout.LayoutParams p = new LayoutParams(
                    columnWidth, LayoutParams.WRAP_CONTENT, 1);
            p.gravity = Gravity.CENTER;
            roomTitle.setLayoutParams(p);
            roomTitle.setGravity(Gravity.CENTER);
            roomTitle.setTypeface(light);
            int v = MyApp.roomList.get(i);
            for (Entry<String, Integer> entry : roomTitleSet) {
                if (entry.getValue() == v) {
                    roomTitle.setText(entry.getKey());
                    break;
                }
            }
            roomTitle.setTextColor(0xffffffff);
            roomTitle.setTextSize(textSize);
            root.addView(roomTitle);
        }
    }

    /**
     * jump to current time or lecture, if we are on today's lecture list
     */
    private void scrollToCurrent(int day) {
        int height;
        // Log.d(LOG_TAG, "lectureListDay: " + MyApp.lectureListDay);
        if (lecture_id != null) {
            return;
        }
        if (MyApp.lectureListDay != MyApp.dateInfos.getIndexOfToday(
                MyApp.dayChangeHour, MyApp.dayChangeMinute)) {
            return;
        }
        Time now = new Time();
        now.setToNow();
        HorizontalSnapScrollView horiz = null;

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
            default:
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                horiz = (HorizontalSnapScrollView) getView().findViewById(R.id.horizScroller);
                break;
        }

        int col = -1;
        if (horiz != null) {
            col = horiz.getColumn();
            MyApp.LogDebug(LOG_TAG, "y pos  = " + col);
        }
        int time = firstLectureStart;
        int printTime = time;
        int scrollAmount = 0;

        if (!((((now.hour * 60) + now.minute) < firstLectureStart) &&
                MyApp.dateInfos.sameDay(now, MyApp.lectureListDay))) {
            while (time < lastLectureEnd) {
                int hour = printTime / 60;
                int minute = printTime % 60;
                if ((now.hour == hour) && (now.minute >= minute)
                        && (now.minute < (minute + 15))) {
                    break;
                } else {
                    scrollAmount += (height * 3);
                }
                time += 15;
                printTime = time;
                if (printTime >= (24 * 60)) {
                    printTime -= (24 * 60);
                }
            }

            for (Lecture l : MyApp.lectureList) {
                if ((l.day == day) && (l.startTime <= time) && (l.startTime + l.duration > time)) {
                    if ((col == -1) || ((col >= 0) && (l.room_index == MyApp.roomList.get(col)))) {
                        MyApp.LogDebug(LOG_TAG, l.title);
                        MyApp.LogDebug(LOG_TAG, time + " " + l.startTime + "/" + l.duration);
                        scrollAmount -= ((time - l.startTime) / 5) * height;
                        time = l.startTime;
                    }
                }
            }
        } else {
            // Log.d(LOG_TAG, "we are before " + firstLectureStart + " " + ((now.hour * 60) + now.minute));
        }

        // Log.d(LOG_TAG, "scrolltoCurrent to " + scrollAmount);

        final int pos = scrollAmount;
        final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.scrollView1);
        scrollView.scrollTo(0, scrollAmount);
        scrollView.post(new Runnable() {

            @Override
            public void run() {
                scrollView.scrollTo(0, pos);
            }
        });
    }

    private void setBell(Lecture lecture) {
        ScrollView parent = (ScrollView) getView().findViewById(R.id.scrollView1);
        if (parent == null) {
            return;
        }
        View v = parent.findViewWithTag(lecture);
        if (v == null) {
            return;
        }
        ImageView bell = (ImageView) v.findViewById(R.id.bell);
        if (bell == null) {
            return;
        }

        if (lecture.has_alarm) {
            bell.setVisibility(View.VISIBLE);
        } else {
            bell.setVisibility(View.GONE);
        }
    }

    private void scrollTo(String lecture_id) {
        int height;
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                MyApp.LogDebug(LOG_TAG, "landscape");
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
            default:
                MyApp.LogDebug(LOG_TAG, "other orientation");
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
        }
        for (Lecture lecture : MyApp.lectureList) {
            if (lecture_id.equals(lecture.lecture_id)) {
                final ScrollView parent = (ScrollView) getView().findViewById(R.id.scrollView1);
                final int pos = (lecture.relStartTime - firstLectureStart) / 5 * height;
                MyApp.LogDebug(LOG_TAG, "position is " + pos);
                parent.post(new Runnable() {

                    @Override
                    public void run() {
                        parent.scrollTo(0, pos);
                    }
                });
                final HorizontalSnapScrollView horiz =
                        (HorizontalSnapScrollView) getView().findViewById(R.id.horizScroller);
                if (horiz != null) {
                    final int hpos = MyApp.roomList.keyAt(
                            MyApp.roomList.indexOfValue(lecture.room_index));
                    MyApp.LogDebug(LOG_TAG, "scroll horiz to " + hpos);
                    horiz.post(new Runnable() {

                        @Override
                        public void run() {
                            horiz.scrollToColumn(hpos, false);
                        }
                    });
                }
                break;
            }
        }
    }

    private void chooseDay(int chosenDay) {
        if ((chosenDay + 1) != mDay) {
            mDay = chosenDay + 1;
            SharedPreferences settings = getSherlockActivity().getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("displayDay", mDay);
            editor.commit();
            viewDay(true);
            fillTimes();
        }
    }

    private int minutesOfDay(long dateUTC) {
        Time t = new Time();
        t.set(dateUTC);
        return (t.hour * 60) + t.minute;
    }

    private void scanDayLectures() {
        Lecture l = MyApp.lectureList.get(0); // they are already sorted
        long end = 0;
        if (l.dateUTC > 0) {
            firstLectureStart = minutesOfDay(l.dateUTC);
        } else {
            firstLectureStart = l.relStartTime;
        }
        lastLectureEnd = -1;
        for (Lecture lecture : MyApp.lectureList) {
            if (l.dateUTC > 0) {
                if (end == 0) {
                    end = lecture.dateUTC + (lecture.duration * 60000);
                } else if ((lecture.dateUTC + (lecture.duration * 60000)) > end) {
                    end = lecture.dateUTC + (lecture.duration * 60000);
                }
            } else {
                if (lastLectureEnd == -1) {
                    lastLectureEnd = lecture.relStartTime + lecture.duration;
                } else if ((lecture.relStartTime + lecture.duration) > lastLectureEnd) {
                    lastLectureEnd = lecture.relStartTime + lecture.duration;
                }
            }
        }
        if (end > 0) {
            lastLectureEnd = minutesOfDay(end);
            if (lastLectureEnd < firstLectureStart) {
                lastLectureEnd += (24 * 60);
            }
        }
        MyApp.LogDebug(LOG_TAG, "firstLectureStart=" + firstLectureStart);
        MyApp.LogDebug(LOG_TAG, "lastLectureEnd=" + lastLectureEnd);
    }

    private void fillTimes() {
        int time = firstLectureStart;
        int printTime = time;
        LinearLayout timeSpalte = (LinearLayout) getView().findViewById(R.id.times_layout);
        timeSpalte.removeAllViews();
        int height;
        Time now = new Time();
        now.setToNow();
        View event;

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                MyApp.LogDebug(LOG_TAG, "landscape");
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
            default:
                MyApp.LogDebug(LOG_TAG, "other orientation");
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
        }

        while (time < lastLectureEnd) {
            StringBuilder sb = new StringBuilder();
            int hour = printTime / 60;
            int minute = printTime % 60;
            sb.append(String.format("%02d", hour)).append(":");
            sb.append(String.format("%02d", minute));
            if ((now.hour == hour) && (now.minute >= minute)
                    && (now.minute < (minute + 15))) {
                event = inflater.inflate(R.layout.time_layout_now, null);
            } else {
                event = inflater.inflate(R.layout.time_layout, null);
            }
            timeSpalte.addView(event, LayoutParams.MATCH_PARENT, height * 3);
            TextView title = (TextView) event.findViewById(R.id.time);
            title.setText(sb.toString());
            time += 15;
            printTime = time;
            if (printTime >= (24 * 60)) {
                printTime -= (24 * 60);
            }
        }
    }

    private int getEventPadding() {
        int padding;
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                padding = (int) (8 * scale);
                break;
            default:
                padding = (int) (10 * scale);
                break;
        }
        return padding;
    }

    private void setLectureBackground(Lecture lecture, View view) {
        Integer drawable;
        int padding = getEventPadding();
        drawable = LectureColorHelper.getBackgroundDrawableId(lecture.track, lecture.highlight);
        if (drawable != null) {
            view.setBackgroundResource(drawable);
            view.setPadding(padding, padding, padding, padding);
        }
    }

    private void fillRoom(ViewGroup root, int roomIdx) {
        LinearLayout room = (LinearLayout) root.getChildAt(roomIdx);
        room.removeAllViews();
        int endTime = firstLectureStart;
        int padding = getEventPadding();
        int standardHeight;
        int startTime;
        int room_index = MyApp.roomList.get(roomIdx);
        View event = null;
        int margin;

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                MyApp.LogDebug(LOG_TAG, "landscape");
                standardHeight = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
            default:
                MyApp.LogDebug(LOG_TAG, "other orientation");
                standardHeight = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
        }
        for (int idx = 0; idx < MyApp.lectureList.size(); idx++) {
            Lecture lecture = MyApp.lectureList.get(idx);
            if (lecture.room_index == room_index) {
                if (lecture.dateUTC > 0) {
                    startTime = minutesOfDay(lecture.dateUTC);
                    if (startTime < endTime) {
                        startTime += (24 * 60);
                    }
                } else {
                    startTime = lecture.relStartTime;
                }
                if (startTime > endTime) {
                    margin = (int) (standardHeight * (startTime - endTime) / 5);
                    if (event != null) {
                        LayoutParams lp = (LayoutParams) event.getLayoutParams();
                        lp.bottomMargin = margin;
                        event.setLayoutParams(lp);
                        margin = 0;
                    }
                } else {
                    margin = 0;
                }

                // fix overlapping events
                Lecture next = null;
                for (int next_idx = idx + 1; next_idx < MyApp.lectureList.size(); next_idx++) {
                    next = MyApp.lectureList.get(next_idx);
                    if (next.room_index == room_index) {
                        break;
                    }
                    next = null;
                }
                if (next != null) {
                    if (next.dateUTC > 0) {
                        if (lecture.dateUTC + (lecture.duration * 60000) > next.dateUTC) {
                            MyApp.LogDebug(LOG_TAG, lecture.title + " collides with " + next.title);
                            lecture.duration = (int) ((next.dateUTC - lecture.dateUTC) / 60000);
                        }
                    }
                }

                event = inflater.inflate(R.layout.event_layout, null);
                int height = (int) (standardHeight * (lecture.duration / 5));
                ImageView bell = (ImageView) event.findViewById(R.id.bell);
                if (lecture.has_alarm) {
                    bell.setVisibility(View.VISIBLE);
                } else {
                    bell.setVisibility(View.GONE);
                }
                room.addView(event, LayoutParams.MATCH_PARENT, height);
                LayoutParams lp = (LayoutParams) event.getLayoutParams();
                lp.topMargin = margin;
                event.setLayoutParams(lp);
                TextView title = (TextView) event.findViewById(R.id.event_title);
                title.setTypeface(boldCondensed);
                title.setText(lecture.title);
                title = (TextView) event.findViewById(R.id.event_subtitle);
                title.setText(lecture.subtitle);
                title = (TextView) event.findViewById(R.id.event_speakers);
                title.setText(lecture.speakers.replaceAll(";", ", "));
                title = (TextView) event.findViewById(R.id.event_track);
                StringBuilder sb = new StringBuilder();
                sb.append(lecture.track);
                if ((lecture.lang != null) && (lecture.lang.length() > 0)) {
                    sb.append(" [").append(lecture.lang).append("]");
                }
                title.setText(sb.toString());
                View recordingOptOut = event.findViewById(R.id.novideo);
                if (recordingOptOut != null) {
                    recordingOptOut.setVisibility(
                            lecture.recordingOptOut ? View.VISIBLE : View.GONE);
                }

                setLectureBackground(lecture, event);
                event.setOnClickListener(this);
                event.setLongClickable(true);
                // event.setOnLongClickListener(this);
                event.setOnCreateContextMenuListener(this);
                event.setTag(lecture);
                endTime = startTime + lecture.duration;
            }
        }
    }

    public static boolean loadLectureList(Context context, int day, boolean force) {
        MyApp.LogDebug(LOG_TAG, "load lectures of day " + day);

        if ((force == false) && (MyApp.lectureList != null) && (MyApp.lectureListDay == day)) {
            return true;
        }

        MyApp.lectureList = FahrplanMisc.loadLecturesForDayIndex(context, day);
        if (MyApp.lectureList == null) return false;

        int lectureIndex = MyApp.lectureList.size() - 1;
        while (lectureIndex >= 0) {
            Lecture l = MyApp.lectureList.get(lectureIndex);
            if (l.changedIsCanceled) MyApp.lectureList.remove(lectureIndex);
            lectureIndex--;
        }
        MyApp.lectureListDay = day;

        MyApp.roomsMap.clear();
        MyApp.roomList.clear();
        for (Lecture lecture : MyApp.lectureList) {
            if (!MyApp.roomsMap.containsKey(lecture.room)) {
                if (!MyApp.roomsMap.containsValue(lecture.room_index)) {
                    MyApp.roomsMap.put(lecture.room, lecture.room_index);
                } else {
                    // upgrade from DB without room_index
                    int new_index;
                    for (new_index = 0; new_index < rooms.length; new_index++) {
                        if (lecture.room.equals(rooms[new_index])) {
                            break;
                        }
                    }
                    if (new_index == rooms.length) {
                        new_index = 0;
                        while (MyApp.roomsMap.containsValue(new_index)) {
                            new_index++;
                        }
                    }
                    MyApp.roomsMap.put(lecture.room, new_index);
                    MyApp.LogDebug(LOG_TAG,
                            "Upgrade room " + lecture.room + " to index " + new_index);
                    lecture.room_index = new_index;
                }
            }
            // upgrade
            if (lecture.room_index == 0) {
                lecture.room_index = MyApp.roomsMap.get(lecture.room);
            }
        }
        MyApp.room_count = MyApp.roomsMap.size();
        MyApp.LogDebug(LOG_TAG, "room count = " + MyApp.room_count);
        List<Integer> rooms = new ArrayList<Integer>(MyApp.roomsMap.values());
        Collections.sort(rooms);
        int k = 0;
        for (Integer v : rooms) {
            MyApp.LogDebug(LOG_TAG, "room column " + k + " is room " + v);
            MyApp.roomList.append(k, v);
            k++;
        }

        if ((MyApp.lectureList.size() > 0) && (MyApp.lectureList.get(0).dateUTC > 0)) {
            Collections.sort(MyApp.lectureList, new Comparator<Lecture>() {

                @Override
                public int compare(Lecture lhs, Lecture rhs) {
                    if (lhs.dateUTC < rhs.dateUTC) {
                        return -1;
                    }
                    if (lhs.dateUTC > rhs.dateUTC) {
                        return 1;
                    }
                    return 0;
                }

            });
        }

        loadAlarms(context);

        return true;
    }

    public static void loadAlarms(Context context) {
        if (MyApp.lectureList == null) {
            return;
        }

        Cursor alarmCursor;
        SQLiteDatabase alarmdb = null;

        for (Lecture lecture : MyApp.lectureList) {
            lecture.has_alarm = false;
        }

        AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(context);
        alarmdb = alarmDB.getReadableDatabase();

        try {
            alarmCursor = alarmdb.query(
                    AlarmsTable.NAME,
                    AlarmsDBOpenHelper.allcolumns,
                    null, null, null,
                    null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            alarmdb.close();
            alarmdb = null;
            alarmDB.close();
            return;
        }
        MyApp.LogDebug(LOG_TAG, "Got " + alarmCursor.getCount() + " alarm rows.");

        alarmCursor.moveToFirst();
        while (!alarmCursor.isAfterLast()) {
            String lecture_id = alarmCursor.getString(
                    alarmCursor.getColumnIndex(AlarmsTable.Columns.EVENT_ID));
            MyApp.LogDebug(LOG_TAG, "lecture " + lecture_id + " has alarm");

            for (Lecture lecture : MyApp.lectureList) {
                if (lecture.lecture_id.equals(lecture_id)) {
                    lecture.has_alarm = true;
                }
            }
            alarmCursor.moveToNext();
        }
        alarmCursor.close();
        alarmdb.close();
        alarmDB.close();
    }

    @Override
    public void onClick(View v) {
        Lecture lecture = (Lecture) v.getTag();
        MyApp.LogDebug(LOG_TAG, "Click on " + lecture.title);
        MainActivity main = (MainActivity) getSherlockActivity();
        if (main != null) {
            main.openLectureDetail(lecture, mDay);
        }
    }

    public void build_navigation_menu() {
        Time now = new Time();
        now.setToNow();
        StringBuilder currentDate = new StringBuilder();
        currentDate.append(String.format("%d", now.year));
        currentDate.append("-");
        currentDate.append(String.format("%02d", now.month + 1));
        currentDate.append("-");
        currentDate.append(String.format("%02d", now.monthDay));

        MyApp.LogDebug(LOG_TAG, "today is " + currentDate.toString());

        String[] days_menu = new String[MyApp.numdays];
        for (int i = 0; i < MyApp.numdays; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.day)).append(" ").append(i + 1);
            for (DateInfo dateInfo : MyApp.dateInfos) {
                if (dateInfo.dayIdx == (i + 1)) {
                    MyApp.LogDebug(LOG_TAG, "DateInfo of day '" + sb.toString() + "': " + dateInfo);
                    if (currentDate.toString().equals(dateInfo.date)) {
                        sb.append(" - ");
                        sb.append(getString(R.string.today));
                    }
                    break;
                }
            }
            days_menu[i] = sb.toString();
        }
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getSherlockActivity(),
                R.layout.sherlock_spinner_dropdown_item, days_menu);
        arrayAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(arrayAdapter, this);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    public void onParseDone(Boolean result, String version) {
        if (result) {
            if ((MyApp.numdays == 0) || (!version.equals(MyApp.version))) {
                FahrplanMisc.loadMeta(getSherlockActivity());
                FahrplanMisc.loadDays(getSherlockActivity());
                build_navigation_menu();
                SharedPreferences prefs = getSherlockActivity().getSharedPreferences(PREFS_NAME, 0);
                mDay = prefs.getInt("displayDay", 1);
                if (mDay > MyApp.numdays) {
                    mDay = 1;
                }
                viewDay(true);
                fillTimes();
                final Toast done = Toast.makeText(
                        global.getApplicationContext(),
                        String.format(getString(R.string.aktualisiert_auf), version),
                        Toast.LENGTH_LONG);
                done.show();
            } else {
                viewDay(false);
            }
        } else {
            Toast.makeText(
                    global.getApplicationContext(),
                    getParsingErrorMessage(version),
                    Toast.LENGTH_LONG).show();
        }
        getSherlockActivity().supportInvalidateOptionsMenu();
    }

    private String getParsingErrorMessage(final String version) {
        if (version == null || version.length() < 1) {
            return getString(R.string.parsing_error_generic);
        } else {
            return getString(R.string.parsing_error_with_version, version);
        }
    }

    void getAlarmTimeDialog(final Lecture lecture) {

        LayoutInflater inflater = (LayoutInflater) getSherlockActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reminder_dialog,
                (ViewGroup) getView().findViewById(R.id.layout_root));

        final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getSherlockActivity(),
                R.array.alarm_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        TextView msg = (TextView) layout.findViewById(R.id.message);
        msg.setText(R.string.choose_alarm_time);

        new AlertDialog.Builder(getSherlockActivity()).setTitle(R.string.setup_alarm)
                .setView(layout)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                int alarm = spinner.getSelectedItemPosition();
                                MyApp.LogDebug(LOG_TAG, "alarm chosen: " + alarm);
                                FahrplanMisc.addAlarm(getSherlockActivity(), lecture, alarm);
                                setBell(lecture);
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        int menuItemIndex = item.getItemId();
        Lecture lecture = (Lecture) contextMenuView.getTag();

        MyApp.LogDebug(LOG_TAG, "clicked on " + ((Lecture) contextMenuView.getTag()).lecture_id);

        switch (menuItemIndex) {
            case 0:
                if (lecture.highlight) {
                    lecture.highlight = false;
                    FahrplanMisc.writeHighlight(getSherlockActivity(), lecture);
                } else {
                    lecture.highlight = true;
                    FahrplanMisc.writeHighlight(getSherlockActivity(), lecture);
                }
                setLectureBackground(lecture, contextMenuView);
                break;
            case 1:
                getAlarmTimeDialog(lecture);
                break;
            case 2:
                FahrplanMisc.deleteAlarm(getSherlockActivity(), lecture);
                setBell(lecture);
                break;
            case 3:
                FahrplanMisc.addToCalender(getSherlockActivity(), lecture);
                break;
            case 4:
                FahrplanMisc.share(getSherlockActivity(), lecture);
                break;
        }
        return true;
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        contextMenuView = v;
        Lecture lecture = (Lecture) v.getTag();
        if (lecture.highlight) {
            menu.add(0, 0, 0, getString(R.string.unflag_as_favorite));
        } else {
            menu.add(0, 0, 0, getString(R.string.flag_as_favorite));
        }
        if (lecture.has_alarm) {
            menu.add(0, 2, 2, getString(R.string.delete_alarm));
        } else {
            menu.add(0, 1, 1, getString(R.string.set_alarm));
        }
        if (Build.VERSION.SDK_INT >= 14) {
            menu.add(0, 3, 3, getString(R.string.addToCalendar));
        }
        menu.add(0, 4, 4, getString(R.string.share));
    }

    private View getLectureView(Lecture lecture) {
        ScrollView parent = (ScrollView) getView().findViewById(R.id.scrollView1);
        if (parent == null) {
            return null;
        }
        View v = parent.findViewWithTag(lecture);
        return v;
    }

    public void refreshViews() {
        if (MyApp.lectureList == null) {
            return;
        }
        for (Lecture lecture : MyApp.lectureList) {
            setBell(lecture);
            View v = getLectureView(lecture);
            if (v != null) {
                setLectureBackground(lecture, v);
            }
        }
    }

    public void refreshEventMarkers() {
        MyApp.LogDebug(LOG_TAG, "Reload alarms");
        loadAlarms(getSherlockActivity());
        refreshViews();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (itemPosition < MyApp.numdays) {
            chooseDay(itemPosition);
            return true;
        }
        return false;
    }

}