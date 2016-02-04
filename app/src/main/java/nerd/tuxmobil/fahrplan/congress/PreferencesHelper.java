package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import info.metadude.android.typedpreferences.BooleanPreference;
import info.metadude.android.typedpreferences.IntPreference;
import info.metadude.android.typedpreferences.LongPreference;
import info.metadude.android.typedpreferences.StringPreference;

public class PreferencesHelper {

    public static final String PREFS_ALARM_TIME_INDEX =
            "nerd.tuxmobil.fahrplan.congress.Prefs.ALARM_TIME_INDEX";

    public static final String PREFS_ALTERNATIVE_HIGHLIGHT =
            "nerd.tuxmobil.fahrplan.congress.Prefs.ALTERNATIVE_HIGHLIGHT";

    public static final String PREFS_AUTO_UPDATE =
            "auto_update";

    public static final String PREFS_CHANGES_SEEN =
            "nerd.tuxmobil.fahrplan.congress.Prefs.CHANGES_SEEN";

    public static final String PREFS_DISPLAY_DAY =
            "displayDay";

    public static final String PREFS_INSISTENT_ALARM =
            "insistent";

    public static final String PREFS_LAST_FETCH =
            "last_fetch";

    public static final String PREFS_REMINDER_TONE_URI_STRING =
            "reminder_tone";

    public static final String PREFS_SCHEDULE_URL =
            "nerd.tuxmobil.fahrplan.congress.Prefs.SCHEDULE_URL";

    protected final IntPreference alarmTimeIndexPreference;

    protected final BooleanPreference alternativeHighlightPreference;

    protected final BooleanPreference autoUpdatePreference;

    protected final BooleanPreference changesSeenPreference;

    protected final IntPreference displayDayPreference;

    protected final BooleanPreference insistentAlarmPreference;

    protected final LongPreference lastFetchPreferences;

    protected final StringPreference reminderTonePreference;

    protected final StringPreference scheduleUrlPreference;

    public PreferencesHelper(@NonNull SharedPreferences sharedPreferences,
                             @NonNull Context context) {

        Resources resources = context.getResources();

        int defaultAlarmTimeIndex = resources.getInteger(
                R.integer.default_alarm_time_index);
        boolean defaultAlternativeHighlight = resources.getBoolean(
                R.bool.default_alternative_highlight);
        boolean defaultAutoUpdate = resources.getBoolean(
                R.bool.default_auto_update);
        boolean defaultChangesSeen = resources.getBoolean(
                R.bool.default_changes_seen);
        int defaultDisplayDay = resources.getInteger(
                R.integer.default_display_day);
        boolean defaultInsistentAlarm = resources.getBoolean(
                R.bool.default_insistent_alarm);
        int defaultLastFetch = resources.getInteger(
                R.integer.default_last_fetch);
        String defaultReminderToneUri = resources.getString(
                R.string.default_reminder_tone_uri);

        alarmTimeIndexPreference = new IntPreference(sharedPreferences,
                PREFS_ALARM_TIME_INDEX,
                defaultAlarmTimeIndex);
        alternativeHighlightPreference = new BooleanPreference(sharedPreferences,
                PREFS_ALTERNATIVE_HIGHLIGHT,
                defaultAlternativeHighlight);
        autoUpdatePreference = new BooleanPreference(sharedPreferences,
                PREFS_AUTO_UPDATE,
                defaultAutoUpdate);
        changesSeenPreference = new BooleanPreference(sharedPreferences,
                PREFS_CHANGES_SEEN,
                defaultChangesSeen);
        displayDayPreference = new IntPreference(sharedPreferences,
                PREFS_DISPLAY_DAY,
                defaultDisplayDay);
        insistentAlarmPreference = new BooleanPreference(sharedPreferences,
                PREFS_INSISTENT_ALARM,
                defaultInsistentAlarm);
        lastFetchPreferences = new LongPreference(sharedPreferences,
                PREFS_LAST_FETCH,
                defaultLastFetch);
        reminderTonePreference = new StringPreference(sharedPreferences,
                PREFS_REMINDER_TONE_URI_STRING,
                defaultReminderToneUri);
        scheduleUrlPreference = new StringPreference(sharedPreferences,
                PREFS_SCHEDULE_URL,
                null);
    }

}
