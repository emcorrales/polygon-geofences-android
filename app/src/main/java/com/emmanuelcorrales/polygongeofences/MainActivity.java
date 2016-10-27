package com.emmanuelcorrales.polygongeofences;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION_LOCATION = 34839;
    private static final String GEOFENCE_ID = TAG;

    private GoogleMap mMap;
    private List<Marker> mMarkers = new ArrayList<>();
    private Polygon mPolygon;
    private Marker mCenterMarker;
    private Circle mCircle;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = createGoogleApiClient();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, GeofenceEditorActivity.class));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_LOCATION);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).draggable(true)));
        mMap.setOnMarkerDragListener(this);
        update();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        update();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed()");
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.d(TAG, "onResult()");
    }

    private void update() {
        if (mMarkers.size() <= 2) {
            return;
        }
        if (mPolygon != null) {
            mPolygon.remove();
        }
        mPolygon = mMap.addPolygon(new PolygonOptions().addAll(convertMarkersToLatlng(mMarkers))
                .strokeColor(Color.RED).fillColor(Color.BLUE));

        if (mCircle != null) {
            mCircle.remove();
        }
        mCircle = mMap.addCircle(getCircularFence());

        if (mCenterMarker != null) {
            mCenterMarker.remove();
        }
        mCenterMarker = mMap.addMarker(new MarkerOptions().position(mCircle.getCenter()));

        removeGeofences();
        List<Geofence> geofences = new ArrayList<>();
        Geofence geofence = new Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(mCircle.getCenter().latitude, mCircle.getCenter().longitude,
                        (float) mCircle.getRadius())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(1000)
                .build();
        geofences.add(geofence);
        addGeofences(geofences);
    }

    private CircleOptions getCircularFence() {
        double latMin = 180;
        double latMax = -180;
        double longMin = 180;
        double longMax = -180;

        for (Marker marker : mMarkers) {
            if (marker.getPosition().latitude < latMin) {
                latMin = marker.getPosition().latitude;
            }
            if (marker.getPosition().latitude > latMax) {
                latMax = marker.getPosition().latitude;
            }
            if (marker.getPosition().longitude < longMin) {
                longMin = marker.getPosition().longitude;
            }
            if (marker.getPosition().longitude > longMax) {
                longMax = marker.getPosition().longitude;
            }
        }
        double latMid = (latMin + latMax) / 2;
        double longMid = (longMin + longMax) / 2;
        float[] result = new float[1];
        Location.distanceBetween(latMid, longMid, latMax, longMax, result);
        double radius = result[0];
        return new CircleOptions()
                .center(new LatLng(latMid, longMid))
                .radius(radius)
                .strokeColor(Color.GREEN);
    }

    private GoogleApiClient createGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void addGeofences(List<Geofence> geofenceList) {
        if (geofenceList == null) {
            throw new IllegalArgumentException("Argument 'geofenceList' cannot be null.");
        }

        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Google API client is not connected!");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    createGeofencingRequest(geofenceList),
                    GeofenceTransitionIntentService.newPendingIntent(this, convertMarkersToLatlng(mMarkers))
            ).setResultCallback(this);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION);
        }
    }

    private void removeGeofences() {
        Log.d(TAG, "removeGeofences()");
        if (mGoogleApiClient == null) {
            Log.d(TAG, "Failed to remove geofence because Google API client is null!");
            return;
        }
        List<String> geofences = new ArrayList<>();
        geofences.add(GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geofences);
    }

    private static GeofencingRequest createGeofencingRequest(List<Geofence> geofenceList) {
        if (geofenceList == null) {
            throw new IllegalArgumentException("Argument 'geofenceList' cannot be null.");
        }
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(geofenceList)
                .build();
    }

    private static List<LatLng> convertMarkersToLatlng(List<Marker> markers) {
        List<LatLng> polygonPoints = new ArrayList<>();
        for (Marker marker : markers) {
            polygonPoints.add(marker.getPosition());
        }
        return polygonPoints;
    }
}