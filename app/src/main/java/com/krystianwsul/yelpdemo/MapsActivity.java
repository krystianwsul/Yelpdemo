package com.krystianwsul.yelpdemo;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    private YelpFusionApi mYelpFusionApi;

    @Nullable
    private AsyncTask<Void, Void, Void> mAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                YelpFusionApiFactory yelpFusionApiFactory = new YelpFusionApiFactory();

                try {
                    mYelpFusionApi = yelpFusionApiFactory.createAPI("Smym4waYVw0m-nFnGJpQ3g", "5KfDI2E0e5o0fvpF2NjsaypsS1cyaSMrJPQxArkYtMyQdOkqH5KExBxLshPuTVvm");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateMap();
            }
        };

        mAsyncTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAsyncTask != null)
            mAsyncTask.cancel(true);
    }

    private static Pair<Double, Double> midPoint(double lat1, double lng1, double lat2, double lng2) {
        double dLon = Math.toRadians(lng2 - lng1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lng1 = Math.toRadians(lng1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx)
                * (Math.cos(lat1) + Bx) + By * By));
        double lng3 = lng1 + Math.atan2(By, Math.cos(lat1) + Bx);

        return Pair.create(Math.toDegrees(lat3), Math.toDegrees(lng3));
    }

    public static double distance(double lat1, double lat2, double lng1, double lng2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                updateMap();
            }
        });

        updateMap();
    }

    private void updateMap() {
        if (mYelpFusionApi == null)
            return;

        if (mMap == null)
            return;

        LatLngBounds latLngBounds = mMap.getProjection()
                .getVisibleRegion()
                .latLngBounds;

        Pair<Double, Double> center = midPoint(latLngBounds.southwest.latitude,
                latLngBounds.southwest.longitude,
                latLngBounds.northeast.latitude,
                latLngBounds.northeast.longitude);

        Log.e("asdf", center.first + ", " + center.second);

        double radius = distance(latLngBounds.southwest.latitude, center.first,
                latLngBounds.southwest.longitude, center.second);

        Log.e("asdf", "radius: " + radius);

        Map<String, String> params = new HashMap<>();
        params.put("term", "restaurants");
        params.put("latitude", center.first.toString());
        params.put("longitude", center.second.toString());
        params.put("radius", center.second.toString());

        //mYelpFusionApi.getBusinessSearch(params);
    }
}
