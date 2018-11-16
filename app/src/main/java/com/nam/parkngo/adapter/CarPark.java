package com.nam.parkngo.adapter;

import android.location.Address;
import android.location.Location;

import java.util.Locale;

/**
 * Created by Nam on 10/16/2014.
 * This class represent a car park
 */
public class CarPark extends Address
{
    //inherit: featureName, latitude, longitude, bundle, locale, locality, phone, postalcode,
    private String placeid = "";
    private String time = "";
    private String address = "";
    private String website = "";
    private double distance;
    private boolean isDetailed = false;

    public CarPark(String id, String name, double lat, double lng) {
        super(Locale.getDefault());
        if (name == null) name = "null";
        setPlaceid(id);
        setFeatureName(name);
        setLatitude(lat);
        setLongitude(lng);
        setPhone("");
    }

    public String getPlaceid() {
        return placeid;
    }

    public void setPlaceid(String placeid) {
        this.placeid = placeid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public double getDist() {
        return distance;
    }

    public void getDist(Location loc, double unitMultiple)
    {
        float[] results = new float[1];
        if (loc == null) return;
        Location.distanceBetween(loc.getLatitude(),loc.getLongitude(),
                getLatitude(),getLongitude(), results);
        this.distance = results[0]*unitMultiple;
    }

    public void setDist(double dist)
    {
        this.distance = dist;
    }

    public boolean isDetailed() {
        return isDetailed;
    }

    public void setDetailed(boolean isDetailed) {
        this.isDetailed = isDetailed;
    }

    public String toString()
    {
        return placeid
                + "," + getFeatureName().replace(","," -")
                + "," + getLatitude()
                + "," + getLongitude()
                + "," + getAddress().replace(","," -");
    }

    public String toLongString()
    {
        return toString()
                + "," + getPhone().replace(","," -")
                + "," + getTime()
                + "," + getWebsite();
    }

    public String toShare()
    {
        return getAddress().replace(","," -")
                + "\n" + getPhone().replace("|","\n")
                + "\n" + getTime().replace("|","\n")
                + "\n" + getWebsite();
    }
}
