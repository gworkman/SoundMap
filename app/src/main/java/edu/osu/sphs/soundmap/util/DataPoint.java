package edu.osu.sphs.soundmap.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Gus on 12/8/2017. The data class that is a template for and holds the recording information.
 * This is an immutable class.
 */

public final class DataPoint {


    private static final String TAG = "DataPoint";
    private static final int DAY_MILLIS = 86400000;
    private static final Calendar today = getToday();

    private long date;
    private double lat;
    private double lon;
    private double decibels;
    private String near;
    private Geocoder geocoder;

    public DataPoint() {
        // empty, required for firebase
    }

    public DataPoint(long date, double lat, double lon, double decibels) {
        this.date = date;
        this.lat = lat;
        this.lon = lon;
        this.decibels = decibels;
    }

    @Exclude
    private static Calendar getToday() {
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    @Exclude
    private String getFormattedDate(Context context) {
        Date formatted = new Date(this.date);
        return DateFormat.getDateFormat(context).format(formatted);
    }

    @Exclude
    private String getFormattedTime(Context context) {
        Date formatted = new Date(this.date);
        return DateFormat.getTimeFormat(context).format(formatted);
    }

    @Exclude
    public String getTimeString(Context context) {
        String timeString;
        long todayInMillis = today.getTimeInMillis();
        if (this.date - todayInMillis > 0) {
            timeString = getFormattedTime(context);
        } else if (todayInMillis - this.date < DAY_MILLIS) {
            timeString = "Yesterday";
        } else {
            timeString = getFormattedDate(context);
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

    public double getDecibels() {
        return decibels;
    }

    @Exclude
    public String getNear(Geocoder geocoder) {
        String near = lat + ", " + lon;
        if (Geocoder.isPresent()) {
            try {
                Address nearby = geocoder.getFromLocation(lat, lon, 1).get(0);
                if ((near = nearby.getFeatureName()) != null && !near.matches("\\d+")) {
                    near = "Near " + near;
                    Log.d(TAG, "getNear: feature " + near);
                } else if ((near = nearby.getAddressLine(0)) != null) {
                    near = "Near " + near.substring(0, near.indexOf(','));
                    Log.d(TAG, "getNear: addressline " + near);
                } else if ((near = nearby.getLocality()) != null) {
                    near = "Near " + near;
                    Log.d(TAG, "getNear: locality " + near);
                } else {
                    near = lat + ", " + lon;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return near;
    }

    @Override
    public String toString() {
        return "Date: " + this.date + " Lat: " + this.lat + " Long: " + this.lon + " dB(A): " + this.decibels;
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
