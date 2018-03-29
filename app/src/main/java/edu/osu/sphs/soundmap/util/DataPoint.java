package edu.osu.sphs.soundmap.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.format.DateFormat;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Gus on 12/8/2017. The data class that is a template for and holds the recording information.
 * This is an immutable class.
 */

public final class DataPoint {

    private static final int DAY_MILLIS = 86400000;
    private static final Calendar today = getToday();

    @PropertyName("time")
    private double time;

    @PropertyName("lat")
    private double lat;

    @PropertyName("lon")
    private double lon;

    @PropertyName("decibels")
    private double decibels;

    @PropertyName("offset")
    private double offset;

    @PropertyName("device")
    private String device;

    @PropertyName("user")
    private String user;

    public DataPoint() {
        // empty, required for firebase
    }

    public DataPoint(long time, double lat, double lon, double decibels, double offset, String device, String user) {
        this.time = time * 1000;
        this.lat = lat;
        this.lon = lon;
        this.decibels = decibels;
        this.offset = offset;
        this.device = device;
        this.user = user;
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
        Date formatted = new Date((long) this.time);
        return DateFormat.getDateFormat(context).format(formatted);
    }

    @Exclude
    private String getFormattedTime(Context context) {
        Date formatted = new Date((long) this.time);
        return DateFormat.getTimeFormat(context).format(formatted);
    }

    @Exclude
    public String getTimeString(Context context) {
        String timeString;
        long todayInMillis = today.getTimeInMillis();
        if (this.time - todayInMillis > 0) {
            timeString = getFormattedTime(context);
        } else if (todayInMillis - this.time < DAY_MILLIS) {
            timeString = "Yesterday";
        } else {
            timeString = getFormattedDate(context);
        }
        return timeString;
    }

    public double getTime() {
        return time / 1000;
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

    public String getDevice() {
        return device;
    }

    public void setTime(long time) {
        this.time = time * 1000;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setDecibels(double decibels) {
        this.decibels = decibels;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @Exclude
    public String getNear(Geocoder geocoder) {
        String near = lat + ", " + lon;
        if (Geocoder.isPresent()) {
            try {
                Address nearby = geocoder.getFromLocation(lat, lon, 1).get(0);
                if ((near = nearby.getFeatureName()) != null && !near.matches("\\d+")) {
                    near = "Near " + near;
                } else if ((near = nearby.getAddressLine(0)) != null) {
                    near = "Near " + near.substring(0, near.indexOf(','));
                } else if ((near = nearby.getLocality()) != null) {
                    near = "Near " + near;
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
        return "Date: " + (this.time - System.currentTimeMillis()) / DAY_MILLIS + " Lat: " + this.lat + " Long: " + this.lon + " dB(A): " + this.decibels;
    }

    public static class Compare implements Comparator<DataPoint> {
        @Override
        public int compare(DataPoint d1, DataPoint d2) {
            int returnCode = 0;
            if (d1.getTime() < d2.getTime()) {
                returnCode = 1;
            } else if (d1.getTime() > d2.getTime()) {
                returnCode = -1;
            }
            return returnCode;
        }
    }
}
