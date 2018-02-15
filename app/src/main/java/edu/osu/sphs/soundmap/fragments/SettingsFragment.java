package edu.osu.sphs.soundmap.fragments;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import edu.osu.sphs.soundmap.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    private FirebaseAuth auth;
    private Preference logoutButton;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        auth = FirebaseAuth.getInstance();
        logoutButton = findPreference(getString(R.string.logout_preference));
        if (auth.getCurrentUser() == null) {
            logoutButton.setEnabled(false);
        }

        logoutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                auth.signOut();
                logoutButton.setEnabled(false);
                return true;
            }
        });
    }
}
