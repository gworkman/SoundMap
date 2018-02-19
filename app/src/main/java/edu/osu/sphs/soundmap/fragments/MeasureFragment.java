package edu.osu.sphs.soundmap.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
    private boolean isRunning = false;
    private boolean uploadInitialized = false;
    private double dBvalue;
    private CountDownTimer chronometer;
    private MeasureTask measureTask;
    private DatabaseReference data;
    private DatabaseReference user;
    private FirebaseAuth auth;
    private FusedLocationProviderClient locationProviderClient;
    private SharedPreferences prefs;
    private RequestQueue queue;
    private Activity activity;
    private Context context;

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
        if (activity == null) activity = getActivity();
        context = getContext();
        queue = Volley.newRequestQueue(context);
        timer = view.findViewById(R.id.timer);
        dB = view.findViewById(R.id.dB);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        data = FirebaseDatabase.getInstance().getReference(prefs.getString(getString(R.string.data_source_pref), "iOS"));
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
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                    if (measureTask == null) {
                        measureTask = new MeasureTask();
                        measureTask.setCallback(this);
                    }

                    if (!isRunning) {
                        if (/*isMicPluggedIn()*/ true) {
                            locationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    if (task.isSuccessful()) {
                                        Location location = task.getResult();
                                        String url = Values.FUNCTION_VALID_LOCATION_URL
                                                .replace("<lat>", String.valueOf(location.getLatitude()))
                                                .replace("<lon>", String.valueOf(location.getLongitude()));
                                        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                if (response.matches("true")) {
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
                                                    ((MainActivity) activity).setErrorMessage("There has already been a recent recording in this area");
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                ((MainActivity) activity).setErrorMessage(error.getMessage());
                                            }
                                        });
                                        queue.add(request);
                                    } else {
                                        ((MainActivity) activity).setErrorMessage("Unable to get an accurate device location");
                                    }
                                }
                            });

                        } else {
                            ((MainActivity) activity).setErrorMessage("Microphone is not plugged in");
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
                } else {
                    locationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                Location location = task.getResult();
                                DataPoint toUpload = new DataPoint(System.currentTimeMillis(), location.getLatitude(), location.getLongitude(), dBvalue);

                                // create new node in Firebase
                                data.push().setValue(toUpload);
                                if (user != null) {
                                    user.push().setValue(toUpload);
                                }
                                upload.setVisibility(View.GONE);
                            } else {
                                ((MainActivity) activity).setErrorMessage("Was unable to get an accurate location");
                            }
                        }
                    });
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

    public void updateFragment() {
        data = FirebaseDatabase.getInstance().getReference(prefs.getString(getString(R.string.data_source_pref), "iOS"));
    }

    private boolean isMicPluggedIn() {
        boolean micIn = false;
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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
}
