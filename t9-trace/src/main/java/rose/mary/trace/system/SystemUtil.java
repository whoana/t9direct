package rose.mary.trace.system;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import rose.mary.trace.core.data.common.Trace; 

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
        // Map<String, String> maps = new HashMap<String, String>();
        // //Map<String, String> maps = new LinkedHashMap<String, String>();
        // maps.put("2", "2");
        // maps.put("3", "3");
        // maps.put("1", "1");
        
        // Collection<String> cols = maps.values();
        // for (String string : cols) {
        //     System.out.printf(string + ",");
        // }
        // System.out.println("");
        // Iterator<String> iterator = maps.values().iterator();
        // while (iterator.hasNext()) {
        //     System.out.printf(iterator.next() + ",");
        // }

        //Set 테스트
        Set<Trace> set = new LinkedHashSet<>();
        
        Trace t1 = new Trace();
        t1.setId("2");
        set.add(t1);


        Trace t2 = new Trace();
        t2.setId("3");
        set.add(t2);


        Trace t3 = new Trace();
        t3.setId("1");
        set.add(t3);



        System.out.printf("\ntrace hashcode:%d", t1.hashCode());
        t1.setId("2.1");        
        t2.setId("3.1");
        t3.setId("1.1");

        set.add(t2);
        set.add(t3);
        set.add(t1);

        for (Trace trace : set) {
            System.out.printf("\ntrace hashcode : %d, id: %s", trace.hashCode(), trace.getId());
        }

    }
}
