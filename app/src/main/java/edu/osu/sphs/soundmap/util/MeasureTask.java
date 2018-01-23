package edu.osu.sphs.soundmap.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Gus on 12/3/2017. A tool to get the dB value in the background asynchronously
 */

public class MeasureTask extends AsyncTask<String, Double, Double> {

    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCELLED = -1;

    private static final String TAG = "MeasureTask";
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;

    private AudioRecord recorder;
    private OnUpdateCallback callback;
    private DoubleFFT_1D transform = new DoubleFFT_1D(8192);
    private long endTime;
    private double calibration = 0;

    public MeasureTask setCallback(OnUpdateCallback callback) {
        this.callback = callback;
        this.calibration = this.callback.getCalibration();
        return this;
    }

    public void removeCallback() {
        this.callback = null;
    }

    @Override
    protected Double doInBackground(String... files) {
        double average = 0;
        int count = 0;

        if (files.length > 0) {

            FileOutputStream os;
            try {
                os = new FileOutputStream(files[0]);

                int bufferSize = 8192; // 2 ^ 13, necessary for the fft
                Log.d(TAG, "doInBackground: bufferSize is " + bufferSize);
                recorder = new AudioRecord(SOURCE, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize);

                short[] buffer = new short[bufferSize];
                long totalRead = 0;
                int sampleLength = 0;
                endTime = System.currentTimeMillis() + 30000;
                recorder.startRecording();

                Log.d(TAG, "doInBackground: recording started");
                while (System.currentTimeMillis() < endTime) {
                    if (isCancelled()) break;
                    sampleLength = recorder.read(buffer, 0, bufferSize);
                    //os.write(buffer, 0, buffer.length); for writing data to output file; buffer must be byte
                    average += averageDB(doFFT(buffer));
                    count++;
                    publishProgress(average / count);
                    totalRead += sampleLength;
                }

                os.close();
                Log.d(TAG, "doInBackground: recording ended");
                Log.d(TAG, "doInBackground: average is " + average + ". count is " + count);

                recorder.stop();
                recorder.release();
                recorder = null;

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            new Error("Must provide a file output path").printStackTrace();
        }

        return average;
    }

    @Override
    protected void onPostExecute(Double d) {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        if (callback != null) {
            callback.onFinish(RESULT_OK);
        }
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        //Log.d(TAG, "onProgressUpdate: values is " + Arrays.toString(values));
        if (callback != null) {
            callback.onUpdate(69 + values[0]);
        }
    }

    @Override
    protected void onCancelled() {
        Log.d(TAG, "onCancelled: was called");
        endTime = System.currentTimeMillis();
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            Log.d(TAG, "onCancelled: supposedly ended recording");
            // recorder = null;
        }

        if (callback != null) {
            callback.onFinish(RESULT_CANCELLED);
        }
    }

    /**
     * A method that transforms a short array of PCM values to amplitudes of the different bins
     * in a Fourier Transform.
     *
     * @param rawData the array of short PCM values. Can contain zeroes.
     * @return an array of amplitudes of the different bins from a Fourier Transform.
     */
    private double[] doFFT(short[] rawData) {
        double[] fft = new double[2 * rawData.length];

        // get a half-filled array of double values for the fft calculation
        for (int i = 0; i < rawData.length; i++) {
            fft[i] = rawData[i] / (Short.MAX_VALUE * 1.0);
            //if (i < 2) Log.d(TAG, "doFFT: fft position " + i+ " is " + fft[i]);
        }

        // fft
        transform.realForwardFull(fft);

        // calculate the amplitudes
        double[] amplitudes = new double[rawData.length];
        for (int i = 0; i < rawData.length; i++) {
            //                                      reals                     imaginary
            amplitudes[i] = Math.sqrt(Math.pow(fft[2 * i], 2) + Math.pow(fft[2 * i + 1], 2));
        }

        return amplitudes;
    }

    /**
     * Computes the average sound pressure measurement level for an array of SPL amplitudes (from FFT).
     *
     * @param amplitudes the double array to average.
     * @return a double value that represents the average of the time period.
     */
    private double averageDB(double[] amplitudes) {
        double avg = 0.0;
        for (double amp : amplitudes) avg += 20 * Math.log10(amp) + this.calibration;
        return avg / amplitudes.length;
    }

    public interface OnUpdateCallback {
        void onUpdate(double averageDB);
        void onFinish(int result);

        double getCalibration();
    }
}
