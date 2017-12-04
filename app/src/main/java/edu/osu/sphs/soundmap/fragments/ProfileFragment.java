package edu.osu.sphs.soundmap.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

import edu.osu.sphs.soundmap.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "ProfileFragment";
    private MapView mapView;
    private GoogleMap googleMap;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.map_background);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Random r = new Random(System.currentTimeMillis());
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (int i = 0; i < 10; i++) {
            r.setSeed(System.currentTimeMillis() * (System.currentTimeMillis() % 532));
            double lat = (r.nextDouble() * .3) + 39.8;
            double lon = (r.nextDouble() * .8) - 83.3;
            LatLng latLng = new LatLng(lat, lon);
            builder.include(latLng);
            Log.d(TAG, "onMapReady: latLng " + latLng.toString());
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Marker " + i));
        }
        mapView.onResume();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
    }
}
