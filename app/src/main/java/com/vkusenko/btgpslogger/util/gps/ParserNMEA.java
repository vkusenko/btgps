package com.vkusenko.btgpslogger.util.gps;

import com.vkusenko.btgpslogger.common.CurrentLocation;
import com.vkusenko.btgpslogger.events.MockProviderEvent;
import com.vkusenko.btgpslogger.util.gps.MockLocationProvider;

import org.greenrobot.eventbus.EventBus;

public class ParserNMEA {

    private CurrentLocation currentLocation;
    private MockLocationProvider mockLocationProvider;
    private float precision = 10f;

    public ParserNMEA() {
        currentLocation = new CurrentLocation();
        MockProviderEvent mockProviderEvent = EventBus.getDefault().removeStickyEvent(MockProviderEvent.class);
        if (mockProviderEvent != null) {
            mockLocationProvider = mockProviderEvent.mockLocationProvider;
        }
    }

    public void bufferParser(String in) {
        String[] arrayMessage = in.split("\\$");
        for (String x : arrayMessage) {
            if ((x.indexOf("\n") >= 0) && (x.indexOf("G") == 0)) {
                lineParser(x);
            }
        }
        if (currentLocation.getmLatitude() > 0)
            mockLocationProvider.pushLocation(currentLocation);

        EventBus.getDefault().postSticky(currentLocation);
    }

    private void lineParser(String line) {
        String [] data = line.split(",");
        StringBuilder sb;
        switch (data[0]) {
            case "GPGGA" :
                sb = new StringBuilder(data[1]);
                sb = new StringBuilder(sb.substring(0, 6));
                sb.insert(2, ":");
                sb.insert(5, ":");
                currentLocation.setStrTime(sb.toString());

                double lat = parseNmeaLatitude(data[2], data[3]);
                currentLocation.setStrLat(getDMS(lat, data[3]));
                currentLocation.setmLatitude(lat);

                double lon = parseNmeaLongitude(data[4], data[5]);
                currentLocation.setStrLon(getDMS(lon, data[5]));
                currentLocation.setmLongitude(lon);

                currentLocation.setStrAlt(data[9]);
                currentLocation.setmAltitude(Double.parseDouble(data[9]));
                currentLocation.setmAccuracy(Float.parseFloat(data[8]) * precision);
                currentLocation.setStrSatellites(data[7]);
                break;
            case "GPRMC" :
                sb = new StringBuilder(data[9]);
                sb.insert(2, "-");
                sb.insert(5, "-");
                currentLocation.setStrDate(sb.toString());
                break;
            case "GPVTG" :
                currentLocation.setStrCourse(data[1]);
                //float speed = parseNmeaSpeed(data[7], data[8]);
                currentLocation.setStrSpeed(data[7]);
                break;
        }
    }

    public float parseNmeaSpeed(String speed,String metric){
        float meterSpeed = 0.0f;
        if (speed != null && metric != null && !speed.equals("") && !metric.equals("")){
            float temp1 = Float.parseFloat(speed)/3.6f;
            if (metric.equals("K")){
                meterSpeed = temp1;
            } else if (metric.equals("N")){
                meterSpeed = temp1*1.852f;
            }
        }
        return meterSpeed;
    }

    private double parseNmeaLatitude(String lat,String orientation){
        double latitude = 0.0;
        if (lat != null && orientation != null && !lat.equals("") && !orientation.equals("")){
            double temp1 = Double.parseDouble(lat);
            double temp2 = Math.floor(temp1/100);
            double temp3 = (temp1/100 - temp2)/0.6;
            if (orientation.equals("S")){
                latitude = -(temp2+temp3);
            } else if (orientation.equals("N")){
                latitude = (temp2+temp3);
            }
        }
        return latitude;
    }
    private double parseNmeaLongitude(String lon,String orientation){
        double longitude = 0.0;
        if (lon != null && orientation != null && !lon.equals("") && !orientation.equals("")){
            double temp1 = Double.parseDouble(lon);
            double temp2 = Math.floor(temp1/100);
            double temp3 = (temp1/100 - temp2)/0.6;
            if (orientation.equals("W")){
                longitude = -(temp2+temp3);
            } else if (orientation.equals("E")){
                longitude = (temp2+temp3);
            }
        }
        return longitude;
    }

    private String getDMS(double value, String orientation) {
        StringBuilder sb = new StringBuilder();
        value = Math.abs(value);
        int degrees = (int) value;
        double decMinutesSeconds = value - degrees;
        double minuteValue = decMinutesSeconds * 60;
        int minutes = (int) minuteValue;
        double secsValue = (minuteValue - minutes) * 60;
        sb.append(degrees);
        sb.append("\u00B0");
        sb.append(minutes);
        sb.append("'");
        sb.append(String.format("%.1f", secsValue));
        sb.append("\" ");
        sb.append(orientation);
        return sb.toString();
    }
}
