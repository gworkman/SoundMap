package edu.osu.sphs.soundmap.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.fragments.SettingsFragment;
import edu.osu.sphs.soundmap.util.Values;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Values.SETTINGS_CHANGED);
                finish();
                break;
            default:
                return false;
        }
        return true;
    }
}
