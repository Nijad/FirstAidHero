package com.svu.firstaidhero.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for handling date formatting operations across the application.
 * Ensures that all dates stored in or displayed by the app follow a consistent format.
 */
public class DateUtils {

    // Standard date format used throughout the app (Year-Month-Day)
    private static final String PATTERN = "yyyy-MM-dd";

    /**
     * Retrieves the current date formatted as "yyyy-MM-dd".
     *
     * @return A string representing today's date.
     */
    public static String today() {
        return new SimpleDateFormat(PATTERN, Locale.US).format(new Date());
    }

    /**
     * Converts a timestamp in milliseconds to a formatted date string ("yyyy-MM-dd").
     *
     * @param millis The timestamp in milliseconds since the Unix epoch.
     * @return A string representing the formatted date.
     */
    public static String format(long millis) {
        return new SimpleDateFormat(PATTERN, Locale.US).format(new Date(millis));
    }
}