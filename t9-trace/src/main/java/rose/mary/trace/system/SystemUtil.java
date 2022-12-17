package rose.mary.trace.system;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map; 

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

    public static void main(String[] args) {
        Map<String, String> maps = new HashMap<String, String>();
        //Map<String, String> maps = new LinkedHashMap<String, String>();
        maps.put("2", "2");
        maps.put("3", "3");
        maps.put("1", "1");
        
        Collection<String> cols = maps.values();
        for (String string : cols) {
            System.out.printf(string + ",");
        }
        System.out.println("");
        Iterator<String> iterator = maps.values().iterator();
        while (iterator.hasNext()) {
            System.out.printf(iterator.next() + ",");
        }


    }
}
