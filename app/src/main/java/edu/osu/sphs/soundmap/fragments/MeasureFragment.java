package edu.osu.sphs.soundmap.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.activities.MainActivity;
import edu.osu.sphs.soundmap.util.DataPoint;
import edu.osu.sphs.soundmap.util.GraphView;
import edu.osu.sphs.soundmap.util.MeasureTask;
import edu.osu.sphs.soundmap.util.Values;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeasureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeasureFragment extends Fragment implements View.OnClickListener, MeasureTask.OnUpdateCallback {

    private static final String TAG = "MeasureFragment";

    private TextView timer;
    private TextView dB;
    private FloatingActionButton fab;
    private FloatingActionButton upload;
    private GraphView graph;
    private boolean isRunning = false;
    private boolean uploadInitialized = false;
    private boolean localOnlyMode = false;
    private double dBvalue;
    private CountDownTimer chronometer;
    private MeasureTask measureTask;
    private DatabaseReference data;
    private DatabaseReference user;
    private FirebaseAuth auth;
    private FusedLocationProviderClient locationProviderClient;
    private SharedPreferences prefs;
    private MainActivity activity;

    public MeasureFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MeasureFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MeasureFragment newInstance() {
        MeasureFragment fragment = new MeasureFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_measure, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (activity == null) activity = (MainActivity) getActivity();
        timer = view.findViewById(R.id.timer);
        dB = view.findViewById(R.id.dB);
        graph = view.findViewById(R.id.graph_view);
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        data = FirebaseDatabase.getInstance().getReference(Values.DATA_REFERNCE);
        auth = FirebaseAuth.getInstance();
        if (auth != null && auth.getCurrentUser() != null && auth.getUid() != null) {
            user = FirebaseDatabase.getInstance().getReference(Values.USER_NODE).child(auth.getUid());
        }
        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    // This is the onClick method for the fab in MainActivity
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                fab = (FloatingActionButton) v;
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                    if (measureTask == null) {
                        measureTask = new MeasureTask();
                        measureTask.setCallback(this);
                    }

                    if (!isRunning) {
                        localOnlyMode = isLocalOnly();
                        if (localOnlyMode || isMicPluggedIn()) {
                            locationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    Location location = task.getResult();
                                    if (localOnlyMode || (task.isSuccessful() && location != null && location.hasAccuracy() && location.getAccuracy() < 50)) {
                                        if (localOnlyMode || activity.canRecord(location)) {
                                            chronometer = new CountDownTimer(30000, 100) {
                                                @Override
                                                public void onTick(long millisUntilFinished) {
                                                    int seconds = (int) (millisUntilFinished / 1000);
                                                    int decimal = (int) (millisUntilFinished % 1000 / 100.0);
                                                    String timerValue = seconds + "." + decimal + " seconds";
                                                    timer.setText(timerValue);
                                                }

                                                @Override
                                                public void onFinish() {
                                                    fab.setImageResource(R.drawable.ic_record);
                                                    isRunning = false;
                                                    measureTask = null;
                                                }
                                            }.start();
                                            fab.setImageResource(R.drawable.ic_stop);
                                            upload.setVisibility(View.GONE);
                                            isRunning = true;
                                            graph.clear();
                                            measureTask.execute();
                                        } else {
                                            activity.setErrorMessage("A measurement was was taken here recently", "More",
                                                    "Users are limited from taking consecutive measurements in the " +
                                                            "same area over a short amount of time in order to encourage an" +
                                                            " increase in the spread and scope of the data that we are able " +
                                                            "to collect. If you want to take local measurements without uploading " +
                                                            "to the database, you can still do so by enabling local-only mode " +
                                                            "in the settings");
                                        }
                                    } else {
                                        activity.setErrorMessage("Unable to get an accurate device location", "More",
                                                "Please wait a few seconds to get a better GPS signal, " +
                                                        "and make sure your location services are turned on in " +
                                                        "the device settings.");
                                    }
                                }
                            });

                        } else {
                            activity.setErrorMessage("Microphone is not plugged in", "More",
                                    "In order to get an accurate measurement, it is necessary to use a " +
                                            "calibrated microphone. If you do not have a calibrated microphone from " +
                                            "the research study, you can use the app in local-only mode by going " +
                                            "to the settings menu.");
                        }

                    } else {
                        chronometer.cancel();
                        String timerResetValue = "30.0 seconds";
                        timer.setText(timerResetValue);
                        fab.setImageResource(R.drawable.ic_record);
                        upload.setVisibility(View.GONE);
                        isRunning = false;
                        measureTask.cancel(true);
                        measureTask = null;
                    }

                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, Values.AUDIO_REQUEST_CODE);
                }

                break;

            case R.id.fab_upload:
                if (!uploadInitialized) {
                    upload = (FloatingActionButton) v;
                    uploadInitialized = true;
                } else if (!localOnlyMode) {
                    locationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (task.isSuccessful() && location != null && location.hasAccuracy() && location.getAccuracy() < 50) {
                                String device = Build.MANUFACTURER + " " + Build.MODEL;
                                DataPoint toUpload = new DataPoint(System.currentTimeMillis(), location.getLatitude(), location.getLongitude(), dBvalue, device);

                                // create new node in Firebase
                                data.push().setValue(toUpload);

                                if (user == null) {
                                    if (auth != null && auth.getCurrentUser() != null && auth.getUid() != null) {
                                        user = FirebaseDatabase.getInstance().getReference(Values.USER_NODE).child(auth.getUid());
                                    }
                                }

                                if (user != null) {
                                    user.push().setValue(toUpload);
                                }
                                upload.setVisibility(View.GONE);
                            } else {
                                activity.setErrorMessage("Unable to get an accurate location", "More",
                                        "Please wait a few seconds to get a better GPS signal, " +
                                                "and make sure your location services are turned on in " +
                                                "the device settings.");
                            }
                        }
                    });
                } else {
                    activity.setErrorMessage("Currently in local-only mode", "More",
                            "Although you can record without a microphone, and take multiple measurements in the " +
                                    "same area in a short amount of time, you cannot upload to the database while in local " +
                                    "only mode. This is to ensure the data collected is accurate and from many different " +
                                    "locations. To upload to the database, please make sure you are logged in and that local " +
                                    "only mode is turned off.");
                    upload.setVisibility(View.GONE);
                }
        }
    }

    @Override
    public void onUpdate(double averageDB, double instantDB) {
        this.dBvalue = averageDB;
        String text = String.format(Locale.US, "%.02f", averageDB) + " dB";
        this.dB.setText(text);
        this.graph.add(System.currentTimeMillis(), (float) instantDB);
    }

    @Override
    public void onFinish(int result) {
        switch (result) {
            case MeasureTask.RESULT_OK:
                upload.setVisibility(View.VISIBLE);
                this.timer.setText(R.string.end_time);
                break;
            case MeasureTask.RESULT_CANCELLED:
                upload.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public double getCalibration() {
        return Double.valueOf(prefs.getString(getResources().getString(R.string.calibration_pref), "0"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Values.AUDIO_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fab.callOnClick();
                }
        }
    }

    private boolean isMicPluggedIn() {
        boolean micIn = false;
        AudioManager manager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AudioDeviceInfo[] devices = manager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            for (AudioDeviceInfo device : devices) {
                micIn = (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) ||
                        (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) ||
                        micIn;
            }
        } else {
            micIn = manager.isWiredHeadsetOn();
        }
        return micIn;
    }

    private boolean isLocalOnly() {
        return prefs.getBoolean(Values.LOCAL_ONLY_PREF, false) || auth.getCurrentUser() == null;
    }
}
