package chaos.fahrplan.congress;

public class MetaInfo {
	public int numdays;
	public String version;
	public String title;
	public String subtitle;
	public int dayChangeHour;
	public int dayChangeMinute;
	public String eTag;

	public MetaInfo() {
		numdays = 0;
		version = "";
		title = "";
		subtitle = "";
		dayChangeHour = 4;
		dayChangeMinute = 0;
		eTag = "";
	}
}
