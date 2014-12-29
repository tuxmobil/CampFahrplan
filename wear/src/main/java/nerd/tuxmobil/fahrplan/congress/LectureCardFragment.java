package nerd.tuxmobil.fahrplan.congress;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
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
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateContentView(inflater, container, savedInstanceState);

        boolean isNow = System.currentTimeMillis() >= lecture.startTime && System.currentTimeMillis() <= lecture.endTime;

        ((TextView) v.findViewById(R.id.title)).setText(isNow ? formatTitleForNow() : formatTitleForNext());
        ((TextView) v.findViewById(R.id.text)).setText(lecture.title);
        return v;
    }

    private CharSequence formatTitleForNow() {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(lecture.room);
        builder.append(" ");

        int length = builder.length();
        String untilStr = App.getContext().getString(R.string.card_running_until_time,
                TIME_FORMATTER.format(lecture.endTime));
        builder.append(untilStr);
        builder.setSpan(new RelativeSizeSpan(0.75F), length, length + untilStr.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private CharSequence formatTitleForNext() {
        boolean isToday = lecture.startTime / 86400000 == System.currentTimeMillis() / 86400000;

        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(lecture.room);
        builder.append(" ");

        int lengthBeforeFormatChange = builder.length();

        if (!isToday) {
            builder.append(App.getContext().getString(R.string.day_x, lecture.day));
            builder.append(", ");
        }

        builder.append(TIME_FORMATTER.format(lecture.startTime));
        builder.setSpan(new RelativeSizeSpan(0.75F), lengthBeforeFormatChange, builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }
}
