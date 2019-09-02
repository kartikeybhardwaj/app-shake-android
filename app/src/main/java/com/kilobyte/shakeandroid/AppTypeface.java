package com.kilobyte.shakeandroid;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class AppTypeface extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/google_sans_regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

    }
}
