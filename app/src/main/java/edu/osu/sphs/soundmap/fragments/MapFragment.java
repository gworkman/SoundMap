package edu.osu.sphs.soundmap.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.util.Values;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, OnSuccessListener<Location>, ValueEventListener {

    private MapView mapView;
    private FusedLocationProviderClient locationProviderClient;
    private GoogleMap googleMap;
    private DatabaseReference data;
    private Activity activity;
    private SharedPreferences prefs;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (activity == null) activity = getActivity();
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            locationProviderClient.getLastLocation().addOnSuccessListener(this);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, Values.LOCATION_REQUEST_CODE);
        }
        data = FirebaseDatabase.getInstance().getReference(prefs.getString(getString(R.string.data_source_pref), "iOS"));
        data.addValueEventListener(this);
        mapView.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Values.LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapView.getMapAsync(this);
                }
        }
    }

    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f));
        } else {
            LatLng latLng = new LatLng(prefs.getFloat("last_lat", 40), prefs.getFloat("last_long", -83));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /*
     * Firebase Database ValueEventListener
     */

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (googleMap != null) {
            for (DataSnapshot point : dataSnapshot.getChildren()) {

                // TODO: THIS IS SUPER HACKY, FIX IT
                try {
                    double decibels = Double.valueOf(point.child("Decibels").getValue().toString());
                    double lat = Double.valueOf(point.child("Lat").getValue().toString());
                    double lon = Double.valueOf(point.child("Long").getValue().toString());
                    if (decibels < 70) {
                        googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(decibels + " dB")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    } else if (decibels < 90) {
                        googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(decibels + " dB")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    } else {
                        googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(decibels + " dB")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    }
                } catch (NullPointerException e) {
                    // don't do anything
                }
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
