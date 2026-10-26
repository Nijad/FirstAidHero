package com.svu.firstaidhero.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String PATTERN = "yyyy-MM-dd";

    public static String today() {
        return new SimpleDateFormat(PATTERN, Locale.US).format(new Date());
    }

    public static String format(long millis) {
        return new SimpleDateFormat(PATTERN, Locale.US).format(new Date(millis));
    }
}