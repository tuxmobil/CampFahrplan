package nerd.tuxmobil.fahrplan.congress;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;

public class LectureCardFragment extends CardFragment {

    private static DateFormat TIME_FORMATTER = android.text.format.DateFormat.getTimeFormat(App.getContext());

    private Lecture lecture;

    public LectureCardFragment() {

    }

    public static LectureCardFragment create(Lecture lecture) {
        LectureCardFragment fragment = new LectureCardFragment();
        fragment.lecture = lecture;

        return fragment;
    }

    public int getLectureTrackColor() {
        return lecture.trackColor;
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateContentView(inflater, container, savedInstanceState);

        boolean isNow = System.currentTimeMillis() >= lecture.startTime && System.currentTimeMillis() <= lecture.endTime;

        ((TextView) v.findViewById(R.id.title)).setText(isNow ? lecture.room : formatTitleForNext());
        ((TextView) v.findViewById(R.id.text)).setText(lecture.title);
        return v;
    }

    private CharSequence formatTitleForNext() {
        boolean isToday = lecture.startTime / 86400 == System.currentTimeMillis() / 86400;

        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (!isToday) {
            String dayString = App.getContext().getString(R.string.day_x, lecture.day + 1);
            builder.append(dayString);
            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, dayString.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        String timeString = TIME_FORMATTER.format(lecture.startTime);
        int length = builder.length();
        builder.append(timeString);
        builder.setSpan(new StyleSpan(Typeface.BOLD), length, length + timeString.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" ");

        length = builder.length();
        builder.append(lecture.room);
        builder.setSpan(new RelativeSizeSpan(0.75F), length, length + lecture.room.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }
}
