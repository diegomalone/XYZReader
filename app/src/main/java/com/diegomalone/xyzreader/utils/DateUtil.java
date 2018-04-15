package com.diegomalone.xyzreader.utils;

import android.text.format.DateUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by malone on 18/03/18.
 */

public class DateUtil {

    private static final String TAG = DateUtil.class.toString();

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    private static SimpleDateFormat outputFormat = new SimpleDateFormat();

    public static Date parseStringDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    public static String getSinceDate(Date date) {
        return DateUtils.getRelativeTimeSpanString(
                date.getTime(),
                System.currentTimeMillis(), android.text.format.DateUtils.HOUR_IN_MILLIS,
                android.text.format.DateUtils.FORMAT_ABBREV_ALL).toString();
    }
}
