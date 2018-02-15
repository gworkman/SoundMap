package edu.osu.sphs.soundmap.util;

import android.content.Context;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import edu.osu.sphs.soundmap.R;

/**
 * Created by Gus on 12/8/2017. A class to manange the behavior of the recycler view in the profile fragment
 */

public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.ViewHolder> {

    private List<DataPoint> recordings;
    private Context context;
    private Geocoder geocoder;

    public RecordingListAdapter(Context context, List<DataPoint> items) {
        this.context = context;
        this.recordings = items;
        this.geocoder = new Geocoder(this.context, Locale.getDefault());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_list, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataPoint data = recordings.get(position);
        String timestamp = data.getTimeString(this.context);
        holder.date.setText(timestamp);
        String near = data.getNear(geocoder);
        holder.latlong.setText(near);
        holder.slevel.setText(String.format(Locale.getDefault(), "%.02f dB", data.getDecibels()));
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView date, latlong, slevel;

        private ViewHolder(View v) {
            super(v);
            this.date = v.findViewById(R.id.recording_list_date);
            this.latlong = v.findViewById(R.id.recording_list_location);
            this.slevel = v.findViewById(R.id.recording_list_slevel);
        }
    }
}
