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

public class RestaurantActivity extends AppCompatActivity {
    private static final String ID_KEY = "id";

    @NonNull
    static Intent newIntent(@NonNull Context context, @NonNull String id) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(ID_KEY, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        String id = getIntent().getStringExtra(ID_KEY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.restaurant_toolbar);
        setSupportActionBar(toolbar);

        //Business business = MapsActivity.sShownBusinesses.get(id);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle(business.getName());

        ImageView restaurantImage = (ImageView) findViewById(R.id.restaurant_image);

        //Log.e("asdf", "url: " + business.getImageUrl());

        /*
        Glide.with(this)
                .load(business.getImageUrl())
                .into(restaurantImage);
                */
    }
}
