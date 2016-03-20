import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import nerd.tuxmobil.fahrplan.congress.DateHelper;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class DateHelperTests {

    @Test
    public void getFormattedDateForSharingWithSummerTime() {
        assertThat(DateHelper.getFormattedDateForSharing(1459580400000L))
                .isEqualTo("Saturday, April 2, 2016 9:00 AM");
    }

    @Test
    public void getFormattedDateForSharingWithLeapYear() {
        assertThat(DateHelper.getFormattedDateForSharing(1456783200000L))
                .isEqualTo("Monday, February 29, 2016 11:00 PM");
    }

}
