/*
 * Copyright 2014 ParanoidAndroid Project
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.URLStringReader;
import com.paranoid.paranoidota.URLStringReader.URLStringReaderListener;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.widget.Preference;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public static final String LOGIN_URL = "http://goo-inside.me/salt";

    private SettingsHelper mSettingsHelper;
    private ListPreference mCheckTime;
    private CheckBoxPreference mCheckGapps;
    private ListPreference mGappsType;
    private Preference mGoo;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        mSettingsHelper = new SettingsHelper(this);

        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.layout.activity_settings);

        mCheckTime = (ListPreference) findPreference(SettingsHelper.PROPERTY_CHECK_TIME);
        mCheckGapps = (CheckBoxPreference) findPreference(SettingsHelper.PROPERTY_CHECK_GAPPS);
        mGappsType = (ListPreference) findPreference(SettingsHelper.PROPERTY_GAPPS_TYPE);
        mGoo = (Preference) findPreference("goo");

        mCheckTime.setValue(String.valueOf(mSettingsHelper.getCheckTime()));
        mCheckGapps.setChecked(mSettingsHelper.getCheckGapps());
        mGappsType.setValue(String.valueOf(mSettingsHelper.getGappsType()));

        updateSummaries();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    @Deprecated
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            android.preference.Preference preference) {
        if (preference.getKey().equals("goo")) {
            if (mSettingsHelper.isLogged()) {
                mSettingsHelper.logout();
                updateSummaries();
                Utils.showToastOnUiThread(this, R.string.logged_out);
            } else {
                showLoginDialog();
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsHelper.PROPERTY_CHECK_TIME.equals(key)) {
            Utils.setAlarm(this, mSettingsHelper.getCheckTime(), false, true);
        }
    }

    private void updateSummaries() {
        if (mSettingsHelper.isLogged()) {
            mGoo.setSummary(getResources().getString(R.string.logged_in, mSettingsHelper.getLoginUserName()));
        } else {
            mGoo.setSummary(R.string.settings_login_goo);
        }
    }

    public void showLoginDialog() {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);
        final EditText username = (EditText) view.findViewById(R.id.username);
        final EditText password = (EditText) view.findViewById(R.id.password);

        username.setText(mSettingsHelper.getLoginUserName());

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.settings_login_goo)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();

                        final ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage(getResources().getString(R.string.logging_in));
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        String user = username.getText() == null ? "" : username.getText()
                                .toString();
                        String pass = password.getText() == null ? "" : password.getText()
                                .toString();

                        mSettingsHelper.setLoginUserName(user);

                        try {
                            String url = LOGIN_URL + "&username="
                                    + URLEncoder.encode(user, "UTF-8") + "&password="
                                    + URLEncoder.encode(pass, "UTF-8");
                            new URLStringReader(new URLStringReaderListener() {

                                @Override
                                public void onReadEnd(String buffer) {
                                    progressDialog.dismiss();
                                    if (buffer != null && buffer.length() == 32) {
                                        mSettingsHelper.login(buffer);
                                        Utils.showToastOnUiThread(
                                                SettingsActivity.this,
                                                getResources().getString(R.string.logged_in,
                                                        mSettingsHelper.getLoginUserName()));
                                        updateSummaries();
                                    } else if (buffer != null) {
                                        Utils.showToastOnUiThread(SettingsActivity.this,
                                                R.string.error_logging_invalid);
                                    } else {
                                        Utils.showToastOnUiThread(SettingsActivity.this, R.string.error_logging_down);
                                    }
                                }

                                @Override
                                public void onReadError(Exception ex) {
                                    progressDialog.dismiss();
                                    ex.printStackTrace();
                                    Utils.showToastOnUiThread(SettingsActivity.this, R.string.error_logging_in);
                                }

                            }).execute(url);
                        } catch (UnsupportedEncodingException ex) {
                            // should never get here
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        builder.setView(view);
        builder.create().show();
    }
}