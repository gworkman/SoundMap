package edu.osu.sphs.soundmap.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.osu.sphs.soundmap.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeasureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeasureFragment extends Fragment implements View.OnClickListener {

    private TextView timer;
    private FloatingActionButton fab;
    private boolean isRunning = false;
    private CountDownTimer chronometer;

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
        timer = view.findViewById(R.id.timer);
    }

    // This is the onClick method for the fab in MainActivity
    @Override
    public void onClick(View v) {
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
                }
            }.start();
            fab.setImageResource(R.drawable.ic_stop);
            isRunning = true;
        } else {
            chronometer.cancel();
            String timerResetValue = "30.0 seconds";
            timer.setText(timerResetValue);
            fab.setImageResource(R.drawable.ic_record);
            isRunning = false;
        }
    }
}
