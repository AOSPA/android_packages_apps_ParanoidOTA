package com.paranoid.paranoidota.activities;

import com.paranoid.paranoidota.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class IntroductionActivity extends Activity {

    public static final String KEY_PREFERENCES = "Introduction";
    public static final String KEY_FIRST_RUN = "first_run";

    Activity mActivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_introduction);
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences mPreferences = mActivity.getSharedPreferences(KEY_PREFERENCES, 0);
                mPreferences.edit().putBoolean(KEY_FIRST_RUN, false).apply();
                mActivity.finish();
            }
        });
    }
}