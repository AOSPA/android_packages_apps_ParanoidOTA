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
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.Version;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.updater.server.BasketServer;
import com.paranoid.paranoidota.updater.server.GooServer;

public class GappsUpdater extends Updater {

    private static final Server[] SERVERS = {
        new BasketServer(false),
        new GooServer(false)
    };

    private RequestQueue mQueue;
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

        mQueue = Volley.newRequestQueue(context);

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
    public void onResponse(JSONObject response) {
        mScanning = false;
        try {
            PackageInfo[] lastGapps = null;
            setLastUpdates(null);
            List<PackageInfo> list = mServer.createPackageInfoList(response);
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
            System.out.println(response.toString());
            ex.printStackTrace();
            versionError(null);
        }
    }

    @Override
    public void onErrorResponse(VolleyError ex) {
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
        fireCheckError(false);
        return false;
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
    public void check() {
        if (mSettingsHelper == null) {
            mSettingsHelper = new SettingsHelper(getContext());
        }
        if (mFromAlarm) {
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
        String gapps = "gapps";
        if (mSettingsHelper.getCheckGappsMini()) {
            gapps += "-mini";
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, mServer.getUrl(
                gapps, getVersion()), null, this, this);
        mQueue.add(jsObjRequest);
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

}
