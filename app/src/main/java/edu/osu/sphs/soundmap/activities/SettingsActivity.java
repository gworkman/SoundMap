package edu.osu.sphs.soundmap.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.fragments.SettingsFragment;
import edu.osu.sphs.soundmap.util.Values;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.SettingsListener {

    private static final String passcode = "BUCKS";
    private boolean logout = false;
    private SharedPreferences prefs;
    private float calibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        calibration = Float.valueOf(prefs.getString(Values.CALIBRATION_PREF, "0.0"));
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
}
