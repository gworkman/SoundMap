package edu.osu.sphs.soundmap.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.fragments.LoginFragment;
import edu.osu.sphs.soundmap.fragments.MapFragment;
import edu.osu.sphs.soundmap.fragments.MeasureFragment;
import edu.osu.sphs.soundmap.fragments.ProfileFragment;
import edu.osu.sphs.soundmap.util.DataPoint;
import edu.osu.sphs.soundmap.util.Values;
import edu.osu.sphs.soundmap.util.ViewPagerAdapter;

/**
 * Created by Gus Workman on 11/22/2017. This is the main activity for the application
 */
public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener,
        ViewPager.OnPageChangeListener, ValueEventListener {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private FloatingActionButton fab;
    private FloatingActionButton upload;
    private CoordinatorLayout coordinator;
    private FirebaseAuth auth;
    private List<DataPoint> points = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    private DatabaseReference data;
    private boolean fabIsSetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViews();
        setupBottomNav();
        setupFirebase();
        setupPager();
    }

    /**
     * Gets the view of objects in MainActivity using the IDs.
     */
    private void getViews(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager = findViewById(R.id.viewPager);
        fab = findViewById(R.id.fab);
        upload = findViewById(R.id.fab_upload);
        coordinator = findViewById(R.id.coordinator);
    }

    /**
     * Add the onClickListener for the bottomNavigationView.
     */
    private void setupBottomNav(){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_map:
                        // required for a smooth transition with the fab
                        if (bottomNavigationView.getSelectedItemId() != R.id.menu_measure) {
                            fab.setVisibility(View.INVISIBLE);
                            upload.setVisibility(View.INVISIBLE);
                        }
                        // scroll to the map fragment
                        viewPager.setCurrentItem(0, true);
                        break;
                    case R.id.menu_measure:
                        // scroll to the measure fragment
                        viewPager.setCurrentItem(1, true);
                        if (!fabIsSetup) setupFab();
                        break;
                    case R.id.menu_profile:
                        // required for a smooth transition with the fab
                        if (bottomNavigationView.getSelectedItemId() != R.id.menu_measure) {
                            fab.setVisibility(View.INVISIBLE);
                            upload.setVisibility(View.INVISIBLE);
                        }
                        // scroll to the profile fragment
                        viewPager.setCurrentItem(2,true);
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Populate the viewPager and adapter with fragments and update the bottomNavigationView selected
     * item on page scroll.
     */
    private void setupPager() {
        fragments.add(MapFragment.newInstance());
        fragments.add(MeasureFragment.newInstance());
        if (auth.getCurrentUser() != null) {
            fragments.add(ProfileFragment.newInstance());
        } else {
            fragments.add(LoginFragment.newInstance());
        }

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setOffscreenPageLimit(2);
    }

    /**
     * setupFab() adds the onClickListener to the fab using the MeasureFragment in the viewPager.
     * This method should only be called once to increase performance.
     */
    private void setupFab() {
        MeasureFragment measureFragment = (MeasureFragment) fragments.get(1);
        fab.setOnClickListener(measureFragment);
        upload.setOnClickListener(measureFragment);
        upload.callOnClick();
        fabIsSetup = true;
    }

    /**
     * setupFirebase() initializes the Firebase objects, including the auth and the auth state listener.
     */
    private void setupFirebase() {
        this.auth = FirebaseAuth.getInstance();
        this.auth.addAuthStateListener(this);
        this.data = FirebaseDatabase.getInstance().getReference(Values.DATA_REFERNCE);
        this.data.addValueEventListener(this);
    }

    /*
     * This method implements the AuthStateListener for FirebaseAuth
     */

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        Log.d(TAG, "onAuthStateChanged: auth state was changed, yo");
        this.fragments.remove(2);
        if (firebaseAuth.getCurrentUser() != null) {
            this.fragments.add(2, ProfileFragment.newInstance());
        } else {
            this.fragments.add(2, LoginFragment.newInstance());
        }
        this.pagerAdapter.notifyDataSetChanged();
        this.viewPager.invalidate();
    }

    /*
     * These methods implement the firebase ValueEventListener interface
     */

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        this.points.clear();
        for (DataSnapshot child : dataSnapshot.getChildren()) {
            DataPoint point = child.getValue(DataPoint.class);
            this.points.add(point);
        }

        Collections.sort(points, new DataPoint.Compare());
        ((MapFragment) fragments.get(0)).updateFragment();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        setErrorMessage(databaseError.getMessage());
    }

    /*
     * These methods all implement the viewpager OnPageChangeListener
     */

    @Override
    public void onPageSelected(int position) {
        MenuItem item = bottomNavigationView.getMenu().getItem(position);
        bottomNavigationView.setSelectedItemId(item.getItemId());
        if (position == 1) {
            fab.setVisibility(View.VISIBLE);
            //upload.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int translation;
        switch (position) {
            case 1:
                translation = (int) (-2 * fab.getHeight() + 2 * fab.getHeight() * positionOffset);
                if (translation != 0) {
                    fab.setTranslationY(translation);
                    upload.setTranslationY(translation);
                }
                break;
            default:
                translation = (int) (-2 * fab.getHeight() * positionOffset);
                if (translation != 0) {
                    fab.setTranslationY(translation);
                    upload.setTranslationY(translation);
                }
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // do nothing! yay
    }

    /*
     * These implement standard activity methods for options menus
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, Values.SETTINGS_RESULT_CODE);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Values.SETTINGS_RESULT_CODE) {
            if (resultCode == Values.SETTINGS_CHANGED) {
                ((MapFragment) fragments.get(0)).updateFragment();
            }
        }
    }

    /*
     * These are methods that implement Fragment Callback
     */

    public void addPointsToMap(GoogleMap map) {
        for (DataPoint point : points) {
            if (point.getDecibels() < 70) {
                map.addMarker(new MarkerOptions().position(new LatLng(point.getLat(), point.getLon()))
                        .title(String.format(Locale.getDefault(), "%.02f dB", point.getDecibels()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            } else if (point.getDecibels() < 90) {
                map.addMarker(new MarkerOptions().position(new LatLng(point.getLat(), point.getLon()))
                        .title(String.format(Locale.getDefault(), "%.02f dB", point.getDecibels()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            } else {
                map.addMarker(new MarkerOptions().position(new LatLng(point.getLat(), point.getLon()))
                        .title(String.format(Locale.getDefault(), "%.02f dB", point.getDecibels()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
    }

    public boolean canRecord(Location location) {
        long time = System.currentTimeMillis() - 86400000;
        if (points.size() > 0) {
            int i = 0;
            DataPoint point;
            do {
                point = points.get(i);
                if (Values.distance(location.getLatitude(), location.getLongitude(), point.getLat(), point.getLon()) < 0.25) {
                    return false;
                }
                i++;
            } while (point.getDate() > time && i < points.size());
        }
        return true;
    }

    public void setErrorMessage(String message) {
        Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG).show();
    }

    public void setErrorMessage(String text, String button, final String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Snackbar.make(coordinator, text, Snackbar.LENGTH_LONG).setAction(button, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialogMessage(message);
                }
            }).setActionTextColor(getResources().getColor(R.color.osuScarlet, getTheme())).show();
        } else {
            Snackbar.make(coordinator, text, Snackbar.LENGTH_LONG).setAction(button, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialogMessage(message);
                }
            }).setActionTextColor(getResources().getColor(R.color.osuScarlet)).show();
        }
    }

    public void showDialogMessage(String message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setTitle("More information")
                .setCancelable(true)
                .create();
        dialog.show();
    }
}
