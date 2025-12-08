package com.starry.tiktoksimplifiededition.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

        // 1分钟内：刚刚
        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "刚刚";
        }
        // 1小时内：xx分钟前
        else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + "分钟前";
        }

        Calendar nowCal = Calendar.getInstance();
        Calendar msgCal = Calendar.getInstance();
        msgCal.setTimeInMillis(timestamp);

        // 是否同一年
        boolean isSameYear = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR);

        // 是否同一天
        boolean isSameDay = isSameYear &&
                nowCal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR);

        // 今天：xx:xx
        if (isSameDay) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
        }

        // 昨天：昨天 xx:xx
        if (isSameYear && nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1) {
            return "昨天 " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
        }

        // 7天内：x天前
        long daysDiff = TimeUnit.MILLISECONDS.toDays(diff);
        if (daysDiff < 7) {
            return daysDiff + "天前";
        }

        // 其他：MM-dd
        return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date(timestamp));
    }
}
