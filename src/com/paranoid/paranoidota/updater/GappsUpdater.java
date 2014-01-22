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

package com.paranoid.paranoidota.updater;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import android.content.Context;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.Version;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.updater.server.GooServer;

public class GappsUpdater extends Updater {

    private static final String PROPERTIES_FILE = "/system/etc/g.prop";
    private static final String VERSION_PROPERTY = "ro.addon.pa_version";
    private static final String VERSION_PROPERTY_EXT = "ro.addon.version";
    private static final String PLATFORM_PROPERTY = "ro.build.version.release";

    private String mPlatform;
    private long mVersion = -1L;

    public GappsUpdater(Context context, boolean fromAlarm) {
        super(context, new Server[] { new GooServer(context, false) }, fromAlarm);

        File file = new File(PROPERTIES_FILE);
        if (file.exists()) {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(file));
                String versionString = properties.getProperty(VERSION_PROPERTY);
                if (versionString == null || "".equals(versionString)) {
                    versionString = properties.getProperty(VERSION_PROPERTY_EXT);
                }
                mPlatform = Utils.getProp(PLATFORM_PROPERTY);
                mPlatform = mPlatform.replace(".", "");
                while (mPlatform.length() < 3) {
                    mPlatform = mPlatform + "0";
                }
                if (versionString != null && !"".equals(versionString)) {
                    String[] version = versionString.split("-");
                    for (int i = 0; i < version.length; i++) {
                        try {
                            mVersion = Long.parseLong(version[i]);
                            break;
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String getPlatform() {
        return mPlatform == null ? "0" : mPlatform;
    }

    @Override
    public Version getVersion() {
        return Version.fromGapps(mPlatform, mVersion);
    }

    @Override
    public boolean isRom() {
        return false;
    }

    @Override
    public String getDevice() {
        switch (getSettingsHelper().getGappsType()) {
            case SettingsHelper.GAPPS_MINI :
                return "gapps-mini";
            case SettingsHelper.GAPPS_STOCK:
                return "gapps";
            case SettingsHelper.GAPPS_FULL :
            default :
                return "gapps-full";
        }
    }

    @Override
    public int getErrorStringId() {
        return R.string.check_gapps_updates_error;
    }

    @Override
    public int getNoUpdatesStringId() {
        return R.string.check_gapps_updates_no_new;
    }

}
