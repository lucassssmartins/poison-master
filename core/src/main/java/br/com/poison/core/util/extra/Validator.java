package br.com.poison.core.util.extra;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class Validator {

    private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private static final Pattern validUserPattern = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    public static boolean isUrl(String str) {
        return url.matcher(str).matches();
    }

    public static boolean isNickname(String nickname) {
        return StringUtils.isNotEmpty(nickname) && validUserPattern.matcher(nickname).matches();
    }

    public static Pattern caseInsensitive(String content) {
        return Pattern.compile("^" + content + "$", Pattern.CASE_INSENSITIVE);
    }
}