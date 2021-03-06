package com.krystianwsul.yelpdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
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
import com.yelp.fusion.client.models.Business;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        YelpViewModel.Listener {

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

                TextView infoViewAddress = (TextView) view.findViewById(R.id.info_view_address);
                infoViewAddress.setText(RestaurantData.getAddress(business));

                return view;
            }
        });

        mMap.setOnInfoWindowClickListener(marker -> startActivity(RestaurantActivity
                .newIntent(MapsActivity.this, mBusinesses.get(marker.getTitle()))));

        mMap.setOnCameraMoveListener(this::updateMap);
        mMap.setOnCameraIdleListener(this::updateMap);

        if (mFirst) {
            LatLng empireStateBuilding = new LatLng(40.748817, -73.985428);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(empireStateBuilding));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }

        YelpViewModel.sInstance.setListener(this);
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

        mBusinesses.putAll(businesses);

        for (Business business : newBusinesses) {
            LatLng latLng = new LatLng(business.getCoordinates().getLatitude(),
                    business.getCoordinates().getLongitude());
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
