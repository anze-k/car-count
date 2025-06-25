package com.anze.CarCounter.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DateUtils {
    public static String timestampToLocalDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setTimeZone(TimeZone.getDefault()); // Potentially set to specific timezone
        
        String formattedDate = sdf.format(timestamp);

        return formattedDate;
    }
}
