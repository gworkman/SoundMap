package edu.osu.sphs.soundmap.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.fragments.MeasureFragment;
import edu.osu.sphs.soundmap.util.ViewPagerAdapter;

/**
 * Created by Gus Workman on 11/22/2017. This is the main activity for the application
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private FloatingActionButton fab;
    private boolean fabIsSetup = false;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViews();
        setupBottomNav();
        setupPager();
        setupFirebase();
    }

    /**
     * Gets the view of objects in MainActivity using the IDs.
     */
    private void getViews(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager = findViewById(R.id.viewPager);
        fab = findViewById(R.id.fab);
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
                        if (bottomNavigationView.getSelectedItemId() != R.id.menu_measure) fab.setVisibility(View.INVISIBLE);
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
                        if (bottomNavigationView.getSelectedItemId() != R.id.menu_measure) fab.setVisibility(View.INVISIBLE);
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
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                MenuItem item = bottomNavigationView.getMenu().getItem(position);
                bottomNavigationView.setSelectedItemId(item.getItemId());
                if (position == 1) fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int translation;
                switch (position) {
                    case 1:
                        translation = (int) (-2 * fab.getHeight() + 2 * fab.getHeight() * positionOffset);
                        if (translation != 0) fab.setTranslationY(translation);
                        break;
                    default:
                        translation = (int) (-2 * fab.getHeight() * positionOffset);
                        if (translation != 0) fab.setTranslationY(translation);
                        break;
                }
            }
        });
    }

    /**
     * setupFab() adds the onClickListener to the fab using the MeasureFragment in the viewPager.
     * This method should only be called once to increase performance.
     */
    private void setupFab() {
        MeasureFragment measureFragment = (MeasureFragment) getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem()
        );
        fab.setOnClickListener(measureFragment);
        fabIsSetup = true;
    }

    private void setupFirebase() {
        this.auth = FirebaseAuth.getInstance();
    }


}
