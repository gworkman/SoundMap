package edu.osu.sphs.soundmap.fragments;

import android.content.Context;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import edu.osu.sphs.soundmap.util.DataPoint;
import edu.osu.sphs.soundmap.util.RecordingListAdapter;
import edu.osu.sphs.soundmap.util.Values;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements OnMapReadyCallback, ValueEventListener {

    private static final String TAG = "ProfileFragment";
    private MapView mapView;
    private GoogleMap googleMap;
    private RecyclerView recycler;
    private RecordingListAdapter adapter;
    private List<DataPoint> recordings = new ArrayList<>();
    private DatabaseReference data;
    private FirebaseAuth auth;
    private Context context;

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
        mapView.setClickable(false);

        context = getContext();

        adapter = new RecordingListAdapter(context, recordings);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            data = FirebaseDatabase.getInstance().getReference(Values.USER_NODE).child(auth.getUid());
            data.addValueEventListener(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        mapView.onResume();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        this.recordings.clear();
        for (DataSnapshot point : dataSnapshot.getChildren()) {
            recordings.add(point.getValue(DataPoint.class));
        }

        Collections.sort(recordings, new DataPoint.Compare());
        updateMap();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    private void updateMap() {
        this.googleMap.clear();
        if (recordings.size() > 0) {
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for (DataPoint point : recordings) {
                LatLng latLng = new LatLng(point.getLat(), point.getLon());
                if (point.getDecibels() < 70) {
                    googleMap.addMarker(new MarkerOptions().position(latLng)
                            .title(String.format(Locale.getDefault(), "%.02f dB", point.getDecibels()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                } else if (point.getDecibels() < 90) {
                    googleMap.addMarker(new MarkerOptions().position(latLng)
                            .title(String.format(Locale.getDefault(), "%.02f dB", point.getDecibels()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                } else {
                    googleMap.addMarker(new MarkerOptions().position(latLng)
                            .title(String.format(Locale.getDefault(), "%.02f dB", point.getDecibels()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
                builder.include(latLng);
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
            googleMap.moveCamera(CameraUpdateFactory.zoomOut());
        }

        mapView.onResume();
    }

    public void updateFragment() {
        // do nothing for now
    }
}
