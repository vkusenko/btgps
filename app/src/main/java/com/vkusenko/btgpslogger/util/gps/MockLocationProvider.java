package com.vkusenko.btgpslogger.util.gps;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

import com.vkusenko.btgpslogger.common.CurrentLocation;

public class MockLocationProvider {
    String providerName;
    Context ctx;

    public MockLocationProvider(String name, Context ctx) {
        this.providerName = name;
        this.ctx = ctx;

        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.addTestProvider(providerName, false, false, false, false, true,
                true, true, 0, 5);
        lm.setTestProviderEnabled(providerName, true);
    }

    public void pushLocation(CurrentLocation currentLocation) {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);

        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(currentLocation.getmLatitude());
        mockLocation.setLongitude(currentLocation.getmLongitude());
        mockLocation.setAltitude(currentLocation.getmAltitude());
        mockLocation.setAccuracy(currentLocation.getmAccuracy());
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        lm.setTestProviderLocation(providerName, mockLocation);
    }

    public void shutdown() {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.removeTestProvider(providerName);
    }
}
