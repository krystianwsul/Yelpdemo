package com.krystianwsul.yelpdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    private YelpFusionApi mYelpFusionApi;

    @Nullable
    private AsyncTask<Void, Void, Void> mAsyncTask;

    private Handler mHandler;

    @NonNull
    static Map<String, Business> sShownBusinesses = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mHandler = new Handler();

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

        mHandler.removeCallbacksAndMessages(null);
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
        LatLng sydney = new LatLng(-33.93544992896953, 151.0491035574696);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                @SuppressLint("InflateParams")
                View view = getLayoutInflater().inflate(R.layout.info_view, null);

                Business business = sShownBusinesses.get(marker.getTitle());

                TextView infoViewTitle = (TextView) view.findViewById(R.id.info_view_title);
                infoViewTitle.setText(business.getName());

                RatingBar infoViewRating = (RatingBar) view.findViewById(R.id.info_view_rating);
                infoViewRating.setRating((float) business.getRating());

                List<String> address = new ArrayList<>();
                if (!TextUtils.isEmpty(business.getLocation().getAddress1()))
                    address.add(business.getLocation().getAddress1());
                if (!TextUtils.isEmpty(business.getLocation().getAddress2()))
                    address.add(business.getLocation().getAddress2());
                if (!TextUtils.isEmpty(business.getLocation().getAddress3()))
                    address.add(business.getLocation().getAddress3());

                TextView infoViewAddress = (TextView) view.findViewById(R.id.info_view_address);
                infoViewAddress.setText(TextUtils.join("\n", address));

                return view;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                startActivity(RestaurantActivity.newIntent(MapsActivity.this, marker.getTitle()));
            }
        });

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

        mHandler.removeCallbacksAndMessages(null);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LatLngBounds latLngBounds = mMap.getProjection()
                        .getVisibleRegion()
                        .latLngBounds;

                Pair<Double, Double> center = midPoint(latLngBounds.southwest.latitude,
                        latLngBounds.southwest.longitude,
                        latLngBounds.northeast.latitude,
                        latLngBounds.northeast.longitude);

                Double longitude = center.second;
                if (longitude > 180)
                    longitude = 360 - longitude;

                Log.e("asdf", "center: " + center.first + ", " + longitude);

                double radius = distance(latLngBounds.southwest.latitude, center.first,
                        latLngBounds.southwest.longitude, center.second);

                radius = Math.min(radius, 40000);

                Map<String, String> params = new HashMap<>();
                params.put("categories", "restaurants");
                params.put("latitude", center.first.toString());
                params.put("longitude", longitude.toString());
                params.put("radius", Long.valueOf(Math.round(radius)).toString());

                mYelpFusionApi.getBusinessSearch(params).enqueue(new Callback<SearchResponse>() {
                    @Override
                    public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                        for (Business business : response.body().getBusinesses()) {
                            if (sShownBusinesses.containsKey(business.getId()))
                                return;

                            sShownBusinesses.put(business.getId(), business);

                            LatLng latLng = new LatLng(business.getCoordinates().getLatitude(), business.getCoordinates().getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(business.getId()));
                        }

                        Log.e("asdf", "response: " + response.body());
                    }

                    @Override
                    public void onFailure(Call<SearchResponse> call, Throwable t) {
                        Log.e("asdf", "onFailure", t);
                    }
                });
            }
        }, 100);
    }
}
