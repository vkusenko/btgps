package com.vkusenko.btgpslogger.events;


import android.location.Location;

public class LocationEvent {
    public final Location location;
    public LocationEvent(Location location) {
        this.location = location;
    }
}
