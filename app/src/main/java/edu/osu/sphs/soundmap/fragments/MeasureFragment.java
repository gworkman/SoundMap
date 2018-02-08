package edu.osu.sphs.soundmap.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.util.DataPoint;
import edu.osu.sphs.soundmap.util.MeasureTask;
import edu.osu.sphs.soundmap.util.Values;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeasureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeasureFragment extends Fragment implements View.OnClickListener, MeasureTask.OnUpdateCallback, OnSuccessListener<Location> {

    private static final String TAG = "MeasureFragment";

    private TextView timer;
    private TextView dB;
    private FloatingActionButton fab;
    private FloatingActionButton upload;
    private boolean isRunning = false;
    private boolean uploadInitialized = false;
    private double dBvalue;
    private CountDownTimer chronometer;
    private MeasureTask measureTask;
    private DatabaseReference data;
    private DatabaseReference users;
    private FusedLocationProviderClient locationProviderClient;
    private SharedPreferences prefs;
    private Activity activity;
    private String uid;

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
    public static MeasureFragment newInstance(String uid) {
        MeasureFragment fragment = new MeasureFragment();
        Bundle args = new Bundle();
        args.putString(Values.UID_KEY, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(Values.UID_KEY)) {
            this.uid = args.getString(Values.UID_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_measure, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (activity == null) activity = getActivity();
        timer = view.findViewById(R.id.timer);
        dB = view.findViewById(R.id.dB);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        data = FirebaseDatabase.getInstance().getReference(prefs.getString(Values.DATA_SRC_PREF, "bad-input"));
        users = FirebaseDatabase.getInstance().getReference("users-test");
        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    // This is the onClick method for the fab in MainActivity
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                    if (measureTask == null) {
                        measureTask = new MeasureTask();
                        measureTask.setCallback(this);
                    }

                    if (!isRunning) {
                        fab = (FloatingActionButton) v;
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
                        measureTask.execute();

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
                } else {
                    locationProviderClient.getLastLocation().addOnSuccessListener(this);
                }
        }
    }

    @Override
    public void onUpdate(double dB) {
        this.dBvalue = dB;
        final String text = String.format(Locale.US, "%.02f", dB) + " dB";
        this.dB.setText(text);
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

    @Override
    public void onSuccess(Location location) {
        DataPoint toUpload = new DataPoint(System.currentTimeMillis(), location.getLatitude(), location.getLongitude(), this.dBvalue, this.uid);

        // create new node in Firebase under general node and user specific node
        DatabaseReference generalNode = data.push();
        generalNode.setValue(toUpload);
        DatabaseReference userNode = users.child(this.uid).push();
        userNode.setValue(toUpload);
        upload.setVisibility(View.GONE);
    }
}
