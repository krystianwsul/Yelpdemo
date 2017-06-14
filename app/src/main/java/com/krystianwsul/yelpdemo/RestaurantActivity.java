package com.krystianwsul.yelpdemo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yelp.fusion.client.models.Business;

import junit.framework.Assert;

import org.parceler.Parcels;

public class RestaurantActivity extends AppCompatActivity {
    private static final String RESTAURANT_DATA_KEY = "restaurantData";

    @NonNull
    static Intent newIntent(@NonNull Context context, @NonNull Business business) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(RESTAURANT_DATA_KEY, Parcels.wrap(new RestaurantData(business)));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        RestaurantData restaurantData = Parcels.unwrap(getIntent().getParcelableExtra(RESTAURANT_DATA_KEY));
        Assert.assertTrue(restaurantData != null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.restaurant_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        Assert.assertTrue(actionBar != null);

        actionBar.setTitle(restaurantData.mName);

        ImageView restaurantImage = (ImageView) findViewById(R.id.restaurant_image);

        Log.e("asdf", "url: " + restaurantData.mImageUrl);

        Glide.with(this)
                .load(restaurantData.mImageUrl)
                .into(restaurantImage);
    }
}
