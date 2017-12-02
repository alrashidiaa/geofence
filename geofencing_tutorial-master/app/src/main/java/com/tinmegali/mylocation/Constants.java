package com.tinmegali.mylocation;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;


public class Constants {

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 12 * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 20;

    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();
    static {
        // Martin Hall.
        LANDMARKS.put("Martin", new LatLng(39.7103525,-86.1355658));

        // Each Hall
        LANDMARKS.put("Each", new LatLng(39.7088908,-86.1353526));

        // Shwitzer Student Center
        LANDMARKS.put("Shwitzer", new LatLng(39.7098588, -86.1337486));
    }
}