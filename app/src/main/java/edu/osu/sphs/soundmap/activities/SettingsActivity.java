package edu.osu.sphs.soundmap.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.fragments.SettingsFragment;
import edu.osu.sphs.soundmap.util.Values;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.SettingsListener {

    private static final String passcode = "BUCKS";
    private boolean logout = false;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SettingsFragment fragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();
        fragment.setSettingsListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (logout) setResult(Values.SETTINGS_LOG_OUT);
                else setResult(RESULT_OK);
                finish();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void logOut() {
        this.logout = true;
    }

    @Override
    public void calibrate() {
        float calibration = 0f;
        try {
            calibration = prefs.getFloat(Values.CALIBRATION_PREF, 0);
        } catch (ClassCastException e) {
            // do nothing
        }

        final View view = getLayoutInflater().inflate(R.layout.dialog_calibrate, null);
        final EditText password = view.findViewById(R.id.dialog_passcode);
        final RelativeLayout numberContainer = view.findViewById(R.id.dialog_calibration_container);
        final NumberPicker sign = view.findViewById(R.id.dialog_calibration_sign);
        final NumberPicker decimals = view.findViewById(R.id.dialog_calibration_decimals);
        final NumberPicker digits = view.findViewById(R.id.dialog_calibration_digits);

        sign.setDisplayedValues(new String[]{"+", "-"});
        sign.setMaxValue(1);
        digits.setMaxValue(20);
        decimals.setMaxValue(9);

        if (calibration < 0) {
            sign.setValue(2);
            calibration = calibration * -1;
        }

        Log.d("SettingsActivity", "calibrate: calibration is " + calibration);
        digits.setValue((int) calibration);
        decimals.setValue((int) ((calibration - (int) calibration) * 10));
        Log.d("SettingsActivity", "calibrate: decimals is " + (int) (calibration - (int) calibration) * 10);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Calibration Password")
                .setMessage("In order to ensure the accuracy of measurements included in the study, the calibration of devices is restricted. Please contact the administrators for more information")
                .setView(view)
                .setPositiveButton("Next", null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positive.setOnClickListener(new View.OnClickListener() {
                    boolean firstClick = true;

                    @Override
                    public void onClick(View v) {
                        if (firstClick) {
                            if (password.getText().toString().equals(passcode)) {
                                password.setVisibility(View.GONE);
                                numberContainer.setVisibility(View.VISIBLE);
                                dialog.setTitle("Calibration Value");
                                dialog.setMessage("Enter the calibration offset below");
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            } else {
                                dialog.dismiss();
                                Toast.makeText(SettingsActivity.this, "Password was incorrect, calibration not updated", Toast.LENGTH_SHORT).show();
                            }
                            firstClick = false;
                        } else {
                            float cal = (float) (digits.getValue() + .1 * decimals.getValue());
                            if (sign.getValue() == 1) cal *= -1;
                            prefs.edit().putFloat(Values.CALIBRATION_PREF, cal).apply();
                            Toast.makeText(SettingsActivity.this, "calibration updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.show();
    }
}
