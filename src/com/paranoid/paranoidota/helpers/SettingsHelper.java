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

package com.paranoid.paranoidota.helpers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class SettingsHelper {

    public static final int GAPPS_FULL = 0;
    public static final int GAPPS_MINI = 1;
    public static final int GAPPS_STOCK = 2;

    // install options
    public static final String INSTALL_BACKUP = "BACKUP";
    public static final String INSTALL_WIPESYSTEM = "WIPESYSTEM";
    public static final String INSTALL_WIPEDATA = "WIPEDATA";
    public static final String INSTALL_WIPECACHES = "WIPECACHES";
    public static final String[] INSTALL_OPTIONS = { INSTALL_BACKUP, INSTALL_WIPESYSTEM,
            INSTALL_WIPEDATA, INSTALL_WIPECACHES };

    public static final String PROPERTY_EXPERT = "expertmode";
    public static final String PROPERTY_CHECK_TIME = "checktime";
    public static final String PROPERTY_CHECK_GAPPS = "checkgapps";
    public static final String PROPERTY_GAPPS_TYPE = "gappstype";
    public static final String PROPERTY_DOWNLOAD_PATH = "downloadpath";
    public static final String PROPERTY_DOWNLOAD_FINISHED = "downloadfinished";
    public static final String PROPERTY_RECOVERY = "recovery";
    public static final String PROPERTY_INTERNAL_STORAGE = "internal-storage";
    public static final String PROPERTY_EXTERNAL_STORAGE = "external-storage";
    public static final String PROPERTY_SETTINGS_RECOVERY = "settings_recovery";
    public static final String PROPERTY_SHOW_OPTIONS = "showoptions";

    public static final String DOWNLOAD_ROM_ID = "download_rom_id";
    public static final String DOWNLOAD_GAPPS_ID = "download_gapps_id";
    public static final String DOWNLOAD_ROM_MD5 = "download_rom_md5";
    public static final String DOWNLOAD_GAPPS_MD5 = "download_gapps_md5";
    public static final String DOWNLOAD_ROM_FILENAME = "download_rom_filaname";
    public static final String DOWNLOAD_GAPPS_FILENAME = "download_gapps_filename";

    private static final boolean DEFAULT_EXPERT = false;
    private static final String DEFAULT_CHECK_TIME = "18000000"; // five hours
    private static final boolean DEFAULT_CHECK_GAPPS = true;
    private static final int DEFAULT_GAPPS_TYPE = GAPPS_FULL;
    private static final String DEFAULT_DOWNLOAD_PATH = new File(Environment
            .getExternalStorageDirectory(), "paranoidota/").getAbsolutePath();
    private static final boolean DEFAULT_DOWNLOAD_FINISHED = true;
    private static final String DEFAULT_RECOVERY = "cwmbased";
    private static final String DEFAULT_INTERNAL_STORAGE = "emmc";
    private static final String DEFAULT_EXTERNAL_STORAGE = "sdcard";
    private static final Set<String> DEFAULT_SHOW_OPTIONS = new HashSet<String>();

    private SharedPreferences settings;
    private Context mContext;

    public SettingsHelper(Context context) {
        mContext = context;

        settings = PreferenceManager.getDefaultSharedPreferences(context);

        DEFAULT_SHOW_OPTIONS.add(INSTALL_BACKUP);
        DEFAULT_SHOW_OPTIONS.add(INSTALL_WIPEDATA);
        DEFAULT_SHOW_OPTIONS.add(INSTALL_WIPECACHES);
    }

    public boolean getExpertMode() {
        return settings.getBoolean(PROPERTY_EXPERT, DEFAULT_EXPERT);
    }

    public String getInternalStorage() {
        return settings.getString(PROPERTY_INTERNAL_STORAGE, DEFAULT_INTERNAL_STORAGE);
    }

    public void setInternalStorage(String value) {
        savePreference(PROPERTY_INTERNAL_STORAGE, value);
    }

    public String getExternalStorage() {
        return settings.getString(PROPERTY_EXTERNAL_STORAGE, DEFAULT_EXTERNAL_STORAGE);
    }

    public void setExternalStorage(String value) {
        savePreference(PROPERTY_EXTERNAL_STORAGE, value);
    }

    public boolean existsRecovery() {
        return settings.contains(PROPERTY_RECOVERY);
    }

    public String getRecovery() {
        return settings.getString(PROPERTY_RECOVERY, DEFAULT_RECOVERY);
    }

    public void setRecovery(String value) {
        savePreference(PROPERTY_RECOVERY, value);
    }

    public boolean isShowOption(String option) {
        Set<String> opts = settings.getStringSet(PROPERTY_SHOW_OPTIONS, DEFAULT_SHOW_OPTIONS);
        return opts.contains(option);
    }

    public Set<String> getShowOptions() {
        return settings.getStringSet(PROPERTY_SHOW_OPTIONS, DEFAULT_SHOW_OPTIONS);
    }

    public void setShowOptions(String options) {
        savePreference(PROPERTY_SHOW_OPTIONS, options);
    }

    public String getDownloadPath() {
        return settings.getString(PROPERTY_DOWNLOAD_PATH, DEFAULT_DOWNLOAD_PATH);
    }

    public void setDownloadPath(String path) {
        savePreference(PROPERTY_DOWNLOAD_PATH, path);
    }

    public boolean getDownloadFinished() {
        return settings.getBoolean(PROPERTY_DOWNLOAD_FINISHED, DEFAULT_DOWNLOAD_FINISHED);
    }

    public long getCheckTime() {
        return Long.parseLong(settings.getString(PROPERTY_CHECK_TIME, DEFAULT_CHECK_TIME));
    }

    public boolean getCheckGapps() {
        return settings.getBoolean(PROPERTY_CHECK_GAPPS, DEFAULT_CHECK_GAPPS);
    }

    public int getGappsType() {
        return Integer.parseInt(settings.getString(PROPERTY_GAPPS_TYPE,
                String.valueOf(DEFAULT_GAPPS_TYPE)));
    }

    public void setDownloadRomId(Long id, String md5, String fileName) {
        if (id == null) {
            removePreference(DOWNLOAD_ROM_ID);
            removePreference(DOWNLOAD_ROM_MD5);
            removePreference(DOWNLOAD_ROM_FILENAME);
        } else {
            savePreference(DOWNLOAD_ROM_ID, String.valueOf(id));
            savePreference(DOWNLOAD_ROM_MD5, md5);
            savePreference(DOWNLOAD_ROM_FILENAME, fileName);
        }
    }

    public long getDownloadRomId() {
        return Long.parseLong(settings.getString(DOWNLOAD_ROM_ID, "-1"));
    }

    public String getDownloadRomMd5() {
        return settings.getString(DOWNLOAD_ROM_MD5, null);
    }

    public String getDownloadRomName() {
        return settings.getString(DOWNLOAD_ROM_FILENAME, null);
    }

    public void setDownloadGappsId(Long id, String md5, String fileName) {
        if (id == null) {
            removePreference(DOWNLOAD_GAPPS_ID);
            removePreference(DOWNLOAD_GAPPS_MD5);
            removePreference(DOWNLOAD_GAPPS_FILENAME);
        } else {
            savePreference(DOWNLOAD_GAPPS_ID, String.valueOf(id));
            savePreference(DOWNLOAD_GAPPS_MD5, md5);
            savePreference(DOWNLOAD_GAPPS_FILENAME, fileName);
        }
    }

    public long getDownloadGappsId() {
        return Long.parseLong(settings.getString(DOWNLOAD_GAPPS_ID, "-1"));
    }

    public String getDownloadGappsMd5() {
        return settings.getString(DOWNLOAD_GAPPS_MD5, null);
    }

    public String getDownloadGappsName() {
        return settings.getString(DOWNLOAD_GAPPS_FILENAME, null);
    }

    private void savePreference(String preference, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(preference, value);
        editor.commit();
    }

    private void removePreference(String preference) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(preference);
        editor.commit();
    }
}
