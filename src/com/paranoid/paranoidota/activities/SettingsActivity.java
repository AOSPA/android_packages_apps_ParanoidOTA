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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.paranoid.paranoidota.DirectoryChooserDialog;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.SettingsHelper;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private SettingsHelper mSettingsHelper;
    private ListPreference mCheckTime;
    private CheckBoxPreference mCheckGapps;
    private ListPreference mGappsType;
    private Preference mDownloadPath;
    private CheckBoxPreference mDownloadFinished;
    private MultiSelectListPreference mOptions;

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
        mDownloadPath = findPreference(SettingsHelper.PROPERTY_DOWNLOAD_PATH);
        mDownloadFinished = (CheckBoxPreference) findPreference(SettingsHelper.PROPERTY_DOWNLOAD_FINISHED);
        mOptions = (MultiSelectListPreference) findPreference(SettingsHelper.PROPERTY_SHOW_OPTIONS);

        mCheckTime.setValue(String.valueOf(mSettingsHelper.getCheckTime()));
        mCheckGapps.setChecked(mSettingsHelper.getCheckGapps());
        mGappsType.setValue(String.valueOf(mSettingsHelper.getGappsType()));
        mDownloadFinished.setChecked(mSettingsHelper.getDownloadFinished());
        mOptions.setValues(mSettingsHelper.getShowOptions());

        updateSummaries();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();

        if (SettingsHelper.PROPERTY_DOWNLOAD_PATH.equals(key)) {
            selectDownloadPath();
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsHelper.PROPERTY_CHECK_TIME.equals(key)) {
            Utils.setAlarm(this, mSettingsHelper.getCheckTime(), false, true);
        }

        updateSummaries();
    }

    private void updateSummaries() {
        mDownloadPath.setSummary(mSettingsHelper.getDownloadPath());
        mOptions.setEntries(R.array.install_options_entries);
        mOptions.setEntryValues(R.array.install_options_values);
    }

    private void selectDownloadPath() {
        new DirectoryChooserDialog(this, new DirectoryChooserDialog.DirectoryChooserListener() {

            @Override
            public void onDirectoryChosen(String chosenDir) {
                mSettingsHelper.setDownloadPath(chosenDir);
                updateSummaries();
            }
        }).chooseDirectory();
    }
}