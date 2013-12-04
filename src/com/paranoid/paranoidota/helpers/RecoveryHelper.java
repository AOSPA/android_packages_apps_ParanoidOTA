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

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.helpers.recovery.CwmBasedRecovery;
import com.paranoid.paranoidota.helpers.recovery.RecoveryInfo;
import com.paranoid.paranoidota.helpers.recovery.TwrpRecovery;

public class RecoveryHelper {

    private SparseArray<RecoveryInfo> recoveries = new SparseArray<RecoveryInfo>();
    private SettingsHelper mSettings;
    private Context mContext;

    public RecoveryHelper(Context context) {

        mContext = context;
        mSettings = new SettingsHelper(context);

        recoveries.put(R.id.cwmbased, new CwmBasedRecovery(context));
        recoveries.put(R.id.twrp, new TwrpRecovery());

        if (!mSettings.existsRecovery()) {
            test();
        }
    }

    public void selectRecovery() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.selection_recovery,
                (ViewGroup) ((Activity) mContext).findViewById(R.id.recovery_layout));

        RadioButton cbCwmbased = (RadioButton) view.findViewById(R.id.cwmbased);
        RadioButton cbTwrp = (RadioButton) view.findViewById(R.id.twrp);

        final RadioGroup mGroup = (RadioGroup) view.findViewById(R.id.recovery_radio_group);

        RecoveryInfo info = getRecovery();
        if (info == null) {
            cbCwmbased.setChecked(true);
        } else {
            switch (info.getId()) {
                case R.id.twrp:
                    cbTwrp.setChecked(true);
                    break;
                default:
                    cbCwmbased.setChecked(true);
                    break;
            }
        }

        new AlertDialog.Builder(mContext).setTitle(R.string.recovery_select_alert_title)
                .setCancelable(false).setMessage(R.string.recovery_select_alert_summary)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        int id = mGroup.getCheckedRadioButtonId();

                        setRecovery(id);

                        dialog.dismiss();
                    }
                }).show();
    }

    public void selectSdcard(final boolean internal) {

        final EditText input = new EditText(mContext);
        input.setText(internal ? mSettings.getInternalStorage() : mSettings.getExternalStorage());

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.recovery_select_sdcard_alert_title)
                .setMessage(R.string.recovery_select_sdcard_alert_summary)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();

                        if (value == null || "".equals(value.trim())) {
                            Toast.makeText(mContext, R.string.recovery_select_sdcard_alert_error,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        if (value.startsWith("/")) {
                            value = value.substring(1);
                        }

                        if (internal) {
                            mSettings.setInternalStorage(value);
                        } else {
                            mSettings.setExternalStorage(value);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public RecoveryInfo getRecovery() {
        String recovery = mSettings.getRecovery();
        for (int i = 0; i < recoveries.size(); i++) {
            int key = recoveries.keyAt(i);
            RecoveryInfo info = recoveries.get(key);
            if (info.getName().equals(recovery)) {
                return info;
            }
        }
        return null;
    }

    public void setRecovery(int id) {
        RecoveryInfo info = recoveries.get(id);
        mSettings.setRecovery(info.getName());
        mSettings.setInternalStorage(info.getInternalSdcard());
        mSettings.setExternalStorage(info.getExternalSdcard());
    }

    public String getCommandsFile() {

        RecoveryInfo info = getRecovery();

        return info.getCommandsFile();
    }

    public String getRecoveryFilePath(String filePath) {

        String internalStorage = mSettings.getInternalStorage();
        String externalStorage = mSettings.getExternalStorage();

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

    public String[] getCommands(String[] items, String[] originalItems, boolean wipeSystem,
            boolean wipeData, boolean wipeCaches, String backupFolder, String backupOptions)
            throws Exception {

        RecoveryInfo info = getRecovery();

        return info.getCommands(mContext, items, originalItems, wipeSystem, wipeData, wipeCaches,
                backupFolder, backupOptions);
    }

    private void test() {

        File folderTwrp = new File(IOUtils.SDCARD + recoveries.get(R.id.twrp).getFolderPath());
        File folderCwm = new File(IOUtils.SDCARD + recoveries.get(R.id.cwmbased).getFolderPath());

        if ((folderTwrp.exists() && folderCwm.exists()) || (!folderTwrp.exists() && !folderCwm.exists())) {
            selectRecovery();
        } else if (folderTwrp.exists()) {
            setRecovery(R.id.twrp);
            Toast.makeText(
                    mContext,
                    mContext.getString(R.string.recovery_changed,
                            mContext.getString(R.string.recovery_twrp)), Toast.LENGTH_LONG).show();
        } else if (folderCwm.exists()) {
            setRecovery(R.id.cwmbased);
            Toast.makeText(
                    mContext,
                    mContext.getString(R.string.recovery_changed,
                            mContext.getString(R.string.recovery_cwm)), Toast.LENGTH_LONG).show();
        }
    }
}
