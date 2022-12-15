package rose.mary.trace.system;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date; 

public class SystemUtil {

    public static String getFormatedDate(String pattern, String fromDate, int unit, int dur) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        Date dates = null;
        Calendar cal = Calendar.getInstance();
        dates = dateFormat.parse(fromDate);
        cal.setTime(dates);
        cal.add(unit, dur);
        return dateFormat.format(cal.getTime());
    }

    
}
