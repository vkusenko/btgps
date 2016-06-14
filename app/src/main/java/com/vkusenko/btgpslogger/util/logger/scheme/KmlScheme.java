package com.vkusenko.btgpslogger.util.logger.scheme;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class KmlScheme extends TrackScheme {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");

    @Override
    public String getHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("\n<kml xmlns=\"http://www.opengis.net/kml/2.2\"> <Document>");
        sb.append("\n<name>Track</name>");
        sb.append("\n<Style id=\"red\">");
        sb.append("\n<LineStyle>");
        sb.append("\n<color>C81400FF</color>");
        sb.append("\n<width>4</width>");
        sb.append("\n</LineStyle>");
        sb.append("\n</Style>");
        sb.append("\n<Placemark>");
        sb.append("\n<name>");
        sb.append(sdf.format(new GregorianCalendar().getTime()));
        sb.append("</name>");
        sb.append("\n<description>Track no. 1</description>");
        sb.append("\n<styleUrl>#red</styleUrl>");
        sb.append("\n<LineString>");
        sb.append("\n<extrude>1</extrude>");
        sb.append("\n<tessellate>1</tessellate>");
        sb.append("\n<altitudeMode>absolute</altitudeMode>");
        sb.append("\n<coordinates>");

        return sb.toString();
    }

    @Override
    public String getBody(Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(location.getLongitude());
        sb.append(",");
        sb.append(location.getLatitude());
        sb.append(",");
        sb.append(location.getAltitude());

        return sb.toString();
    }

    @Override
    public String getFooter() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n</coordinates>");
        sb.append("\n</LineString> </Placemark>");
        sb.append("\n</Document> </kml>");

        return sb.toString();
    }
}
