package edu.osu.sphs.soundmap.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by Gus Workman on 11/22/2017. This is the launcher activity for the application. It shows
 * a splash screen while the resources are loading and forwards the user to the MainActivity. To
 * change the splash screen, change the drawable in @drawable/splash.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // forwards on to the main activity after resources load
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
