package com.krystianwsul.yelpdemo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.Review;
import com.yelp.fusion.client.models.Reviews;

import junit.framework.Assert;

import org.parceler.Parcels;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantActivity extends AppCompatActivity {
    private static final String RESTAURANT_DATA_KEY = "restaurantData";

    @NonNull
    static Intent newIntent(@NonNull Context context, @NonNull Business business) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(RESTAURANT_DATA_KEY, Parcels.wrap(new RestaurantData(business)));
        return intent;
    }

    private Disposable mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        RestaurantData restaurantData = Parcels.unwrap(getIntent()
                .getParcelableExtra(RESTAURANT_DATA_KEY));
        Assert.assertTrue(restaurantData != null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.restaurant_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        Assert.assertTrue(actionBar != null);

        actionBar.setTitle(restaurantData.mName);

        ImageView restaurantImage = (ImageView) findViewById(R.id.restaurant_image);
        Glide.with(this)
                .load(restaurantData.mImageUrl)
                .into(restaurantImage);

        TextView restaurantPhone = (TextView) findViewById(R.id.restaurant_phone);
        restaurantPhone.setText(restaurantData.mPhone);

        TextView restaurantRatingNumber = (TextView) findViewById(R.id.restaurant_rating_number);
        restaurantRatingNumber.setText(String.valueOf(restaurantData.mRating));

        RatingBar restaurantRatingStars = (RatingBar) findViewById(R.id.restaurant_rating_stars);
        restaurantRatingStars.setRating(restaurantData.mRating);

        TextView restaurantReviewCount = (TextView) findViewById(R.id.restaurant_review_count);
        restaurantReviewCount.setText(String.valueOf(restaurantData.mReviewCount));

        TextView restaurantAddress = (TextView) findViewById(R.id.restaurant_address);
        restaurantAddress.setText(restaurantData.mAddress);

        TextView restaurantCategories = (TextView) findViewById(R.id.restaurant_categories);
        restaurantCategories.setText(restaurantData.mCategories);

        LinearLayout restaurantReviews = (LinearLayout) findViewById(R.id.restaurant_reviews);

        mListener = ReviewSource.sInstance.getReview(restaurantData.mId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reviews -> {
                    for (Review review : reviews) {
                        View view = View.inflate(RestaurantActivity.this,
                                R.layout.row_review, null);
                        Assert.assertTrue(view != null);

                        TextView reviewBody = (TextView) view.findViewById(R.id.review_body);
                        Assert.assertTrue(reviewBody != null);

                        reviewBody.setText(review.getText());

                        restaurantReviews.addView(view);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        mListener.dispose();

        super.onDestroy();
    }
}
