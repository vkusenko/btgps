package com.vkusenko.btgpslogger.util.logger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.vkusenko.btgpslogger.R;
import com.vkusenko.btgpslogger.common.Maths;
import com.vkusenko.btgpslogger.util.logger.scheme.GpxScheme;
import com.vkusenko.btgpslogger.util.logger.scheme.KmlScheme;
import com.vkusenko.btgpslogger.util.logger.scheme.TrackScheme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class RecordTrack extends Thread {

    private String fileName;
    private String fileNameSuffix;
    private TrackScheme trackScheme;
    private boolean flag = true;
    private long interval;
    private int countPoint = 0;
    private LocationManager locationManager;
    private Location currentLocation = null;
    private boolean fixLocation = false;

    private SimpleDateFormat sdfName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private String filesDir;
    private Activity activity;

    private double lastLon;
    private double lastLat;
    private double distance;
    private double allDistance;

    private SharedPreferences sharedPreferences;

    public RecordTrack(String filesDir, Activity activity) {
        this.filesDir = filesDir;
        this.activity = activity;
        init();
    }

    private void init() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String defInterval = activity.getString(R.string.default_interval_recording);
        String prefInterval = sharedPreferences.getString("interval_points", defInterval);
        interval = (prefInterval == null || prefInterval.isEmpty()) ? Integer.parseInt(defInterval) : Integer.parseInt(prefInterval);
        interval *= 1000;

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        initScheme();
    }

    private void initScheme() {
        String defScheme = activity.getString(R.string.default_track_scheme);
        String scheme = sharedPreferences.getString("scheme_track", defScheme);
        switch (scheme) {
            case "KML":
                trackScheme = new KmlScheme();
                fileNameSuffix = ".kml";
                break;
            case "GPX":
                trackScheme = new GpxScheme();
                fileNameSuffix = ".gpx";
                break;
        }

        fileName = filesDir + File.separator + sdfName.format(new GregorianCalendar().getTime()) + fileNameSuffix;
    }

    public boolean getFixLocation() {
        return fixLocation;
    }

    public int getCountPoint() {
        return countPoint;
    }

    public double getAllDistance() {
        return allDistance;
    }

    @Override
    public void run() {
        super.run();
        while (flag) {
            if (currentLocation != null) {
                if (countPoint == 0) {
                    putHeader();
                }
                countPoint++;
                fixLocation = true;
                putBody();
                if (lastLon != 0) {
                    distance = getDistance();
                    allDistance = allDistance + distance;
                }
                lastLon = currentLocation.getLongitude();
                lastLat = currentLocation.getLatitude();
                currentLocation = null;
            } else {
                fixLocation = false;
            }

            try {
                sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (countPoint > 0) {
            putFooter();
        }

    }

    private double getDistance() {
        return Maths.calculateDistance(lastLat, lastLon, currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    public void finish() {
        flag = false;
        locationManager.removeUpdates(locationListener);
    }

    private void putHeader() {
        writer(trackScheme.getHeader());
    }

    private void putBody() {
        writer(trackScheme.getBody(currentLocation));
    }

    private void putFooter() {
        writer(trackScheme.getFooter());
    }

    private void writer(String stringLine) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(fileName), true));
            bw.write(stringLine);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
