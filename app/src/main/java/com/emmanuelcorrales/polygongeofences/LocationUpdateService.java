package com.emmanuelcorrales.polygongeofences;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;


public class LocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String KEY_POLYGON_POINTS_COUNT = "key_polygon_points_count";
    public static final String KEY_LATITUDE = "key_lat";
    public static final String KEY_LONGITUDE = "key_long";

    private static final String TAG = LocationUpdateService.class.getSimpleName();
    private static final int NOTIFICATION_INSIDE_POLYGON = 777;

    private GoogleApiClient mGoogleApiClient;
    private List<LatLng> mPolygonPoints;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null) {
            mPolygonPoints = readFlattenedArrayExtras(intent);
        }
        if (mGoogleApiClient == null) {
            mGoogleApiClient = createGoogleApiClient();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mGoogleApiClient == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null || mPolygonPoints == null || mPolygonPoints.size() < 3) {
            return;
        }
        String msg = "";
        LatLng triggeredPoint = new LatLng(location.getLatitude(), location.getLongitude());
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        if (PolyUtil.containsLocation(triggeredPoint, mPolygonPoints, true)) {
            msg += "Inside the polygon geofence.";
            Log.d(TAG, msg);
        } else {
            msg += "Outside of polygon geofence.";
            Log.d(TAG, "Outside of polygon geofence.");
        }
        inboxStyle.setBigContentTitle(msg);
        inboxStyle.addLine("Accuracy: " + location.getAccuracy());
        inboxStyle.addLine("Latitude: " + location.getLatitude());
        inboxStyle.addLine("Longitude: " + location.getLongitude());
        showNotification(msg, NOTIFICATION_INSIDE_POLYGON, inboxStyle);
    }

    private GoogleApiClient createGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return request;
    }

    private void showNotification(String message, int notifId, NotificationCompat.InboxStyle style) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setStyle(style)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(notifId, builder.build());
    }

    private static List<LatLng> readFlattenedArrayExtras(Intent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("Argument 'intent' cannot be null.");
        }
        List<LatLng> points = new ArrayList<>();
        int count = intent.getIntExtra(KEY_POLYGON_POINTS_COUNT, 0);
        for (int i = 0; i < count; i++) {
            double latitude = intent.getDoubleExtra(KEY_LATITUDE + i, 0);
            double longitude = intent.getDoubleExtra(KEY_LONGITUDE + i, 0);
            points.add(new LatLng(latitude, longitude));
        }
        return points;
    }
}
