package com.krystianwsul.yelpdemo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class YelpViewModel {
    static final YelpViewModel sInstance = new YelpViewModel();

    @Nullable
    private Listener mListener;

    @Nullable
    private Map<String, Business> mBusinesses;

    @Nullable
    private LatLngBounds mLocation;

    private boolean mExecutingRequest = false;

    private YelpViewModel() {

    }

    void setListener(@NonNull Listener listener) {
        Assert.assertTrue(mListener == null); // todo remove assertions

        mListener = listener;

        if (mBusinesses != null) {
            Log.e("asdf", "onPostExecute setListener returning cached");

            listener.onResponse(mBusinesses);
        }
    }

    void clearListener() {
        Log.e("asdf", "clearListener");

        mListener = null;
    }

    void enqueueRequest(@NonNull LatLngBounds latLngBounds) {
        Assert.assertTrue(mListener != null);

        mLocation = latLngBounds;

        if (!mExecutingRequest) {
            Log.e("asdf", "enqueueRequest getting request");

            getRequest();
        } else {
            Log.e("asdf", "enqueueRequest already executing, setting pending");
        }
    }

    private void getRequest() {
        Log.e("asdf", "enqueueRequest getRequest");

        Assert.assertTrue(!mExecutingRequest);
        Assert.assertTrue(mLocation != null);

        mExecutingRequest = true;

        YelpApiSingleton.sInstance.mSingle.subscribe(yelpFusionApi -> {
            Assert.assertTrue(mExecutingRequest);
            Assert.assertTrue(mLocation != null);

            LatLngBounds latLngBounds = mLocation;

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

            yelpFusionApi.getBusinessSearch(params).enqueue(new Callback<SearchResponse>() {
                @Override
                public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                    if (mBusinesses == null)
                        mBusinesses = new HashMap<>();

                    for (Business business : response.body().getBusinesses())
                        mBusinesses.put(business.getId(), business);

                    if (mListener != null)
                        mListener.onResponse(mBusinesses);

                    Log.e("asdf", "response: " + response.body());

                    onFinished(latLngBounds);
                }

                @Override
                public void onFailure(Call<SearchResponse> call, Throwable t) {
                    Log.e("asdf", "onFailure", t);

                    onFinished(latLngBounds);
                }
            });
        });
    }

    private void onFinished(@NonNull LatLngBounds latLngBounds) {
        mExecutingRequest = false;

        if (!latLngBounds.equals(mLocation)) {
            Log.e("asdf", "onFinished mLocation not null, restarting");

            getRequest();
        } else {
            Log.e("asdf", "onFinished mLocation null, finished");
        }
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

    private static double distance(double lat1, double lat2, double lng1, double lng2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }

    interface Listener {
        void onResponse(@NonNull Map<String, Business> businesses);
    }
}
