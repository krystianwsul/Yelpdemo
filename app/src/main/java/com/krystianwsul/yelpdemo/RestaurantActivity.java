package com.krystianwsul.yelpdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.Review;

import junit.framework.Assert;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.parceler.Parcels;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class RestaurantActivity extends AppCompatActivity {
    private static final String RESTAURANT_DATA_KEY = "restaurantData";

    private static final DateTimeFormatter sDateTimeFormatter
            = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

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
                    restaurantReviews.removeAllViews();

                    for (Review review : reviews) {
                        View view = View.inflate(RestaurantActivity.this,
                                R.layout.row_review, null);

                        ImageView reviewImage = (ImageView) view.findViewById(R.id.review_image);
                        if (!TextUtils.isEmpty(review.getUser().getImageUrl()))
                            Glide.with(RestaurantActivity.this)
                                    .load(review.getUser().getImageUrl())
                                    .into(reviewImage);

                        TextView reviewName = (TextView) view.findViewById(R.id.review_name);
                        reviewName.setText(review.getUser().getName());

                        RatingBar reviewStars = (RatingBar) view.findViewById(R.id.review_stars);
                        reviewStars.setRating(review.getRating());

                        TextView reviewDate = (TextView) view.findViewById(R.id.review_date);
                        reviewDate.setText(formatDate(review.getTimeCreated()));

                        TextView reviewBody = (TextView) view.findViewById(R.id.review_body);
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

    @NonNull
    private String formatDate(@NonNull String date) {
        LocalDateTime then = sDateTimeFormatter.parseLocalDateTime(date);
        LocalDateTime now = LocalDateTime.now();

        Period period = new Period(then, now);

        if (period.getYears() > 0)
            return period.getYears() + " years ago";

        if (period.getMonths() > 0)
            return period.getMonths() + " months ago";

        if (period.getDays() > 0)
            return period.getDays() + " days ago";

        if (period.getHours() > 0)
            return period.getHours() + " hours ago";

        if (period.getMinutes() > 0)
            return period.getMinutes() + " minutes ago";

        return "now";
    }
}
