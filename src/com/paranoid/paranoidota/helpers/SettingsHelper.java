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

package com.paranoid.paranoidota.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsHelper {

    public static final int GAPPS_FULL = 0;
    public static final int GAPPS_MINI = 1;
    public static final int GAPPS_STOCK = 2;
    public static final int GAPPS_MICRO = 3;

    public static final String PROPERTY_CHECK_TIME = "checktime";
    public static final String PROPERTY_CHECK_GAPPS = "checkgapps";
    public static final String PROPERTY_GAPPS_TYPE = "gappstype";

    public static final String DOWNLOAD_ROM_ID = "download_rom_id";
    public static final String DOWNLOAD_GAPPS_ID = "download_gapps_id";
    public static final String DOWNLOAD_ROM_MD5 = "download_rom_md5";
    public static final String DOWNLOAD_GAPPS_MD5 = "download_gapps_md5";
    public static final String DOWNLOAD_ROM_FILENAME = "download_rom_filaname";
    public static final String DOWNLOAD_GAPPS_FILENAME = "download_gapps_filename";

    private static final String DEFAULT_CHECK_TIME = "18000000"; // five hours
    private static final boolean DEFAULT_CHECK_GAPPS = true;
    private static final int DEFAULT_GAPPS_TYPE = GAPPS_FULL;

    private SharedPreferences settings;

    public SettingsHelper(Context context) {

        settings = PreferenceManager.getDefaultSharedPreferences(context);
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
