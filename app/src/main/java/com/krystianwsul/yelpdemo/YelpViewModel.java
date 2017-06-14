package com.krystianwsul.yelpdemo;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import junit.framework.Assert;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class YelpViewModel {
    @Nullable
    private static YelpViewModel sInstance;

    @NonNull
    static YelpViewModel getInstance() {
        if (sInstance == null)
            sInstance = new YelpViewModel();
        return sInstance;
    }

    @Nullable
    private YelpFusionApi mYelpFusionApi;

    @Nullable
    private Listener mListener;

    @Nullable
    private Map<String, Business> mBusinesses;

    @Nullable
    private LatLngBounds mPendingRequest;

    private boolean mExecutingRequest = false;

    private YelpViewModel() {
        AsyncTask<Void, Void, YelpFusionApi> asyncTask = new AsyncTask<Void, Void, YelpFusionApi>() {
            @Override
            protected YelpFusionApi doInBackground(Void... params) {
                YelpFusionApiFactory yelpFusionApiFactory = new YelpFusionApiFactory();

                try {
                    return yelpFusionApiFactory.createAPI("Smym4waYVw0m-nFnGJpQ3g", "5KfDI2E0e5o0fvpF2NjsaypsS1cyaSMrJPQxArkYtMyQdOkqH5KExBxLshPuTVvm");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void onPostExecute(@SuppressWarnings("NullableProblems") YelpFusionApi yelpFusionApi) {
                Assert.assertTrue(yelpFusionApi != null);
                Assert.assertTrue(!mExecutingRequest);
                Assert.assertTrue(mBusinesses == null);
                Assert.assertTrue(mYelpFusionApi == null);

                mYelpFusionApi = yelpFusionApi;

                Log.e("asdf", "onPostExecute mPendingRequest " + mPendingRequest);
                if (mPendingRequest != null) {
                    LatLngBounds latLngBounds = mPendingRequest;
                    mPendingRequest = null;

                    getRequest(latLngBounds);
                }
            }
        };

        asyncTask.execute();
    }

    void setListener(@NonNull Listener listener, @Nullable LatLngBounds latLngBounds) {
        Assert.assertTrue(mListener == null); // todo remove assertions

        mListener = listener;

        if (mBusinesses != null) {
            Log.e("asdf", "onPostExecute setListener returning cached");

            Assert.assertTrue(mYelpFusionApi != null);

            listener.onResponse(mBusinesses);
        } else {
            if (latLngBounds != null) {
                Log.e("asdf", "onPostExecute enqueueing");

                enqueueRequest(latLngBounds);
            }
        }
    }

    void clearListener() {
        Log.e("asdf", "clearListener");

        mListener = null;
    }

    void enqueueRequest(@NonNull LatLngBounds latLngBounds) {
        Assert.assertTrue(mListener != null);

        if (mYelpFusionApi == null) {
            Log.e("asdf", "enqueueRequest setting pending");

            Assert.assertTrue(mBusinesses == null);
            Assert.assertTrue(!mExecutingRequest);

            mPendingRequest = latLngBounds; // possibly overwrite
        } else {
            if (!mExecutingRequest) {
                Log.e("asdf", "enqueueRequest getting request");

                Assert.assertTrue(mPendingRequest == null);

                getRequest(latLngBounds);
            } else {
                Log.e("asdf", "enqueueRequest already executing, setting pending");

                mPendingRequest = latLngBounds; // possibly overwrite
            }
        }
    }

    private void getRequest(@NonNull LatLngBounds latLngBounds) {
        Log.e("asdf", "enqueueRequest getRequest");

        Assert.assertTrue(mYelpFusionApi != null);
        Assert.assertTrue(mPendingRequest == null);
        Assert.assertTrue(!mExecutingRequest);

        mExecutingRequest = true;

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
                if (mBusinesses == null)
                    mBusinesses = new HashMap<>();

                for (Business business : response.body().getBusinesses()) {
                    mBusinesses.put(business.getId(), business);
                }

                if (mListener != null)
                    mListener.onResponse(mBusinesses);

                Log.e("asdf", "response: " + response.body());

                onFinished();
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e("asdf", "onFailure", t);

                onFinished();
            }
        });
    }

    private void onFinished() {
        mExecutingRequest = false;

        if (mPendingRequest != null) {
            Log.e("asdf", "onFinished mPendingRequest not null, restarting");

            LatLngBounds latLngBounds = mPendingRequest;
            mPendingRequest = null;

            getRequest(latLngBounds);
        } else {
            Log.e("asdf", "onFinished mPendingRequest null, finished");
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
