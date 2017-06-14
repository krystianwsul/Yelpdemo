package com.krystianwsul.yelpdemo;

import android.support.annotation.NonNull;

import com.yelp.fusion.client.models.Business;

import org.parceler.Parcel;

@Parcel
class RestaurantData {
    String mName;
    String mImageUrl;

    RestaurantData() {

    }

    RestaurantData(@NonNull Business business) {
        mName = business.getName();
        mImageUrl = business.getImageUrl();
    }
}
