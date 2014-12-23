package nerd.tuxmobil.fahrplan.congress;

import android.support.wearable.view.CardFragment;

public class LectureCardFragment extends CardFragment {

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
}
