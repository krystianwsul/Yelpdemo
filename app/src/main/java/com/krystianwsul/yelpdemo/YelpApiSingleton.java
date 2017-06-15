package com.krystianwsul.yelpdemo;

import android.util.Log;

import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class YelpApiSingleton {
    static final YelpApiSingleton sInstance = new YelpApiSingleton();

    final Single<YelpFusionApi> mSingle =
            Observable.fromCallable(() -> {
                YelpFusionApiFactory yelpFusionApiFactory = new YelpFusionApiFactory();

                try {
                    return yelpFusionApiFactory.createAPI("Smym4waYVw0m-nFnGJpQ3g",
                            "5KfDI2E0e5o0fvpF2NjsaypsS1cyaSMrJPQxArkYtMyQdOkqH5KExBxLshPuTVvm");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
                    .cache()
                    .singleOrError()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

    private YelpApiSingleton() {
        mSingle.subscribe(yelpFusionApi -> {});
    }
}
