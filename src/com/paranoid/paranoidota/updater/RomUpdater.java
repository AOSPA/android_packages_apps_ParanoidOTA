/*
 * Copyright 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paranoid.paranoidota.updater;

import java.util.List;

import android.content.Context;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.http.URLStringReader;
import com.paranoid.paranoidota.updater.server.GooServer;
import com.paranoid.paranoidota.updater.server.PaServer;

public class RomUpdater extends Updater {

    private static final Server[] SERVERS = {
        new PaServer(),
        new GooServer()
    };

    private SettingsHelper mSettingsHelper;
    private Server mServer;
    private boolean mScanning = false;
    private boolean mFromAlarm;
    private int mCurrentServer = -1;

    public RomUpdater(Context context, boolean fromAlarm) {
        super(context);
        mFromAlarm = fromAlarm;
    }

    @Override
    public void check() {
        if (mFromAlarm) {
            if (mSettingsHelper == null) {
                mSettingsHelper = new SettingsHelper(getContext());
            }
            if (mSettingsHelper.getCheckTime() < 0) {
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
        new URLStringReader(this).execute(mServer.getUrl(getDevice(), getVersion()));
    }

    @Override
    public long getVersion() {
        String version = Utils.getProp(Utils.MOD_VERSION);
        return Utils.parseRomVersion(version);
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

    @Override
    public boolean isRom() {
        return true;
    }

    private String getDevice() {
        String device = Utils.getProp(PROPERTY_DEVICE);
        if (device == null || device.isEmpty()) {
            device = Utils.getProp(PROPERTY_DEVICE_EXT);
        }
        return device;
    }

    @Override
    public void onReadEnd(String buffer) {
        try {
            mScanning = false;
            PackageInfo[] lastRoms = null;
            setLastUpdates(null);
            List<PackageInfo> list = mServer.createPackageInfoList(buffer);
            lastRoms = list.toArray(new PackageInfo[list.size()]);
            String error = mServer.getError();
            if (list.size() > 0) {
                if (mFromAlarm) {
                    Utils.showNotification(getContext(), lastRoms, null);
                }
            } else {
                if (error != null && !error.isEmpty()) {
                    if (versionError(error)) {
                        return;
                    }
                } else {
                    if (!mFromAlarm) {
                        Utils.showToastOnUiThread(getContext(), R.string.check_rom_updates_no_new);
                    }
                }
            }
            mCurrentServer = -1;
            setLastUpdates(lastRoms);
            fireCheckCompleted(lastRoms);
        } catch (Exception ex) {
            mScanning = false;
            ex.printStackTrace();
            versionError(null);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        mScanning = false;
        versionError(null);
    }

    private boolean versionError(String error) {
        if (mCurrentServer < SERVERS.length - 1) {
            nextServerCheck();
            return true;
        }
        if (!mFromAlarm) {
            if (error != null) {
                Utils.showToastOnUiThread(getContext(),
                        getContext().getResources().getString(R.string.check_rom_updates_error)
                                + ": " + error);
            } else {
                Utils.showToastOnUiThread(getContext(), R.string.check_rom_updates_error);
            }
        }
        mCurrentServer = -1;
        fireCheckCompleted(null);
        fireCheckError(true);
        return false;
    }
}
