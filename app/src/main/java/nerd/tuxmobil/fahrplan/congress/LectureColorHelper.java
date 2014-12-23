package nerd.tuxmobil.fahrplan.congress;

import java.util.HashMap;

public class LectureColorHelper {

    private static HashMap<String, Integer> TRACK_DRAWABLES;

    private static HashMap<String, Integer> TRACK_DRAWABLES_HI;
    
    private static HashMap<String, Integer> TRACK_COLORS;

    static {
        TRACK_DRAWABLES = new HashMap<String, Integer>();
        TRACK_DRAWABLES.put("Art & Beauty",
                R.drawable.event_border_default_art_beauty);
        TRACK_DRAWABLES.put("CCC",
                R.drawable.event_border_default_ccc);
        TRACK_DRAWABLES.put("Entertainment",
                R.drawable.event_border_default_entertainment);
        TRACK_DRAWABLES.put("Ethics, Society & Politics",
                R.drawable.event_border_default_ethics_society_politics);
        TRACK_DRAWABLES.put("Hardware & Making",
                R.drawable.event_border_default_hardware_making);
        TRACK_DRAWABLES.put("Other",
                R.drawable.event_border_default_other);
        TRACK_DRAWABLES.put("Science & Engineering",
                R.drawable.event_border_default_science_engineering);
        TRACK_DRAWABLES.put("Security & Safety",
                R.drawable.event_border_default_security_safety);
        TRACK_DRAWABLES.put("",
                R.drawable.event_border_default);

        TRACK_DRAWABLES_HI = new HashMap<String, Integer>();
        TRACK_DRAWABLES_HI.put("Art & Beauty",
                R.drawable.event_border_highlight_art_beauty);
        TRACK_DRAWABLES_HI.put("CCC",
                R.drawable.event_border_highlight_ccc);
        TRACK_DRAWABLES_HI.put("Entertainment",
                R.drawable.event_border_highlight_entertainment);
        TRACK_DRAWABLES_HI.put("Ethics, Society & Politics",
                R.drawable.event_border_highlight_ethics_society_politics);
        TRACK_DRAWABLES_HI.put("Hardware & Making",
                R.drawable.event_border_highlight_hardware_making);
        TRACK_DRAWABLES_HI.put("Other",
                R.drawable.event_border_highlight_other);
        TRACK_DRAWABLES_HI.put("Science & Engineering",
                R.drawable.event_border_highlight_science_engineering);
        TRACK_DRAWABLES_HI.put("Security & Safety",
                R.drawable.event_border_highlight_security_safety);
        TRACK_DRAWABLES_HI.put("",
                R.drawable.event_border_highlight);

        TRACK_COLORS = new HashMap<String, Integer>();
        TRACK_COLORS.put("Art & Beauty",
                R.color.event_border_default_art_beauty);
        TRACK_COLORS.put("CCC",
                R.color.event_border_default_ccc);
        TRACK_COLORS.put("Entertainment",
                R.color.event_border_default_entertainment);
        TRACK_COLORS.put("Ethics, Society & Politics",
                R.color.event_border_default_ethics_society_politics);
        TRACK_COLORS.put("Hardware & Making",
                R.color.event_border_default_hardware_making);
        TRACK_COLORS.put("Other",
                R.color.event_border_default_other);
        TRACK_COLORS.put("Science & Engineering",
                R.color.event_border_default_science_engineering);
        TRACK_COLORS.put("Security & Safety",
                R.color.event_border_default_security_safety);
        TRACK_COLORS.put("",
                R.color.event_border_default);
    }

    public static Integer getBackgroundDrawableId(String track, boolean highlight) {
        if (highlight) {
            return TRACK_DRAWABLES_HI.get(track);
        }

        return TRACK_DRAWABLES.get(track);
    }
    
    public static int getBackgroundColor(String track) {
        Integer colorId = TRACK_COLORS.get(track);
        if (colorId == null) {
            return MyApp.app.getApplicationContext().getResources().getColor(R.color.event_border_default);
        }

        return MyApp.app.getApplicationContext().getResources().getColor(colorId);
    }
}
