package nerd.tuxmobil.fahrplan.congress;

import java.util.Locale;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

interface OnCloseDetailListener {
	public void closeDetailView();
}

public class EventDetailFragment extends SherlockFragment {

	private final String LOG_TAG = "Detail";
	private String event_id;
	private String title;
	private static String feedbackURL = "https://cccv.pentabarf.org/feedback/30C3/event/"; // + 4302.en.html
	private Locale locale;
	private Typeface boldCondensed;
	private Typeface black;
	private Typeface light;
	private Typeface regular;
	private Typeface bold;
	private Lecture lecture;
	private int day;
	private String subtitle;
	private String spkr;
	private String abstractt;
	private String descr;
	private String links;
	private Boolean sidePane = false;
	private boolean hasArguments = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		MyApp.LogDebug(LOG_TAG, "onCreate");
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if (sidePane) {
			return inflater.inflate(R.layout.detail_narrow, container, false);
    	} else {
			return inflater.inflate(R.layout.detail, container, false);
    	}
    }

    @Override
    public void setArguments(Bundle args) {
    	super.setArguments(args);
        day = args.getInt("day", 0);
        event_id = args.getString("eventid");
        title = args.getString("title");
        subtitle = args.getString("subtitle");
        spkr = args.getString("spkr");
        abstractt = args.getString("abstract");
        descr = args.getString("descr");
        links = args.getString("links");
        sidePane = args.getBoolean("sidepane", false);
        hasArguments = true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);

    	if (hasArguments) {
			boldCondensed = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-BoldCondensed.ttf");
			black = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Black.ttf");
			light = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Light.ttf");
			regular = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Regular.ttf");
			bold = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Bold.ttf");

	        locale = getResources().getConfiguration().locale;

	        FahrplanFragment.loadLectureList(getSherlockActivity(), day, false);
//	        lecture = eventid2Lecture(event_id);

	        TextView t = (TextView)view.findViewById(R.id.title);
	        t.setTypeface(boldCondensed);
	        t.setText(title);

	        t = (TextView)view.findViewById(R.id.subtitle);
	        t.setText(subtitle);
	        t.setTypeface(light);
	        if (subtitle.length() == 0) t.setVisibility(View.GONE);

	        t = (TextView)view.findViewById(R.id.speakers);
	        t.setTypeface(black);
	        t.setText(spkr);

	        t = (TextView)view.findViewById(R.id.abstractt);
	        t.setTypeface(bold);
	        abstractt = abstractt.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
	        t.setText(Html.fromHtml(abstractt), TextView.BufferType.SPANNABLE);
	        t.setMovementMethod(new LinkMovementMethod());

	        t = (TextView)view.findViewById(R.id.description);
	        t.setTypeface(regular);
	        descr = descr.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
	        t.setText(Html.fromHtml(descr), TextView.BufferType.SPANNABLE);
	        t.setMovementMethod(new LinkMovementMethod());

	        TextView l = (TextView)view.findViewById(R.id.linksSection);
	        l.setTypeface(bold);
	        t = (TextView)view.findViewById(R.id.links);
	        t.setTypeface(regular);

	        if (links.length() > 0) {
	        	MyApp.LogDebug(LOG_TAG, "show links");
	        	l.setVisibility(View.VISIBLE);
	        	t.setVisibility(View.VISIBLE);
	        	links = links.replaceAll("\\),", ")<br>");
		        links = links.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
		        t.setText(Html.fromHtml(links), TextView.BufferType.SPANNABLE);
		        t.setMovementMethod(new LinkMovementMethod());
	        } else {
	        	l.setVisibility(View.GONE);
	        	t.setVisibility(View.GONE);
	        }

	        final TextView eventOnlineSection = (TextView) view.findViewById(R.id.eventOnlineSection);
	        eventOnlineSection.setTypeface(bold);
	        final TextView eventOnlineLink = (TextView) view.findViewById(R.id.eventOnline);
	        eventOnlineLink.setTypeface(regular);
	        final String eventUrl = FahrplanMisc.getEventUrl(getActivity(), event_id);
	        final String eventLink = "<a href=\"" + eventUrl + "\">" + eventUrl + "</a>";
	        eventOnlineLink.setText(Html.fromHtml(eventLink), TextView.BufferType.SPANNABLE);
	        eventOnlineLink.setMovementMethod(new LinkMovementMethod());

	        getSherlockActivity().supportInvalidateOptionsMenu();
    	}
        getSherlockActivity().setResult(SherlockFragmentActivity.RESULT_CANCELED);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.detailmenu, menu);
		MenuItem item;
		if (Build.VERSION.SDK_INT < 14) {
			item = menu.findItem(R.id.item_add_to_calendar);
			if (item != null) item.setVisible(false);
		}
                lecture = eventid2Lecture(event_id);

		if (lecture != null) {
			if (lecture.highlight) {
				item = menu.findItem(R.id.item_fav);
				if (item != null) item.setVisible(false);
				item = menu.findItem(R.id.item_unfav);
				if (item != null) item.setVisible(true);
			}
			if (lecture.has_alarm) {
				item = menu.findItem(R.id.item_set_alarm);
				if (item != null) item.setVisible(false);
				item = menu.findItem(R.id.item_clear_alarm);
				if (item != null) item.setVisible(true);
			}
		}
		if (sidePane) {
			item = menu.findItem(R.id.item_close);
			if (item != null) item.setVisible(true);
		}
    }

	private Lecture eventid2Lecture(String event_id) {
		if (MyApp.lectureList == null) return null;
		for (Lecture lecture : MyApp.lectureList) {
			if (lecture.lecture_id.equals(event_id)) {
				return lecture;
			}
		}
		return null;
	}

	void setAlarmDialog(final Lecture lecture) {

		LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.reminder_dialog,
				(ViewGroup) getView().findViewById(R.id.layout_root));

		final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(getSherlockActivity(), R.array.alarm_array,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		TextView msg = (TextView)layout.findViewById(R.id.message);
		msg.setText(R.string.choose_alarm_time);

		new AlertDialog.Builder(getSherlockActivity()).setTitle(R.string.setup_alarm).setView(layout)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								int alarm = spinner.getSelectedItemPosition();
								MyApp.LogDebug(LOG_TAG, "alarm chosen: "+alarm);
								FahrplanMisc.addAlarm(getSherlockActivity(), lecture, alarm);
								getSherlockActivity().supportInvalidateOptionsMenu();
								getSherlockActivity().setResult(SherlockFragmentActivity.RESULT_OK);
								refreshEventMarkers();
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create().show();
	}

	public void refreshEventMarkers() {
		SherlockFragmentActivity activity = getSherlockActivity();
		if ((activity != null) && (activity instanceof OnRefreshEventMarers)) {
			((OnRefreshEventMarers)activity).refreshEventMarkers();
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Lecture l;
		switch (item.getItemId()) {
		case R.id.item_feedback:
			StringBuilder sb = new StringBuilder();
			sb.append(feedbackURL);
			sb.append(event_id).append(".");
			if (locale.getLanguage().equals("de")) {
				sb.append("de");
			} else {
				sb.append("en");
			}
			sb.append(".html");
			Uri uri = Uri.parse(sb.toString());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			return true;
		case R.id.item_share:
			l = eventid2Lecture(event_id);
			if (l != null) FahrplanMisc.share(getSherlockActivity(), l);
			return true;
		case R.id.item_add_to_calendar:
			l = eventid2Lecture(event_id);
			if (l != null) FahrplanMisc.addToCalender(getSherlockActivity(), l);
			return true;
		case R.id.item_fav:
			lecture.highlight = true;
			if (lecture != null) FahrplanMisc.writeHighlight(getSherlockActivity(), lecture);
			getSherlockActivity().supportInvalidateOptionsMenu();
			getSherlockActivity().setResult(SherlockFragmentActivity.RESULT_OK);
			refreshEventMarkers();
			return true;
		case R.id.item_unfav:
			lecture.highlight = false;
			if (lecture != null) FahrplanMisc.writeHighlight(getSherlockActivity(), lecture);
			getSherlockActivity().supportInvalidateOptionsMenu();
			getSherlockActivity().setResult(SherlockFragmentActivity.RESULT_OK);
			refreshEventMarkers();
			return true;
		case R.id.item_set_alarm:
			setAlarmDialog(lecture);
			return true;
		case R.id.item_clear_alarm:
			if (lecture != null) FahrplanMisc.deleteAlarm(getSherlockActivity(), lecture);
			getSherlockActivity().supportInvalidateOptionsMenu();
			getSherlockActivity().setResult(SherlockFragmentActivity.RESULT_OK);
			refreshEventMarkers();
			return true;
		case R.id.item_close:
			SherlockFragmentActivity activity = getSherlockActivity();
			if ((activity != null) && (activity instanceof OnCloseDetailListener)) {
				((OnCloseDetailListener)activity).closeDetailView();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MyApp.LogDebug(LOG_TAG, "onDestroy");
	}
}
