package com.krystianwsul.yelpdemo;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.models.Review;
import com.yelp.fusion.client.models.Reviews;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class ReviewSource {
    static final ReviewSource sInstance = new ReviewSource();

    private final Map<String, Single<List<Review>>> mReviews = new HashMap<>();

    private ReviewSource() {

    }

    @NonNull
    Single<List<Review>> getReview(@NonNull String businessId) {
        if (!mReviews.containsKey(businessId))
            mReviews.put(businessId, YelpApiSingleton.sInstance.mSingle
                    .map(yelpFusionApi -> (List<Review>) yelpFusionApi
                            .getBusinessReviews(businessId, "en_US")
                            .execute()
                            .body()
                            .getReviews())
                    .cache());

        return mReviews.get(businessId);
    }
}
