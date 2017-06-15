package com.krystianwsul.yelpdemo;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.Category;

import org.parceler.Parcel;

import java.util.Arrays;
import java.util.List;

@Parcel
class RestaurantData {
    String mName;
    String mImageUrl;
    String mPhone;
    float mRating;
    String mAddress;
    String mCategories;
    int mReviewCount;

    RestaurantData() {

    }

    RestaurantData(@NonNull Business business) {
        mName = business.getName();
        mImageUrl = business.getImageUrl();
        mPhone = business.getDisplayPhone();
        mRating = (float) business.getRating();
        mAddress = getAddress(business);
        mCategories = Stream.of(business.getCategories())
                .map(Category::getTitle)
                .collect(Collectors.joining("\n"));
        mReviewCount = business.getReviewCount();
    }

    static String getAddress(@NonNull Business business) {
        List<String> addressList = Arrays.asList(business.getLocation().getAddress1(),
                business.getLocation().getAddress2(),
                business.getLocation().getAddress3());

        return Stream.of(addressList)
                .filter(line -> !TextUtils.isEmpty(line))
                .collect(Collectors.joining("\n"));
    }
}
