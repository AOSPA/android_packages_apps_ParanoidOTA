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
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.paranoid.paranoidota.DirectoryChooserDialog;
import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.RecoveryHelper;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.helpers.recovery.RecoveryInfo;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private SettingsHelper mSettingsHelper;
    private RecoveryHelper mRecoveryHelper;
    private CheckBoxPreference mExpertMode;
    private ListPreference mCheckTime;
    private CheckBoxPreference mCheckGapps;
    private ListPreference mGappsType;
    private Preference mDownloadPath;
    private CheckBoxPreference mDownloadFinished;
    private PreferenceCategory mRecoveryCategory;
    private Preference mRecovery;
    private Preference mInternalSdcard;
    private Preference mExternalSdcard;
    private MultiSelectListPreference mOptions;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        mSettingsHelper = new SettingsHelper(this);
        mRecoveryHelper = new RecoveryHelper(this);

        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.layout.activity_settings);

        mExpertMode = (CheckBoxPreference) findPreference(SettingsHelper.PROPERTY_EXPERT);
        mCheckTime = (ListPreference) findPreference(SettingsHelper.PROPERTY_CHECK_TIME);
        mCheckGapps = (CheckBoxPreference) findPreference(SettingsHelper.PROPERTY_CHECK_GAPPS);
        mGappsType = (ListPreference) findPreference(SettingsHelper.PROPERTY_GAPPS_TYPE);
        mDownloadPath = findPreference(SettingsHelper.PROPERTY_DOWNLOAD_PATH);
        mDownloadFinished = (CheckBoxPreference) findPreference(SettingsHelper.PROPERTY_DOWNLOAD_FINISHED);
        mRecovery = findPreference(SettingsHelper.PROPERTY_RECOVERY);
        mInternalSdcard = findPreference(SettingsHelper.PROPERTY_INTERNAL_STORAGE);
        mExternalSdcard = findPreference(SettingsHelper.PROPERTY_EXTERNAL_STORAGE);
        mRecoveryCategory = (PreferenceCategory) findPreference(SettingsHelper.PROPERTY_SETTINGS_RECOVERY);
        mOptions = (MultiSelectListPreference) findPreference(SettingsHelper.PROPERTY_SHOW_OPTIONS);

        if (!IOUtils.hasSecondarySdCard()) {
            mRecoveryCategory.removePreference(mExternalSdcard);
        }

        mExpertMode.setDefaultValue(mSettingsHelper.getExpertMode());
        mCheckTime.setValue(String.valueOf(mSettingsHelper.getCheckTime()));
        mCheckGapps.setChecked(mSettingsHelper.getCheckGapps());
        mGappsType.setValue(String.valueOf(mSettingsHelper.getGappsType()));
        mDownloadFinished.setChecked(mSettingsHelper.getDownloadFinished());
        mOptions.setValues(mSettingsHelper.getShowOptions());

        updateSummaries();
        addOrRemovePreferences();

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
        } else if (SettingsHelper.PROPERTY_RECOVERY.equals(key)) {
            mRecoveryHelper.selectRecovery();
        } else if (SettingsHelper.PROPERTY_INTERNAL_STORAGE.equals(key)) {
            mRecoveryHelper.selectSdcard(true);
        } else if (SettingsHelper.PROPERTY_EXTERNAL_STORAGE.equals(key)) {
            mRecoveryHelper.selectSdcard(false);
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsHelper.PROPERTY_EXPERT.equals(key)) {
            addOrRemovePreferences();
        } else if (SettingsHelper.PROPERTY_CHECK_TIME.equals(key)) {
            Utils.setAlarm(this, mSettingsHelper.getCheckTime(), false, true);
        }

        updateSummaries();
    }

    private void updateSummaries() {
        Resources res = getResources();
        mDownloadPath.setSummary(mSettingsHelper.getDownloadPath());
        RecoveryInfo info = mRecoveryHelper.getRecovery();
        mRecovery.setSummary(res.getText(R.string.settings_selectrecovery_summary)
                + " (" + info.getName() + ")");
        mInternalSdcard.setSummary(res.getText(R.string.settings_internalsdcard_summary)
                + " (" + mSettingsHelper.getInternalStorage() + ")");
        mExternalSdcard.setSummary(res.getText(R.string.settings_externalsdcard_summary)
                + " (" + mSettingsHelper.getExternalStorage() + ")");
        mOptions.setEntries(R.array.install_options_entries);
        mOptions.setEntryValues(R.array.install_options_values);
    }

    @SuppressWarnings("deprecation")
    private void addOrRemovePreferences() {
        boolean expert = mSettingsHelper.getExpertMode();
        if (expert) {
            getPreferenceScreen().addPreference(mRecoveryCategory);
        } else {
            getPreferenceScreen().removePreference(mRecoveryCategory);
        }
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