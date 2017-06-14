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

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
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

import junit.framework.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, YelpViewModel.Listener {

    private GoogleMap mMap;

    @NonNull
    private final Map<String, Business> mBusinesses = new HashMap<>();

    private boolean mFirst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFirst = (savedInstanceState == null);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Assert.assertTrue(googleMap != null);

        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                @SuppressLint("InflateParams")
                View view = getLayoutInflater().inflate(R.layout.info_view, null);
                Assert.assertTrue(view != null);

                Business business = mBusinesses.get(marker.getTitle());

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

        mMap.setOnInfoWindowClickListener(marker -> startActivity(RestaurantActivity.newIntent(MapsActivity.this, marker.getTitle())));

        mMap.setOnCameraMoveListener(this::updateMap);
        mMap.setOnCameraIdleListener(this::updateMap);

        if (mFirst) {
            LatLng empireStateBuilding = new LatLng(40.748817, -73.985428);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(empireStateBuilding));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

            Log.e("asdf", "initial bounds " + mMap.getProjection()
                    .getVisibleRegion()
                    .latLngBounds);

            YelpViewModel.sInstance.setListener(this, null);
        } else {
            LatLngBounds latLngBounds = mMap.getProjection()
                    .getVisibleRegion()
                    .latLngBounds;

            YelpViewModel.sInstance.setListener(this, latLngBounds);
        }
    }

    private void updateMap() {
        LatLngBounds latLngBounds = mMap.getProjection()
                .getVisibleRegion()
                .latLngBounds;

        YelpViewModel.sInstance.enqueueRequest(latLngBounds);
    }

    @Override
    public void onResponse(@NonNull Map<String, Business> businesses) {
        List<Business> newBusinesses = Stream.of(businesses.entrySet())
                .filter(entry -> !mBusinesses.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        Log.e("asdf", newBusinesses.size() + " new results");

        mBusinesses.putAll(businesses);

        for (Business business : newBusinesses) {
            LatLng latLng = new LatLng(business.getCoordinates().getLatitude(), business.getCoordinates().getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(business.getId()));
        }
    }

    @Override
    protected void onDestroy() {
        YelpViewModel.sInstance.clearListener();

        super.onDestroy();
    }
}
