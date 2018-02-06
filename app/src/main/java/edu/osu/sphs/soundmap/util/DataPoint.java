package edu.osu.sphs.soundmap.util;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Gus on 12/8/2017. The data class that is a template for and holds the recording information.
 * This is an immutable class.
 */
@IgnoreExtraProperties
public final class DataPoint implements Parcelable {

    public static final Parcelable.Creator<DataPoint> CREATOR = new Parcelable.Creator<DataPoint>() {
        @Override
        public DataPoint createFromParcel(Parcel source) {
            return new DataPoint(source);
        }

        @Override
        public DataPoint[] newArray(int size) {
            return new DataPoint[size];
        }
    };
    private static final int DAY_MILLIS = 86400000;
    private static final Calendar today = getToday();
    private static final String device = Build.MANUFACTURER + " " + Build.MODEL;
    private long date;
    private double lat;
    private double lon;
    private double measurement;
    private String user;

    public DataPoint() {
        // empty, as required by Firebase
    }

    public DataPoint(long date, double lat, double lon, double measurement, String user) {
        this.date = date;
        this.lat = lat;
        this.lon = lon;
        this.measurement = measurement;
        this.user = user;
    }

    private DataPoint(Parcel in) {
        this.date = in.readLong();
        this.lat = in.readDouble();
        this.lon = in.readDouble();
        this.measurement = in.readDouble();
        this.user = in.readString();
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
        return this.date;
    }

    public double getLat() {
        return this.lat;
    }

    public double getLon() {
        return this.lon;
    }

    public double getDecibels() {
        return this.measurement;
    }

    public String getDevice() {
        return device;
    }

    public String getUser() {
        return this.user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.date);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lon);
        dest.writeDouble(this.measurement);
        dest.writeString(this.user);
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
