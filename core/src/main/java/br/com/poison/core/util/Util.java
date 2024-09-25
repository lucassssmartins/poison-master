package br.com.poison.core.util;

import com.google.common.base.Strings;
import net.md_5.bungee.api.ChatColor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe utilitária.
 */
public class Util {

    public static String formatNumber(double value) {
        DecimalFormat def = new DecimalFormat("#,###.#", new DecimalFormatSymbols(Locale.US));
        return def.format(value);
    }

    public static String formatNumberWithLetter(double value) {
        if (value >= 1000000) {
            return new DecimalFormat("#,###.#m", new DecimalFormatSymbols(Locale.US)).format(value / 1000000);
        } else if (value >= 1000) {
            return new DecimalFormat("#,###.#k", new DecimalFormatSymbols(Locale.US)).format(value / 1000);
        } else {
            return new DecimalFormat("#,###.#", new DecimalFormatSymbols(Locale.US)).format(value);
        }
    }

    public static boolean isNumber(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static long formatMS(long ms) {
        return (System.currentTimeMillis() - ms);
    }

    public static String color(String content) {
        return ChatColor.translateAlternateColorCodes('&', content);
    }

    public static boolean hasColor(String message) {
        String pattern = "([&§][0-9A-Fa-f])";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(message);

        return matcher.find();
    }

    public static String extractColor(String message) {
        String pattern = "([&§][0-9A-Fa-f])";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(message);

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            result.append(matcher.group());
        }

        return result.toString();
    }

    public static String createProgressBar(ChatColor color, ChatColor otherColor, String symbol, int current, int max, int totalBars) {
        return createProgressBar(color, otherColor, symbol, current, max, totalBars, true);
    }

    public static String createProgressBar(ChatColor color, ChatColor otherColor, String symbol, int current, int max, int totalBars, boolean mColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return Strings.repeat(color + (mColor ? "§m" : "") + symbol, progressBars)
                + Strings.repeat(otherColor + (mColor ? "§m" : "") + symbol, totalBars - progressBars);
    }

    public static String createProgressBar(ChatColor color, ChatColor otherColor, String symbol, int current, int max, int totalBars, boolean mColor, int maxTeamLength) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        String progressBarString = Strings.repeat(color + (mColor ? "§m" : "") + symbol, progressBars)
                + Strings.repeat(otherColor + (mColor ? "§m" : "") + symbol, totalBars - progressBars);

        // Limita o comprimento do progressBarString
        if (progressBarString.length() > maxTeamLength) {
            progressBarString = progressBarString.substring(0, maxTeamLength);
        }

        return progressBarString;
    }

    public static int percent(int current, int total) {
        return percent(current, total, 100);
    }

    public static int percent(int current, int total, int divisor) {
        int division = current * divisor / total;
        return Math.min(division, divisor);
    }
}
