package com.krystianwsul.yelpdemo;

import android.support.annotation.NonNull;

import com.yelp.fusion.client.models.Review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;

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
