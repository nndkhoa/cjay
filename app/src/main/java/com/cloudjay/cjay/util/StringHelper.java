package com.cloudjay.cjay.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StringHelper {
    @SuppressLint("SimpleDateFormat")
    public static String getCurrentTimestamp(String format) {

        String timeStamp = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date();
        timeStamp = dateFormat.format(date);

        return timeStamp;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getRelativeDate(String format, String date) {

        if (TextUtils.isEmpty(date)) return "";

        Date now = new Date();

        // 2013-11-10T21:05:24+08:00
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }

        String timeString = DateUtils.getRelativeTimeSpanString(convertedDate.getTime(), now.getTime(),
                DateUtils.SECOND_IN_MILLIS).toString();

        return timeString;

    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimestamp(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String timeStamp = dateFormat.format(date);
        return timeStamp;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimestamp(String oldFormat, String newFormat, String date) {

        if (TextUtils.isEmpty(date)) return "";

        Logger.Log(oldFormat);
        Logger.Log(newFormat);
        Logger.Log(date);

        SimpleDateFormat formatter = new SimpleDateFormat(newFormat);
        String timeStamp = formatter.format(date);
        return timeStamp;
    }

    public static String concatStringsWSep(List<String> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String s : strings) {
            sb.append(sep).append(s);
            sep = separator;
        }
        return sb.toString();
    }

}
