package edu.osu.sphs.soundmap.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
    private FirebaseAuth auth;
    private DatabaseReference data;
    private SharedPreferences prefs;
    private ArrayList<DataPoint> points = new ArrayList<>();
    private ArrayList<DataPoint> userPoints = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViews();
        setupBottomNav();
        setupFirebase();
        setupPager();
        setupFab();
    }

    /**
     * Gets the view of objects in MainActivity using the IDs.
     */
    private void getViews(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager = findViewById(R.id.viewPager);
        fab = findViewById(R.id.fab);
        upload = findViewById(R.id.fab_upload);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
        fragments.add(MapFragment.newInstance(this.points));
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
    }

    /**
     * setupFirebase() initializes the Firebase objects, including the auth and the auth state listener.
     */
    private void setupFirebase() {
        this.auth = FirebaseAuth.getInstance();
        this.auth.addAuthStateListener(this);

        this.data = FirebaseDatabase.getInstance().getReference(prefs.getString(getString(R.string.data_source_pref), "iOS"));
        this.data.addValueEventListener(this);

        this.user = this.auth.getCurrentUser().getUid();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        this.fragments.remove(2);
        if (firebaseAuth.getCurrentUser() != null) {
            this.fragments.add(2, ProfileFragment.newInstance());
        } else {
            this.fragments.add(2, LoginFragment.newInstance());
        }
        this.pagerAdapter.notifyDataSetChanged();
        this.viewPager.invalidate();
    }

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
                startActivity(intent);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot child : dataSnapshot.getChildren()) {
            DataPoint point = child.getValue(DataPoint.class);
            if (point != null) {
                points.add(point);
                // filter the user's recorded points
//                if (point.getUser().equals(this.user)) {
//                    userPoints.add(point);
//                }
            }
        }

        ((MapFragment) fragments.get(Values.MAP_FRAGMENT_POSITION)).updateView();
        ((ProfileFragment) fragments.get(Values.PROFILE_FRAGMENT_POSITION)).updateList();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
