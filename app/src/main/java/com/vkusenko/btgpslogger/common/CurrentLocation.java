package com.vkusenko.btgpslogger.common;

public class CurrentLocation {

    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private double mAltitude = 0.0f;
    private float mSpeed = 0.0f;
    private float mBearing = 0.0f;
    private float mAccuracy = 0.0f;

    private String
            strTime,
            strDate,
            strLat,
            strLon,
            strAlt,
            strSpeed,
            strCourse,
            strSatellites;

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getmAltitude() {
        return mAltitude;
    }

    public void setmAltitude(double mAltitude) {
        this.mAltitude = mAltitude;
    }

    public float getmSpeed() {
        return mSpeed;
    }

    public void setmSpeed(float mSpeed) {
        this.mSpeed = mSpeed;
    }

    public float getmBearing() {
        return mBearing;
    }

    public void setmBearing(float mBearing) {
        this.mBearing = mBearing;
    }

    public float getmAccuracy() {
        return mAccuracy;
    }

    public void setmAccuracy(float mAccuracy) {
        this.mAccuracy = mAccuracy;
    }

    public String getStrTime() {
        return strTime;
    }

    public void setStrTime(String strTime) {
        this.strTime = strTime;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }

    public String getStrLat() {
        return strLat;
    }

    public void setStrLat(String strLat) {
        this.strLat = strLat;
    }

    public String getStrLon() {
        return strLon;
    }

    public void setStrLon(String strLon) {
        this.strLon = strLon;
    }

    public String getStrAlt() {
        return strAlt;
    }

    public void setStrAlt(String strAlt) {
        this.strAlt = strAlt;
    }

    public String getStrSpeed() {
        return strSpeed;
    }

    public void setStrSpeed(String strSpeed) {
        this.strSpeed = strSpeed;
    }

    public String getStrCourse() {
        return strCourse;
    }

    public void setStrCourse(String strCourse) {
        this.strCourse = strCourse;
    }

    public String getStrSatellites() {
        return strSatellites;
    }

    public void setStrSatellites(String strSatellites) {
        this.strSatellites = strSatellites;
    }
}
