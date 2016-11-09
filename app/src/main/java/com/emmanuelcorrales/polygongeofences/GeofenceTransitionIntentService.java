package com.emmanuelcorrales.polygongeofences;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import static com.emmanuelcorrales.polygongeofences.LocationUpdateService.KEY_LATITUDE;
import static com.emmanuelcorrales.polygongeofences.LocationUpdateService.KEY_LONGITUDE;
import static com.emmanuelcorrales.polygongeofences.LocationUpdateService.KEY_POLYGON_POINTS_COUNT;


public class GeofenceTransitionIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionIntentService.class.getSimpleName();
    private static final int NOTIFICATION_ENTER = 2222;
    private static final int NOTIFICATION_EXIT = 1111;
    private static final int NOTIFICATION_DWELL = 555;

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

        intent.setClass(this, LocationUpdateService.class);
        int transition = event.getGeofenceTransition();
        switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "Entered the geofence.");
                showNotification("Entered the circular geofence.", NOTIFICATION_ENTER);
                startService(intent);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "Dwelling on the geofence.");
                startService(intent);
                showNotification("Dwells inside the circular geofence.", NOTIFICATION_DWELL);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "Exited the geofence.");
                showNotification("Exited the circular geofence.", NOTIFICATION_EXIT);
                stopService(intent);
                break;

            default:
                Log.d(TAG, "Invalid transition: " + transition);
                break;
        }
    }

    private void showNotification(String message, int notifId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(notifId, builder.build());
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
}



