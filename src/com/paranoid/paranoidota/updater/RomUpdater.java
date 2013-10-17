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

import java.util.List;

import android.content.Context;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.http.URLStringReader;
import com.paranoid.paranoidota.updater.server.BasketServer;
import com.paranoid.paranoidota.updater.server.GooServer;

public class RomUpdater extends Updater {

    private static final Server[] SERVERS = {
        new BasketServer(true),
        new GooServer(true)
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
            device = Utils.translateDeviceName(getContext(), device);
        }
        return device == null ? "" : device.toLowerCase();
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
