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

package com.paranoid.paranoidota.helpers.recovery;

import android.content.Context;

public abstract class RecoveryInfo {

    private int id;
    private String name = null;
    private String internalSdcard = null;
    private String externalSdcard = null;

    public RecoveryInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternalSdcard() {
        return internalSdcard;
    }

    public void setInternalSdcard(String sdcard) {
        this.internalSdcard = sdcard;
    }

    public String getExternalSdcard() {
        return externalSdcard;
    }

    public void setExternalSdcard(String sdcard) {
        this.externalSdcard = sdcard;
    }

    public abstract String getCommandsFile();

    public abstract String[] getCommands(Context context, String[] items, String[] originalItems,
            boolean wipeData, boolean wipeCaches, String backupFolder, String backupOptions) throws Exception;
}