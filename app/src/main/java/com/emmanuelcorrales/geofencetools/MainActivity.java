package com.emmanuelcorrales.geofencetools;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
        OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {
    private static final int REQUEST_PERMISSION_LOCATION = 34839;

    private GoogleMap mMap;
    private List<Marker> mMarkers = new ArrayList<>();
    private Polygon mPolygon;
    private Marker mCenterMarker;
    private Circle mCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).draggable(true)));
        mMap.setOnMarkerDragListener(this);
        redraw();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        redraw();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private void redraw() {
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
    }

    private CircleOptions getCircularFence() {
        double latMin = Double.MAX_VALUE;
        double latMax = Double.MIN_VALUE;
        double longMin = Double.MAX_VALUE;
        double longMax = Double.MIN_VALUE;

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
        double radius = computeDistance(latMid, longMid, latMax, longMax);
        return new CircleOptions()
                .center(new LatLng(latMid, longMid))
                .radius(radius)
                .strokeColor(Color.GREEN);
    }

    public static double computeDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        int meterConversion = 1609;
        return distance * meterConversion;
    }

    private static List<LatLng> convertMarkersToLatlng(List<Marker> markers) {
        List<LatLng> polygonPoints = new ArrayList<>();
        for (Marker marker : markers) {
            polygonPoints.add(marker.getPosition());
        }
        return polygonPoints;
    }
}