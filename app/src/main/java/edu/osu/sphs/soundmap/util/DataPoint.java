package edu.osu.sphs.soundmap.util;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Gus on 12/8/2017. The data class that is a template for and holds the recording information.
 */

public class DataPoint {

    private static final int DAY_MILLIS = 86400000;
    private static final Calendar today = getToday();

    private Context context;
    private long date;
    private double lat;
    private double lon;
    private double measurement;

    public DataPoint() {

    }

    public DataPoint(Context context, long date, double lat, double lon, double measurement) {
        this.context = context;
        this.date = date;
        this.lat = lat;
        this.lon = lon;
        this.measurement = measurement;
    }

    private static Calendar getToday() {
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    private String getFormattedDate() {
        Date formatted = new Date(this.date);
        return DateFormat.getDateFormat(this.context).format(formatted);
    }

    private String getFormattedTime() {
        Date formatted = new Date(this.date);
        return DateFormat.getTimeFormat(this.context).format(formatted);
    }

    public String getTimeString() {
        String timeString;
        long todayInMillis = today.getTimeInMillis();
        if (this.date - todayInMillis > 0) {
            timeString = getFormattedTime();
        } else if (todayInMillis - this.date < DAY_MILLIS) {
            timeString = "Yesterday";
        } else {
            timeString = getFormattedDate();
        }
        return timeString;
    }

    public long getDate() {
        return date;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getMeasurement() {
        return measurement;
    }

    public static class Compare implements Comparator<DataPoint> {
        @Override
        public int compare(DataPoint d1, DataPoint d2) {
            int returnCode = 0;
            if (d1.getDate() < d2.getDate()) {
                returnCode = 1;
            } else if (d1.getDate() > d2.getDate()) {
                returnCode = -1;
            }
            return returnCode;
        }
    }
}
