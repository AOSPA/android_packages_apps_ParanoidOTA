/*
 * Copyright (C) 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use mContext file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paranoid.paranoidota.updater;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import android.content.Context;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.http.URLStringReader;
import com.paranoid.paranoidota.updater.server.GooServer;
import com.paranoid.paranoidota.updater.server.PaServer;

public class GappsUpdater extends Updater {

    private static final Server[] SERVERS = {
        new PaServer(),
        new GooServer()
    };

    private SettingsHelper mSettingsHelper;
    private Server mServer;
    private String mPlatform;
    private long mVersion = -1L;
    private boolean mCanUpdate;
    private boolean mFromAlarm;
    private boolean mScanning;
    private int mCurrentServer = -1;

    public GappsUpdater(Context context, boolean fromAlarm) {
        super(context);
        mFromAlarm = fromAlarm;

        File file = new File("/system/etc/g.prop");
        mCanUpdate = file.exists();
        if (mCanUpdate) {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(file));
                String versionProperty = "ro.addon.pa_version";
                String versionString = properties.getProperty(versionProperty);
                if (versionString == null || "".equals(versionString) || versionProperty == null
                        || "".equals(versionProperty)) {
                    versionProperty = "ro.addon.version";
                    versionString = properties.getProperty(versionProperty);
                }
                mPlatform = Utils.getProp("ro.build.version.release");
                mPlatform = mPlatform.replace(".", "");
                if (mPlatform.length() > 2) {
                    mPlatform = mPlatform.substring(0, 2);
                }
                if (versionString == null || "".equals(versionString)) {
                    mCanUpdate = false;
                } else {
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
                mCanUpdate = false;
            }
        }
    }

    @Override
    public void onReadEnd(String buffer) {
        mScanning = false;
        try {
            PackageInfo[] lastGapps = null;
            setLastUpdates(null);
            List<PackageInfo> list = mServer.createPackageInfoList(buffer);
            String error = mServer.getError();
            lastGapps = list.toArray(new PackageInfo[list.size()]);
            if (lastGapps.length > 0) {
                if (mFromAlarm) {
                    Utils.showNotification(getContext(), null, lastGapps);
                }
            } else {
                if (error != null && !error.isEmpty()) {
                    if (versionError(error)) {
                        return;
                    }
                } else {
                    if (!mFromAlarm) {
                        Utils.showToastOnUiThread(getContext(), R.string.check_gapps_updates_no_new);
                    }
                }
            }
            mCurrentServer = -1;
            setLastUpdates(lastGapps);
            fireCheckCompleted(lastGapps);
        } catch (Exception ex) {
            System.out.println(buffer);
            onReadError(ex);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        ex.printStackTrace();
        versionError(null);
    }

    private boolean versionError(String error) {
        mScanning = false;
        if (mCurrentServer < SERVERS.length - 1) {
            nextServerCheck();
            return true;
        }
        if (!mFromAlarm) {
            if (error != null) {
                Utils.showToastOnUiThread(getContext(), R.string.check_gapps_updates_error + ": "
                        + error);
            } else {
                Utils.showToastOnUiThread(getContext(), R.string.check_gapps_updates_error);
            }
        }
        mCurrentServer = -1;
        fireCheckCompleted(null);
        return false;
    }

    public String getPlatform() {
        return mPlatform == null ? "0" : mPlatform;
    }

    @Override
    public long getVersion() {
        if (mVersion <= 0L) {
            return 0L;
        }
        return mVersion;
    }

    @Override
    public boolean isRom() {
        return false;
    }

    @Override
    public void check() {
        if (mFromAlarm) {
            if (mSettingsHelper == null) {
                mSettingsHelper = new SettingsHelper(getContext());
            }
            if (mSettingsHelper.getCheckTime() < 0 || !mSettingsHelper.getCheckGapps()) {
                return;
            }
        }
        mScanning = true;
        fireStartChecking();
        nextServerCheck();
    }

    private void nextServerCheck() {
        mScanning = true;
        mCurrentServer++;
        mServer = SERVERS[mCurrentServer];
        new URLStringReader(this).execute(mServer.getUrl("gapps",
                Long.parseLong(getPlatform() + getVersion())));
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

}
