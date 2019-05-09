package com.jeonghoi.ssumart;

/**
 * Created by remna on 2017-09-04.
 */

public class Location {
    private static double latitude;
    private static double longitude;

    public double getLatitude()
    {
        return this.latitude;
    }

    public double getLongitude()
    {
        return this.longitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

}
