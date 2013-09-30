/*
 * Copyright 2013 ParanoidAndroid Project
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.paranoid.paranoidota.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;

public class IntroductionActivity extends Activity {

    public static final String KEY_PREFERENCES = "Introduction";
    public static final String KEY_FIRST_RUN = "first_run";

    Activity mActivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        setContentView(R.layout.activity_introduction);

        TextView introduction = (TextView) findViewById(R.id.introduction);
        if (!Utils.weAreInAospa()) {
            introduction.setText(R.string.introduction_installer);
        }

        Button button = (Button) findViewById(R.id.button);
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