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

public class NotificationHelper {

    public static final int NO_UPDATE = -1;

    private NotificationCallback mCallback;
    private int mRomNotifications;
    private int mGappsNotifications;

    public interface NotificationCallback {
        public abstract void updateNotifications(int notifications);
    }

    public NotificationHelper(NotificationCallback callback) {
        mCallback = callback;
    }

    public void setNotifications(int rom, int gapps) {
        if(rom == NO_UPDATE) {
            rom = mRomNotifications;
        }
        if(gapps == NO_UPDATE) {
           gapps = mGappsNotifications;
        }
        mRomNotifications = rom;
        mGappsNotifications = gapps;
        mCallback.updateNotifications(rom + gapps);
    }
}
