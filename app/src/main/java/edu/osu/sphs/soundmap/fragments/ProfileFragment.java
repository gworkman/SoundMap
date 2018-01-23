package edu.osu.sphs.soundmap.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.util.DataPoint;
import edu.osu.sphs.soundmap.util.RecordingListAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "ProfileFragment";
    private MapView mapView;
    private GoogleMap googleMap;
    private RecyclerView recycler;
    private RecordingListAdapter adapter;
    private ArrayList<DataPoint> recordings;

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
        recycler = view.findViewById(R.id.recycler);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        recordings = new ArrayList<>();

        recordings.add(new DataPoint(getContext(), System.currentTimeMillis(), 40.12473, -83.12452, 45.2));
        recordings.add(new DataPoint(getContext(), 1495189976235L, 40.52485, -83.73542, 70.1));
        recordings.add(new DataPoint(getContext(), 1511468462432L, 39.47432, -83.22146, 95.6));
        recordings.add(new DataPoint(getContext(), 1495487976235L, 40.52485, -83.73542, 73.1));
        recordings.add(new DataPoint(getContext(), 1501460462432L, 39.47432, -83.22146, 90.6));

        Collections.sort(recordings, new DataPoint.Compare());

        adapter = new RecordingListAdapter(recordings);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
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
            googleMap.addMarker(new MarkerOptions().position(latLng));
        }
        mapView.onResume();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
    }
}
