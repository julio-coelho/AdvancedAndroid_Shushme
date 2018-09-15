package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback {

    private static final String TAG = Geofencing.class.getSimpleName();

    private static final long GEOFENCE_TIMEOUT = 24*60*60*1000;
    private static final float GEOFENCE_RADIUS = 50;

    private GoogleApiClient mClient;
    private Context mContext;
    private List<Geofence> mGeofences;
    private PendingIntent mPendingIntent;

    public Geofencing(Context mContext, GoogleApiClient mClient) {
        this.mClient = mClient;
        this.mContext = mContext;
        this.mPendingIntent = null;
        this.mGeofences = new ArrayList<>();
    }

    public void registerAllGeofences() {
        if (mClient == null || !mClient.isConnected() || mGeofences == null || mGeofences.size() == 0) {
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void unRegisterAllGeofences() {
        if (mClient == null || !mClient.isConnected()) {
            return;
        }

        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void updateGeofences(PlaceBuffer places) {
        mGeofences = new ArrayList<>();
        for (Place place : places) {
            mGeofences.add(
                    new Geofence.Builder()
                            .setRequestId(place.getId())
                            .setExpirationDuration(GEOFENCE_TIMEOUT)
                            .setCircularRegion(place.getLatLng().latitude, place.getLatLng().longitude, GEOFENCE_RADIUS)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build());
        }
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.i(TAG, String.format("Error adding/removing geofence : %s", result.getStatus().toString()));
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mPendingIntent != null) {
            return mPendingIntent;
        }

        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mPendingIntent;
    }

}
