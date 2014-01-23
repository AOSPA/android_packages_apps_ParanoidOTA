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

import java.io.File;
import java.util.Scanner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.SparseArray;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.recovery.CwmBasedRecovery;
import com.paranoid.paranoidota.helpers.recovery.RecoveryInfo;
import com.paranoid.paranoidota.helpers.recovery.TwrpRecovery;

public class RecoveryHelper {

    private SparseArray<RecoveryInfo> mRecoveries = new SparseArray<RecoveryInfo>();
    private Context mContext;

    public RecoveryHelper(Context context) {

        mContext = context;

        mRecoveries.put(Utils.CWM_BASED, new CwmBasedRecovery(context));
        mRecoveries.put(Utils.TWRP, new TwrpRecovery());
    }

    public RecoveryInfo getRecovery(int id) {
        for (int i = 0; i < mRecoveries.size(); i++) {
            int key = mRecoveries.keyAt(i);
            RecoveryInfo info = mRecoveries.get(key);
            if (info.getId() == id) {
                return info;
            }
        }
        return null;
    }

    public String getCommandsFile(int id) {

        RecoveryInfo info = getRecovery(id);

        return info.getCommandsFile();
    }

    public String getRecoveryFilePath(int id, String filePath) {

        RecoveryInfo info = getRecovery(id);

        String internalStorage = info.getInternalSdcard();
        String externalStorage = info.getExternalSdcard();

        String primarySdcard = IOUtils.getPrimarySdCard();
        String secondarySdcard = IOUtils.getSecondarySdCard();

        String[] internalNames = new String[] {
                primarySdcard,
                "/mnt/sdcard",
                "/storage/sdcard/",
                "/sdcard",
                "/storage/sdcard0",
                "/storage/emulated/0" };
        String[] externalNames = new String[] {
                secondarySdcard == null ? " " : secondarySdcard,
                "/mnt/extSdCard",
                "/storage/extSdCard/",
                "/extSdCard",
                "/storage/sdcard1",
                "/storage/emulated/1" };
        for (int i = 0; i < internalNames.length; i++) {
            String internalName = internalNames[i];
            String externalName = externalNames[i];
            if (filePath.startsWith(externalName)) {
                filePath = filePath.replace(externalName, "/" + externalStorage);
                break;
            } else if (filePath.startsWith(internalName)) {
                filePath = filePath.replace(internalName, "/" + internalStorage);
                break;
            }
        }

        while (filePath.startsWith("//")) {
            filePath = filePath.substring(1);
        }

        return filePath;
    }

    public String[] getCommands(int id, String[] items, String[] originalItems, boolean wipeData,
            boolean wipeCaches, String backupFolder, String backupOptions) throws Exception {

        RecoveryInfo info = getRecovery(id);

        return info.getCommands(mContext, items, originalItems, wipeData, wipeCaches, backupFolder,
                backupOptions);
    }
}
