package br.com.poison.core.util.extra;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String getDate(long millis) {
        return getDate(millis, true);
    }

    public static String getDate(long millis, boolean withHoursAndMinutes) {
        String date = new SimpleDateFormat("dd/MM/yyyy" + (withHoursAndMinutes ? " - HH:mm" : ""))
                .format(new Date(millis));

        if (withHoursAndMinutes) {
            date = date.replace("-", "às");
        }

        return date;
    }
}