package com.starry.tiktoksimplifiededition.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    public static String getFriendlyTime(long timestamp) {

        // 添加时间戳有效性检查
        if (timestamp <= 0) {
            return "";
        }

        long now = System.currentTimeMillis();

        if (timestamp > now) {
            timestamp = now;
        }

        long diff = now - timestamp;

        if (diff < 60 * 1000) {
            return "刚刚";
        } else if (diff < 3600 * 1000) {
            return (diff / (60 * 1000)) + " 分钟前";
        }

        Calendar nowCal = Calendar.getInstance();
        Calendar msgCal = Calendar.getInstance();
        msgCal.setTimeInMillis(timestamp);

        boolean isSameYear = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR);
        boolean isSameDay = isSameYear && nowCal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR);

        if (isSameDay) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
        }

        if (isSameYear && nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1) {
            return "昨天 " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
        }

        return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date(timestamp));
    }
}