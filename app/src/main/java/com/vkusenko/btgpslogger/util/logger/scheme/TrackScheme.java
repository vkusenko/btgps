package com.vkusenko.btgpslogger.util.logger.scheme;

import android.location.Location;

public abstract class TrackScheme {
    public String getHeader() {
        return null;
    }

    public String getBody(Location location) {
        return null;
    }

    public String getFooter() {
        return null;
    }
}
