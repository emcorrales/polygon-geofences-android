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
        redrawPolygon();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        redrawPolygon();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private void redrawPolygon() {
        if (mPolygon != null) {
            mPolygon.remove();
        }
        mPolygon = mMap.addPolygon(new PolygonOptions().addAll(convertMarkersToLatlng(mMarkers))
                .strokeColor(Color.RED).fillColor(Color.BLUE));
    }

    private static List<LatLng> convertMarkersToLatlng(List<Marker> markers) {
        List<LatLng> polygonPoints = new ArrayList<>();
        for (Marker marker : markers) {
            polygonPoints.add(marker.getPosition());
        }
        return polygonPoints;
    }
}
