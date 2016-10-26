package com.emmanuelcorrales.geofencetools;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


public class GeofenceTransitionIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionIntentService.class.getSimpleName();

    public GeofenceTransitionIntentService() {
        super(TAG);
    }

    public static PendingIntent newPendingIntent(Context context) {
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
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
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "Exited the geofence.");
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "Dwelling on the geofence.");
                break;

            default:
                break;
        }
    }
}



