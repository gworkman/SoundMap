package edu.osu.sphs.soundmap.util;

import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.io.IOException;

import static java.lang.Math.log10;

/**
 * Created by Gus on 12/3/2017. A tool to get the dB value in the background asynchronously
 */

public class MeasureTask extends AsyncTask<String, Double, Double> {

    private double average;
    private MediaRecorder recorder;
    private OnUpdateCallback callback;

    public MeasureTask setCallback(OnUpdateCallback callback) {
        this.callback = callback;
        return this;
    }

    public void removeCallback() {
        this.callback = null;
    }

    @Override
    protected Double doInBackground(String... files) {
        if (files.length > 0) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            recorder.setOutputFile(files[0]);

            try {
                recorder.prepare();
                recorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            long endTime = System.currentTimeMillis() + 30000;
            int count = 1;

            while (System.currentTimeMillis() < endTime) {
                double amplitude = recorder.getMaxAmplitude();
                publishProgress(amplitude);

                try {
                    Thread.sleep(333);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;

        } else {
            new Error("Recording file path output parameters must be greater than one").printStackTrace();
        }

        return average;
    }

    @Override
    protected void onPostExecute(Double d) {
        if (recorder != null) {
            recorder.stop();
            recorder.reset();
            recorder.reset();
            recorder = null;
        }
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        if (callback != null) {
            callback.onUpdate(20 * log10(values[0] / 1.0));
        }
    }

    @Override
    protected void onCancelled() {
        if (recorder != null) {
            recorder.stop();
            recorder.reset();
            recorder.reset();
            recorder = null;
        }
    }

    public interface OnUpdateCallback {
        void onUpdate(double averageDB);
    }
}
