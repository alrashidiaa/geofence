package me.alrashidi.geofencing;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        OnMapReadyCallback


    {

        protected ArrayList<Geofence> mGeofenceList;
        protected GoogleApiClient mGoogleApiClient;
        private Button mAddGeofencesButton;
        private GoogleMap mMap;
        Location mLastLocation;



        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();


        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();


            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


    }



        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Add a marker in Sydney, Australia, and move the camera.
            LatLng MartinHall = new LatLng(39.7095516, -86.1350672);
            mMap.addMarker(new MarkerOptions().position(MartinHall).title("Marker in UIndy"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(MartinHall));
            for (Map.Entry<String, LatLng> entry : Constants.LANDMARKS.entrySet()) {

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(entry.getValue().latitude, entry.getValue().longitude))
                        .radius(40)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(128, 255, 0, 0)));
            }
        }


        public void populateGeofenceList() {
            for (Map.Entry<String, LatLng> entry : Constants.LANDMARKS.entrySet()) {
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(entry.getKey())
                        .setCircularRegion(
                                entry.getValue().latitude,
                                entry.getValue().longitude,
                                Constants.GEOFENCE_RADIUS_IN_METERS
                        )
                        .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());
            }
        }

        protected synchronized void buildGoogleApiClient() {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        @Override
        protected void onStart() {
            super.onStart();
            if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

        @Override
        public void onConnected(Bundle connectionHint) {


        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            // Do something with result.getErrorCode());
        }

        @Override
        public void onConnectionSuspended(int cause) {
            mGoogleApiClient.connect();
        }


        public void addGeofencesButtonHandler(View view) {
            if (!mGoogleApiClient.isConnected()) {
                Toast.makeText(this, "Google API Client not connected!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(this); // Result processed in onResult().
            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            }
        }

        private GeofencingRequest getGeofencingRequest() {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(mGeofenceList);
            return builder.build();
        }

        private PendingIntent getGeofencePendingIntent() {
            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        public void onResult(Status status) {
            if (status.isSuccess()) {
                Toast.makeText(
                        this,
                        "Geofences Added",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                // Get the status code for the error and log it using a user-friendly message.
                String errorMessage = " no Connction sorry";
                        //GeofenceErrorMessages.getErrorString(this, status.getStatusCode());
            }
        }
    }
