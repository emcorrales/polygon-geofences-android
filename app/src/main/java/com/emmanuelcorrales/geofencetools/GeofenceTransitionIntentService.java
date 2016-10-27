package com.emmanuelcorrales.geofencetools;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionIntentService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 4858;
    private static final String KEY_POLYGON_POINTS_COUNT = "key_polygon_points_count";
    private static final String KEY_LATITUDE = "key_lat";
    private static final String KEY_LONGITUDE = "key_long";

    public GeofenceTransitionIntentService() {
        super(TAG);
    }

    public static PendingIntent newPendingIntent(Context context, List<LatLng> points) {
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
        flattenArrayExtra(intent, points);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.d(TAG, "intent is null.");
            return;
        }

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e(TAG, "Error code: " + event.getErrorCode());
            return;
        }

        int transition = event.getGeofenceTransition();
        switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "Entered the geofence.");
                showNotification("Entered the circular geofence.");
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "Exited the geofence.");
                showNotification("Exited the circular geofence.");
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "Dwelling on the geofence.");
                showNotification("Dwells inside the circular geofence.");
                break;

            default:
                Log.d(TAG, "Invalid transition: " + transition);
                break;
        }

        List<LatLng> points = readFlattenedArrayExtras(intent);
        Location location = event.getTriggeringLocation();
        if (location != null) {
            LatLng triggeredPoint = new LatLng(location.getLatitude(), location.getLongitude());
            if (PolyUtil.containsLocation(triggeredPoint, points, true)) {
                Log.d(TAG, "Inside the polygon geofence.");
                showNotification("Inside the polygon geofence.");
            } else {
                Log.d(TAG, "Not inside the polygon geofence.");
            }
        }
    }

    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Geofence pending intent doesn't go well with with storing arrays or list extras.
     *
     * @param intent Intent where values will be stored.
     * @param points List of LatLng that will be flattened.
     */
    private static void flattenArrayExtra(Intent intent, List<LatLng> points) {
        intent.putExtra(KEY_POLYGON_POINTS_COUNT, points.size());
        for (int i = 0; i < points.size(); i++) {
            intent.putExtra(KEY_LATITUDE + i, points.get(i).latitude);
            intent.putExtra(KEY_LONGITUDE + i, points.get(i).longitude);
        }
    }

    private static List<LatLng> readFlattenedArrayExtras(Intent intent) {
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



