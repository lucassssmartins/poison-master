package br.com.poison.core.util.extra;

import br.com.poison.core.Core;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {

    static String NUMERIC_CHARS = "0123456789";
    static String LETTERS_LOWER_CHARS = "abcdefghijklmnopqrstuvwxyz";
    static String LETTERS_UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static String SYMBOLS_CHARS = "#$%*&_+=^?/";
    static String FULL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            /* letters */ + "abcdefghijklmnopqrstuvwxyz"
            /* numeric */ + "0123456789"
            /* symbols */ + "#$%*&_+=^?/";
    static String EXCLUSIVE_CHARS = "abcdefghijklmnopqrstuvwxyz"
            /* numeric */ + "0123456789";

    public static String generateCode() {
        return generateCode(8);
    }

    public static String generateExclusiveCode(int length) {
        return Core.RANDOM.ints(length, 0, EXCLUSIVE_CHARS.length())
                .mapToObj(index -> "" + EXCLUSIVE_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static String generateCode(int length) {
        return Core.RANDOM.ints(length, 0, FULL_CHARS.length())
                .mapToObj(index -> "" + FULL_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static String generateNumberCode(int length) {
        return Core.RANDOM.ints(length, 0, NUMERIC_CHARS.length())
                .mapToObj(index -> "" + NUMERIC_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static String generateSymbolsCode(int length) {
        return Core.RANDOM.ints(length, 0, SYMBOLS_CHARS.length())
                .mapToObj(index -> "" + SYMBOLS_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static List<String> formatForLore(String text) {
        return getLore(30, text);
    }

    public static List<String> getLore(int max, String text) {
        List<String> lore = new ArrayList<>();
        text = ChatColor.translateAlternateColorCodes('&', text);
        String[] split = text.split(" ");
        String color = "";
        text = "";
        for (int i = 0; i < split.length; i++) {
            if (ChatColor.stripColor(text).length() >= max || ChatColor.stripColor(text).endsWith(".")
                    || ChatColor.stripColor(text).endsWith("!")) {
                lore.add(text);
                if (text.endsWith(".") || text.endsWith("!"))
                    lore.add("");
                text = color;
            }
            String toAdd = split[i];
            if (toAdd.contains("§"))
                color = ChatColor.getLastColors(toAdd.toLowerCase());
            if (toAdd.contains("\n")) {
                toAdd = toAdd.substring(0, toAdd.indexOf("\n"));
                split[i] = split[i].substring(toAdd.length() + 1);
                lore.add(text + (text.length() == 0 ? "" : " ") + toAdd);
                text = color;
                i--;
            } else {
                text += (ChatColor.stripColor(text).length() == 0 ? "" : " ") + toAdd;
            }
        }
        lore.add(text);
        return lore;
    }

}