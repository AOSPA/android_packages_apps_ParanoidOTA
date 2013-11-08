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

package com.paranoid.paranoidota.helpers.recovery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.R;

public class StockRecovery extends RecoveryInfo {

    public StockRecovery() {
        super();

        setId(R.id.stock);
        setName("stock");
        setInternalSdcard("sdcard");
        setExternalSdcard("external_sd");
    }

    @Override
    public String getFullName(Context context) {
        return context.getString(R.string.recovery_stock);
    }

    @Override
    public String getFolderPath() {
        return null;
    }

    @Override
    public String getCommandsFile() {
        return "command";
    }

    @Override
    public String[] getCommands(Context context, String[] items, String[] originalItems,
            boolean wipeSystem, boolean wipeData, boolean wipeCaches, String backupFolder,
            String backupOptions) throws Exception {

        List<String> commands = new ArrayList<String>();

        int size = items.length, i = 0;

        if (wipeData) {
            commands.add("--wipe_data");
        }

        if (wipeCaches) {
            commands.add("--wipe_cache");
        }

        if (size > 0) {
            commands.add("--disable_verification");
            for (; i < size; i++) {
                File file = new File(originalItems[i]);
                IOUtils.copyOrRemoveCache(file, true);
                commands.add("--update_package=/cache/" + file.getName());
            }
        }

        return commands.toArray(new String[commands.size()]);
    }
}
