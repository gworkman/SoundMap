package edu.osu.sphs.soundmap.fragments;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.util.Values;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    private FirebaseAuth auth;
    private Preference logoutButton;
    private Preference aboutButton;
    private Preference calibration;
    private SwitchPreference localOnlyMode;
    private SettingsListener listener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        auth = FirebaseAuth.getInstance();
        logoutButton = findPreference(getString(R.string.logout_preference));
        aboutButton = findPreference(getString(R.string.about_preference));
        calibration = findPreference("calibration_button");
        localOnlyMode = (SwitchPreference) findPreference(getString(R.string.local_only_pref));
        if (auth.getCurrentUser() == null) localOnlyMode.setChecked(true);
        if (auth.getCurrentUser() == null) {
            logoutButton.setEnabled(false);
        }

        logoutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                logoutButton.setEnabled(false);
                if (listener != null) listener.logOut();
                return true;
            }
        });

        aboutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Intent intent = new Intent(getActivity(), AboutActivity.class);
                //startActivity(intent);
                return true;
            }
        });

        calibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (listener != null) {
                    listener.calibrate();
                }
                return true;
            }
        });
    }

    public void setSettingsListener(SettingsListener listener) {
        this.listener = listener;
    }

    public interface SettingsListener {
        void logOut();
        void calibrate();
    }
}
