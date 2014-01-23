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

package com.paranoid.paranoidota;

import com.paranoid.paranoidota.updater.GappsUpdater;
import com.paranoid.paranoidota.updater.RomUpdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationAlarm extends BroadcastReceiver {

    private RomUpdater mRomUpdater;
    private GappsUpdater mGappsUpdater;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (mRomUpdater == null) {
            mRomUpdater = new RomUpdater(context, true);
        }
        if (mGappsUpdater == null) {
            mGappsUpdater = new GappsUpdater(context, true);
        }
        if (Utils.isNetworkAvailable(context)) {
            mRomUpdater.check();
            mGappsUpdater.check();
        }
    }
}