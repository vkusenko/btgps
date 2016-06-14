package com.vkusenko.btgpslogger.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vkusenko.btgpslogger.R;
import com.vkusenko.btgpslogger.common.FileManager;
import com.vkusenko.btgpslogger.common.Strings;
import com.vkusenko.btgpslogger.events.RecordTrackEvent;
import com.vkusenko.btgpslogger.service.RecordTrackService;
import com.vkusenko.btgpslogger.util.logger.RecordTrack;
import com.vkusenko.btgpslogger.common.Stopwatch;

import org.greenrobot.eventbus.EventBus;

public class TrackerFragment extends Fragment {

    private RecordTrack recordTrack;
    private TextView txtTimeTrack;
    private TextView txtCountPoint;
    private TextView txtDistance;
    private TextView txtTrackWaitGPS;
    private Button btnStartStopTrack;
    private boolean flag = false;
    private RecordTrackAsync recordTrackAsync;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tracker_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        txtTimeTrack = (TextView) getActivity().findViewById(R.id.txtTimeTrack);
        txtCountPoint = (TextView) getActivity().findViewById(R.id.txtCountPoint);
        txtDistance = (TextView) getActivity().findViewById(R.id.txtDistance);
        txtTrackWaitGPS = (TextView) getActivity().findViewById(R.id.txtTrackWaitGPS);
        btnStartStopTrack = (Button) getActivity().findViewById(R.id.btnStartStopTrack);
        btnStartStopTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopRecord();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (flag) {
            btnStartStopTrack.setText(getString(R.string.stop));
        } else {
            btnStartStopTrack.setText(getString(R.string.start));
        }
    }

    private void startStopRecord() {
        if (flag) {
            flag = false;
            getActivity().stopService(new Intent(getActivity().getApplicationContext(), RecordTrackService.class));
            recordTrackAsync.cancel(false);
            btnStartStopTrack.setText(getString(R.string.start));
            txtTrackWaitGPS.setVisibility(View.GONE);
        } else {
            FileManager fileManager = new FileManager(getActivity());
            String path = fileManager.getPath();

            if (path != null) {
                recordTrack = new RecordTrack(path, getActivity());
                flag = true;
                txtTrackWaitGPS.setVisibility(View.VISIBLE);

                EventBus.getDefault().postSticky(new RecordTrackEvent(recordTrack));
                getActivity().startService(new Intent(getActivity().getApplicationContext(), RecordTrackService.class));
                recordTrackAsync = new RecordTrackAsync();
                recordTrackAsync.execute();
                btnStartStopTrack.setText(getString(R.string.stop));
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        getActivity().getString(R.string.error_data_folder),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class RecordTrackAsync extends AsyncTask<Void, String, Void> {
        private Stopwatch stopwatch;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            stopwatch = new Stopwatch();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                publishProgress(
                        stopwatch.getElapsedTime(),
                        Integer.toString(recordTrack.getCountPoint()),
                        Strings.getDistanceDisplay(getActivity().getApplicationContext(), recordTrack.getAllDistance()));

                SystemClock.sleep(1000);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            txtTimeTrack.setText(values[0]);
            txtCountPoint.setText(values[1]);
            txtDistance.setText(values[2]);

            if (recordTrack.getFixLocation()) {
                txtTrackWaitGPS.setVisibility(View.GONE);
            } else {
                txtTrackWaitGPS.setVisibility(View.VISIBLE);
            }
        }
    }
}
