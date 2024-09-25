package br.com.poison.core.util.extra;

import java.text.DecimalFormat;

public class TimeUtil {

    public static long convertTime(long days, long hours, long minutes, long seconds) {
        long day = 86400 * days;
        long hour = 3600 * hours;
        long minute = 60 * minutes;

        long time = minute + hour + day + seconds;

        return System.currentTimeMillis() + time * 1000L;
    }

    public static long getTime(String timeFormat) {
        if (timeFormat.equals("-1L")) return -1L;

        String[] times = timeFormat.split(",");

        int day = 0, hour = 0, minute = 0, second = 0;

        for (String time : times) {
            time = time.toLowerCase();

            if (time.contains("d")) {
                day = Integer.parseInt(time.replace("d", ""));
            }

            if (time.contains("h")) {
                hour = Integer.parseInt(time.replace("h", ""));
            }

            if (time.contains("m")) {
                minute = Integer.parseInt(time.replace("m", ""));
            }

            if (time.contains("s")) {
                second = Integer.parseInt(time.replace("s", ""));
            }
        }

        return convertTime(day, hour, minute, second);
    }

    public static String formatTime(long time) {
        return formatTime(time, TimeFormat.NORMAL);
    }

    public static String formatTime(long time, TimeFormat format) {
        String message = "";

        long now = System.currentTimeMillis(), diff = time - now;

        int seconds = (int) (diff / 1000L);

        boolean hasDay = seconds >= 86400L, hasHour = seconds >= 3600, hasMinute = seconds >= 60, hasSeconds = seconds >= 1;

        boolean isNormalFormat = format.equals(TimeFormat.NORMAL);

        if (hasDay) {
            int day = seconds / 86400;

            seconds %= 86400;

            message = day + (isNormalFormat ? (" dia" + (day > 1 ? "s" : "")) : "d");
        }

        if (hasHour) {
            int hour = seconds / 3600;

            if (hour != 0) {
                seconds %= 3600;

                message = message + (hasDay ? ", " : "") + hour + (isNormalFormat ? (" hora" + (hour > 1 ? "s" : "")) : "h");
            }
        }

        if (hasMinute) {
            int min = seconds / 60;

            if (min != 0) {
                seconds %= 60;

                message = message + (hasHour ? ", " : "") + min + (isNormalFormat ? (" minuto" + (min > 1 ? "s" : "")) : "m");
            }
        }

        if (hasSeconds && seconds != 0) {
            message = message + (hasMinute ? ", " : "") + seconds + (isNormalFormat ? (" segundo" + (seconds > 1 ? "s" : "")) : "s");
        }

        return message;
    }

    public static String time(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    public static String newFormatTime(long time) {
        long now = System.currentTimeMillis();
        long diff = time - now;

        double seconds = diff / 1000.0;

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        return decimalFormat.format(seconds) + "s";
    }

    public static String formatTime(double time) {
        if (time <= 0) return "";

        double seconds = (time - System.currentTimeMillis()) / 1000;

        StringBuilder sb = new StringBuilder();

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        if (seconds >= 60) {
            double minutes = seconds / 60;

            sb.append(decimalFormat.format(minutes)).append(" ");
            sb.append("minuto").append(minutes > 1 ? "s" : "");

            return sb.toString();
        } else {
            sb.append(decimalFormat.format(seconds)).append(" ");
            sb.append("segundo").append(seconds > 1 ? "s" : "");
        }

        return sb.toString();
    }

    public static String formatTime(int time) {
        return formatTime(time, TimeFormat.SHORT);
    }

    public static String formatTime(int time, TimeFormat format) {
        int minutes = time / 60;
        int seconds = (time % 3600) % 60;

        boolean hasSeconds = seconds > 0, hasMinutes = minutes > 0;

        switch (format) {
            case NORMAL: {
                return (hasMinutes ? minutes + "m" : "") + (hasSeconds ? (hasMinutes ? " e " : "") + seconds + "s" : "");
            }
            case SHORT: {
                return (hasMinutes ? minutes + (minutes == 1 ? " minuto" : " minutos") : "")
                        + (hasSeconds ? (hasMinutes ? " e " : "") + seconds + (seconds == 1 ? " segundo" : " segundos") : "");
            }
        }

        return "???";
    }

    public enum TimeFormat {NORMAL, SHORT}
}
