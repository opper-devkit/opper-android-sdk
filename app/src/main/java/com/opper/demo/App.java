package com.opper.demo;

import android.app.Application;

import com.omofresh.oppersdk.helper.OpperHelper;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OpperHelper.init(this, new OpperHelper.Weigh() {
            @Override
            public int unit() {
                return Settings.unit;
            }

            @Override
            public int decimalPlaces() {
                return Settings.decimals;
            }

            @Override
            public double offset() {
                return Settings.offset;
            }

            @Override
            public double tare() {
                return Settings.tare;
            }

            @Override
            public void onClearWeight(double v) {
                Settings.offset = v;
            }

            @Override
            public void onTareChange(double v) {
                Settings.tare = v;
            }

            @Override
            public int vibrateGrams() {
                return Settings.vibrateGrams;
            }

            @Override
            public double e() {
                return Settings.e;
            }
        });
    }

}
