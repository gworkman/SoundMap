package edu.osu.sphs.soundmap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import edu.osu.sphs.soundmap.R;

/**
 * Created by Gus Workman on 11/22/2017. This is the launcher activity for the application. It shows
 * a splash screen while the resources are loading and forwards the user to the MainActivity. To
 * change the splash screen, change the drawable in @drawable/splash.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // forwards on to the main activity after resources load
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
