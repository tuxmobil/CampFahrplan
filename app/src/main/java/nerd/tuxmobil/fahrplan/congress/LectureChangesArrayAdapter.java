package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.graphics.Paint;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

public class LectureChangesArrayAdapter extends ArrayAdapter<Lecture> {

    private final Context context;
    private final List<Lecture> list;
    private TreeSet<Integer> mSeparatorsSet;
    private ArrayList<String> mSeperatorStrings;
    private ArrayList<Integer> mMapper;
    private final static int TYPE_ITEM = 0;
    private final static int TYPE_SEPARATOR = 1;
    private final static int NUM_VIEW_TYPES = 2;

    public LectureChangesArrayAdapter(Context context, List<Lecture> list) {
        super(context, R.layout.lecture_change_row, list);
        this.context = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light);
        this.list = list;
        initMapper();
    }

    private void resetTextStyle(TextView textView, int style) {
        textView.setTextAppearance(context, style);
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setTextStyleChanged(TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.schedule_change));
    }

    private void setTextStyleNew(TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.schedule_change_new));
    }

    private void setTextStyleCanceled(TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.schedule_change_canceled));
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        ViewHolder viewHolder = null;
        ViewHolderSeperator viewHolderSeperator = null;

        int type = getItemViewType(position);

        if (convertView == null) {

            // clone the inflater using the ContextThemeWrapper
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LayoutInflater localInflater = inflater.cloneInContext(context);

            switch (type) {
                case TYPE_ITEM:
                    rowView = localInflater.inflate(R.layout.lecture_change_row, parent, false);
                    viewHolder = new ViewHolder();

                    viewHolder.title = (TextView) rowView.findViewById(R.id.title);
                    viewHolder.subtitle = (TextView) rowView.findViewById(R.id.subtitle);
                    viewHolder.speakers = (TextView) rowView.findViewById(R.id.speakers);
                    viewHolder.lang = (TextView) rowView.findViewById(R.id.lang);
                    viewHolder.day = (TextView) rowView.findViewById(R.id.day);
                    viewHolder.time = (TextView) rowView.findViewById(R.id.time);
                    viewHolder.room = (TextView) rowView.findViewById(R.id.room);
                    viewHolder.duration = (TextView) rowView.findViewById(R.id.duration);
                    viewHolder.video = (ImageView) rowView.findViewById(R.id.video);
                    viewHolder.novideo = (ImageView) rowView.findViewById(R.id.no_video);
                    rowView.setTag(viewHolder);
                    break;
                case TYPE_SEPARATOR:
                    rowView = localInflater.inflate(R.layout.lecture_list_seperator, parent, false);
                    viewHolderSeperator = new ViewHolderSeperator();
                    viewHolderSeperator.text = (TextView) rowView.findViewById(R.id.title);
                    rowView.setTag(viewHolderSeperator);
                    break;
            }
        } else {
            rowView = convertView;
            switch (type) {
                case TYPE_ITEM:
                    viewHolder = (ViewHolder) rowView.getTag();
                    break;
                case TYPE_SEPARATOR:
                    viewHolderSeperator = (ViewHolderSeperator) rowView.getTag();
                    break;
            }
        }

        switch (type) {
            case TYPE_ITEM:
                DateFormat df = SimpleDateFormat
                        .getDateInstance(SimpleDateFormat.SHORT);
                DateFormat tf = SimpleDateFormat
                        .getTimeInstance(SimpleDateFormat.SHORT);

                resetTextStyle(viewHolder.title, R.style.ScheduleListPrimary);
                resetTextStyle(viewHolder.subtitle, R.style.ScheduleListSecondary);
                resetTextStyle(viewHolder.speakers, R.style.ScheduleListSecondary);
                resetTextStyle(viewHolder.lang, R.style.ScheduleListSecondary);
                resetTextStyle(viewHolder.day, R.style.ScheduleListSecondary);
                resetTextStyle(viewHolder.time, R.style.ScheduleListSecondary);
                resetTextStyle(viewHolder.room, R.style.ScheduleListSecondary);
                resetTextStyle(viewHolder.duration, R.style.ScheduleListSecondary);

                Lecture l = list.get(mMapper.get(position));
                viewHolder.title.setText(l.title);
                viewHolder.subtitle.setText(l.subtitle);
                viewHolder.speakers.setText(l.getFormattedSpeakers());
                viewHolder.lang.setText(l.lang);
                viewHolder.day.setText(df.format(new Date(l.dateUTC)));
                viewHolder.time.setText(tf.format(new Date(l.dateUTC)));
                viewHolder.room.setText(l.room);
                viewHolder.duration.setText(String.valueOf(l.duration) + " min.");
                viewHolder.video.setVisibility(View.GONE);
                viewHolder.novideo.setVisibility(View.GONE);

                if (l.changedIsNew) {
                    setTextStyleNew(viewHolder.title);
                    setTextStyleNew(viewHolder.subtitle);
                    setTextStyleNew(viewHolder.speakers);
                    setTextStyleNew(viewHolder.lang);
                    setTextStyleNew(viewHolder.day);
                    setTextStyleNew(viewHolder.time);
                    setTextStyleNew(viewHolder.room);
                    setTextStyleNew(viewHolder.duration);
                } else if (l.changedIsCanceled) {
                    setTextStyleCanceled(viewHolder.title);
                    setTextStyleCanceled(viewHolder.subtitle);
                    setTextStyleCanceled(viewHolder.speakers);
                    setTextStyleCanceled(viewHolder.lang);
                    setTextStyleCanceled(viewHolder.day);
                    setTextStyleCanceled(viewHolder.time);
                    setTextStyleCanceled(viewHolder.room);
                    setTextStyleCanceled(viewHolder.duration);
                } else {
                    if (l.changedTitle) {
                        setTextStyleChanged(viewHolder.title);
                        if (l.title.length() == 0) {
                            viewHolder.title.setText(context.getText(R.string.dash));
                        }
                    }
                    if (l.changedSubtitle) {
                        setTextStyleChanged(viewHolder.subtitle);
                        if (l.subtitle.length() == 0) {
                            viewHolder.subtitle.setText(context.getText(R.string.dash));
                        }
                    }
                    if (l.changedSpeakers) {
                        setTextStyleChanged(viewHolder.speakers);
                        if (l.speakers.length() == 0) {
                            viewHolder.speakers.setText(context.getText(R.string.dash));
                        }
                    }
                    if (l.changedLanguage) {
                        setTextStyleChanged(viewHolder.lang);
                        if (l.lang.length() == 0) {
                            viewHolder.lang.setText(context.getText(R.string.dash));
                        }
                    }
                    if (l.changedDay) {
                        setTextStyleChanged(viewHolder.day);
                    }
                    if (l.changedTime) {
                        setTextStyleChanged(viewHolder.time);
                    }
                    if (l.changedRoom) {
                        setTextStyleChanged(viewHolder.room);
                    }
                    if (l.changedDuration) {
                        setTextStyleChanged(viewHolder.duration);
                    }
                    if (l.changedRecordingOptOut) {
                        if (l.recordingOptOut) {
                            viewHolder.novideo.setVisibility(View.VISIBLE);
                        } else {
                            viewHolder.video.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;
            case TYPE_SEPARATOR:
                viewHolderSeperator.text.setText(mSeperatorStrings.get(mMapper.get(position)));
                break;
        }

        return rowView;
    }

    static class ViewHolder {
        TextView title;
        TextView subtitle;
        TextView speakers;
        TextView lang;
        TextView day;
        TextView time;
        TextView room;
        TextView duration;
        ImageView novideo;
        ImageView video;
    }

    static class ViewHolderSeperator {
        TextView text;
    }

    @Override
    public int getViewTypeCount() {
        return NUM_VIEW_TYPES;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return mSeparatorsSet.contains(position) ? false : true;
    }

    @Override
    public int getItemViewType(int position) {
        return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (list != null) count += list.size();
        if (mSeparatorsSet != null) count += mSeparatorsSet.size();
        return count;
    }

    private void initMapper() {
        mSeparatorsSet = new TreeSet<Integer>();
        mSeperatorStrings = new ArrayList<String>();
        mMapper = new ArrayList<Integer>();
        int day = 0;
        int lastDay = 0;
        int sepCount = 0;

        if (list == null) return;

        DateFormat df = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

        for (int index = 0; index < list.size(); index++) {

            Lecture l = list.get(index);
            day = l.day;
            if (day != lastDay) {
                String sepStr = String.format(context.getString(R.string.day_seperator), day,
                        df.format(new Date(l.dateUTC)));
                mSeperatorStrings.add(sepStr);
                lastDay = day;
                mSeparatorsSet.add(index + sepCount);
                mMapper.add(sepCount);
                sepCount++;
            }

            mMapper.add(index);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        initMapper();
    }

    public ArrayList<Integer> getMapper() {
        return mMapper;
    }
}
