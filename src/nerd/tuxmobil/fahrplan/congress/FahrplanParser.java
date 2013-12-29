package nerd.tuxmobil.fahrplan.congress;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Xml;

interface OnParseCompleteListener
{
	public void onParseDone(Boolean result, String version);
}

public class FahrplanParser {
	private parser task;
	private OnParseCompleteListener listener;
	private Context context;

	public FahrplanParser(Context context) {
		task = null;
		MyApp.parser = this;
		this.context = context;
	}

	public void parse(String fahrplan, String eTag) {
		task = new parser(listener, context);
		task.execute(fahrplan, eTag);
	}

	public void cancel()
	{
		if (task != null) task.cancel(false);
	}

	public void setListener(OnParseCompleteListener listener) {
		this.listener = listener;
		if (task != null) {
			task.setListener(listener);
		}
	}
}

class parser extends AsyncTask<String, Void, Boolean> {
	private String LOG_TAG = "ParseFahrplan";
	private ArrayList<Lecture> lectures;
	private MetaInfo meta;
	private MetaDBOpenHelper metaDB;
	private SQLiteDatabase db;
	private OnParseCompleteListener listener;
	private boolean completed;
	private boolean result;
	private Context context;

	public parser(OnParseCompleteListener listener, Context context) {
		this.listener = listener;
		this.completed = false;
		this.db = null;
		this.context = context;
	}

	public void setListener(OnParseCompleteListener listener) {
		this.listener = listener;

		if (completed && (listener != null)) {
			notifyActivity();
		}
	}

	protected Boolean doInBackground(String... args) {

		return parseFahrplan(args[0], args[1]);

	}

	protected void onCancelled() {
		MyApp.LogDebug(LOG_TAG, "parse cancelled");
		if (db != null) db.close();
	}

	private void notifyActivity() {
		listener.onParseDone(result, meta.version);
		completed = false;
	}

	protected void onPostExecute(Boolean result) {
		completed = true;
		this.result = result;

		if (listener != null) {
			notifyActivity();
		}
	}

	public void storeMeta(Context context, MetaInfo meta) {
		MyApp.LogDebug(LOG_TAG, "storeMeta");
		metaDB = new MetaDBOpenHelper(context);

		db = metaDB.getWritableDatabase();
		ContentValues values = new ContentValues();

		try {
			db.beginTransaction();
			db.delete("meta", null, null);
			values.put("numdays", meta.numdays);
			values.put("version", meta.version);
			values.put("title", meta.title);
			values.put("subtitle", meta.subtitle);
			values.put("day_change_hour", meta.dayChangeHour);
			values.put("day_change_minute", meta.dayChangeMinute);
			values.put("etag", meta.eTag);

			db.insert("meta", null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public void storeLectureList(Context context, ArrayList<Lecture> lectures) {
		MyApp.LogDebug(LOG_TAG, "storeLectureList");
		LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);

		db = lecturesDB.getWritableDatabase();
		try {
			db.beginTransaction();
			db.delete("lectures", null, null);
			for (Lecture lecture : lectures) {
				if (isCancelled()) break;
				ContentValues values = new ContentValues();
				values.put("event_id", lecture.lecture_id);
				values.put("title", lecture.title);
				values.put("subtitle", lecture.subtitle);
				values.put("day", lecture.day);
				values.put("room", lecture.room);
				values.put("start", lecture.startTime);
				values.put("duration", lecture.duration);
				values.put("speakers", lecture.speakers);
				values.put("track", lecture.track);
				values.put("type", lecture.type);
				values.put("lang", lecture.lang);
				values.put("abstract", lecture.abstractt);
				values.put("descr", lecture.description);
				values.put("links", lecture.links);
				values.put("relStart", lecture.relStartTime);
				values.put("date", lecture.date);
				values.put("dateUTC", lecture.dateUTC);
				values.put("room_idx", lecture.room_index);
				db.insert("lectures", null, values);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	private Boolean parseFahrplan(String fahrplan, String eTag) {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new StringReader(fahrplan));
			int eventType = parser.getEventType();
			boolean done = false;
			int numdays = 0;
			String room = null;
			int day = 0;
			int dayChangeTime = 600; // hardcoded as not provided
			String date = "";
			int room_index = 0;
			int room_map_index = 0;
			HashMap<String, Integer> roomsMap = new HashMap<String, Integer>();
			while (eventType != XmlPullParser.END_DOCUMENT && !done && !isCancelled()) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					lectures = new ArrayList<Lecture>();
					meta = new MetaInfo();
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equals("version")) {
						parser.next();
						meta.version = new String(parser.getText());
					}
					if (name.equals("day")) {
						day = Integer.parseInt(parser.getAttributeValue(null,
								"index"));
						date = parser.getAttributeValue(null, "date");
						dayChangeTime = getDayChange(parser.getAttributeValue(null, "end"));
						if (day > numdays) { numdays = day; }
					}
					if (name.equals("room")) {
						room = new String(parser.getAttributeValue(null, "name"));
						if (!roomsMap.containsKey(room)) {
							roomsMap.put(room, room_index);
							room_map_index = room_index;
							room_index++;
						} else {
							room_map_index = roomsMap.get(room);
						}
					}
					if (name.equalsIgnoreCase("event")) {
						String id = parser.getAttributeValue(null, "id");
						Lecture lecture = new Lecture(id);
						lecture.day = day;
						lecture.room = room;
						lecture.date = date;
						lecture.room_index = room_map_index;
						MyApp.LogDebug(LOG_TAG, "room " + room + " with index " + room_map_index);
						eventType = parser.next();
						boolean lecture_done = false;
						while (eventType != XmlPullParser.END_DOCUMENT
								&& !lecture_done && !isCancelled()) {
							switch (eventType) {
							case XmlPullParser.END_TAG:
								name = parser.getName();
								if (name.equals("event")) {
									lectures.add(lecture);
									lecture_done = true;
								}
								break;
							case XmlPullParser.START_TAG:
								name = parser.getName();
								if (name.equals("title")) {
									parser.next();
									if (parser.getText() != null) lecture.title = parser.getText();
								} else if (name.equals("subtitle")) {
									parser.next();
									if (parser.getText() != null) lecture.subtitle = parser.getText();
								} else if (name.equals("track")) {
									parser.next();
									if (parser.getText() != null) lecture.track = parser.getText();
								} else if (name.equals("type")) {
									parser.next();
									if (parser.getText() != null) lecture.type = parser.getText();
								} else if (name.equals("language")) {
									parser.next();
									if (parser.getText() != null) lecture.lang = parser.getText();
								} else if (name.equals("abstract")) {
									parser.next();
									if (parser.getText() != null) lecture.abstractt = parser.getText();
								} else if (name.equals("description")) {
									parser.next();
									if (parser.getText() != null) lecture.description = parser.getText();
								} else if (name.equals("person")) {
									parser.next();
									if (parser.getText() != null) {
										lecture.speakers = lecture.speakers + (lecture.speakers.length() > 0 ? ";":"") + parser.getText();
									}
								} else if (name.equals("link")) {
									String url = parser.getAttributeValue(null, "href");
									parser.next();
									String urlname = parser.getText();
									if (!url.contains("://")) { url = "http://" + url; }
									StringBuilder sb = new StringBuilder();
									if (lecture.links.length() > 0) {
										sb.append(lecture.links);
										sb.append(",");
									}
									sb.append("[").append(urlname).append("]").append("(").append(url).append(")");
									lecture.links = sb.toString();
								} else if (name.equals("start")) {
									parser.next();
									if (parser.getText() != null) lecture.startTime = Lecture.parseStartTime(parser.getText());
									lecture.relStartTime = lecture.startTime;
									if (lecture.relStartTime < dayChangeTime) lecture.relStartTime += (24*60);
								} else if (name.equals("duration")) {
									parser.next();
									if (parser.getText() != null) lecture.duration = Lecture.parseDuration(parser.getText());
								} else if (name.equals("date")) {
									parser.next();
									if (parser.getText() != null) {
										lecture.dateUTC = Lecture.parseDateTime(parser.getText());
									}
								}
								break;
							}
							if (lecture_done) break;
							eventType = parser.next();
						}
					} else if (name.equalsIgnoreCase("conference")) {
						boolean conf_done = false;
						eventType = parser.next();
						while (eventType != XmlPullParser.END_DOCUMENT
								&& !conf_done) {
							switch (eventType) {
							case XmlPullParser.END_TAG:
								name = parser.getName();
								if (name.equals("conference")) {
									conf_done = true;
								}
								break;
							case XmlPullParser.START_TAG:
								name = parser.getName();
								if (name.equals("subtitle")) {
									parser.next();
									if (parser.getText() != null) {
										meta.subtitle = new String(parser.getText());
									} else {
										meta.subtitle = "";
									}
								}
								if (name.equals("title")) {
									parser.next();
									meta.title = new String(parser.getText());
								}
								if (name.equals("release")) {
									parser.next();
									meta.version = new String(parser.getText());
								}
								if (name.equals("day_change")) {
									parser.next();
									if (parser.getText() != null) dayChangeTime = Lecture.parseStartTime(parser.getText());
								}
								break;
							}
							if (conf_done) break;
							eventType = parser.next();
						}
					}
					break;
				}
				eventType = parser.next();
			}
			meta.numdays = numdays;
			if (isCancelled()) return false;
			storeLectureList(context, lectures);
			if (isCancelled()) return false;
			meta.eTag = eTag;
			storeMeta(context, meta);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private int getDayChange(String attributeValue) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		Date date;
		try {
			date = df.parse(attributeValue);
			long timeUTC = date.getTime();
			Time t = new Time();
			t.set(timeUTC);
			return (t.hour * 60) + t.minute;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 600;	// default
	}
}