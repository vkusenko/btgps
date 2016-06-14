package com.vkusenko.btgpslogger.util.logger.scheme;

import android.location.Location;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class GpxScheme extends TrackScheme {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");
    private SimpleDateFormat sdfGPX = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public String getHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("\n<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        sb.append("\nversion=\"1.0\"");
        sb.append("\nxmlns=\"http://www.topografix.com/GPX/1/0\"");
        sb.append("\ncreator=\"BTGPS Logger\"");
        sb.append("\nxsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">");
        sb.append("\n<time>");
        sb.append(sdfGPX.format(new GregorianCalendar().getTime()));
        sb.append("</time>");
        sb.append("\n<trk>\n<name>");
        sb.append(sdf.format(new GregorianCalendar().getTime()));
        sb.append("</name>\n<trkseg>");

        return sb.toString();
    }

    @Override
    public String getBody(Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n<trkpt lat=\"");
        sb.append(location.getLatitude());
        sb.append("\" lon=\"");
        sb.append(location.getLongitude());
        sb.append("\"><time>");
        sb.append(sdfGPX.format(new GregorianCalendar().getTime()));
        sb.append("</time><ele>");
        sb.append(location.getAltitude());
        sb.append("</ele><speed>");
        sb.append(location.getSpeed());
        sb.append("</speed>");
        sb.append("</trkpt>");

        return sb.toString();
    }

    @Override
    public String getFooter() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n</trkseg>");
        sb.append("\n</trk>");
        sb.append("\n</gpx>");

        return sb.toString();
    }
}
