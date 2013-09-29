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

package com.paranoid.paranoidota;

import com.paranoid.paranoidota.helpers.SettingsHelper;

import android.content.Context;
import android.database.AbstractCursor;

public class InstallOptionsCursor extends AbstractCursor {

    private static final String[] COLUMN_NAMES = { "_id", "TEXT", "CHECKED" };

    private String[] mOption;
    private String[] mText;
    private int[] mChecked;
    private int mCount = 0;

    public InstallOptionsCursor(Context context) {
        SettingsHelper mSettingsHelper = new SettingsHelper(context);

        for (int i = 0; i < SettingsHelper.INSTALL_OPTIONS.length; i++) {
            if (mSettingsHelper.isShowOption(SettingsHelper.INSTALL_OPTIONS[i])) {
                mCount++;
            }
        }
        mOption = new String[mCount];
        mText = new String[mCount];
        mChecked = new int[mCount];
        int count = 0;
        for (int i = 0; i < SettingsHelper.INSTALL_OPTIONS.length; i++) {
            if (mSettingsHelper.isShowOption(SettingsHelper.INSTALL_OPTIONS[i])) {
                mOption[count] = SettingsHelper.INSTALL_OPTIONS[i];
                mText[count] = context.getResources().getString(
                        getText(SettingsHelper.INSTALL_OPTIONS[i]));
                mChecked[count] = 0;
                count++;
            }
        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public String[] getColumnNames() {
        return COLUMN_NAMES;
    }

    @Override
    public String getString(int column) {
        switch (column) {
            case 0:
                return mOption[getPosition()];
            case 1:
                return mText[getPosition()];
        }
        return null;
    }

    @Override
    public short getShort(int column) {
        return 0;
    }

    @Override
    public int getInt(int column) {
        if (column == 2) {
            return mChecked[getPosition()];
        }
        return 0;
    }

    @Override
    public long getLong(int column) {
        return 0;
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }

    public void setOption(int which, boolean isChecked) {
        mChecked[which] = isChecked ? 1 : 0;
    }

    public boolean isWipeSystem() {
        return isOption("WIPESYSTEM");
    }

    public boolean isWipeData() {
        return isOption("WIPEDATA");
    }

    public boolean isWipeCaches() {
        return isOption("WIPECACHES");
    }

    public boolean isBackup() {
        return isOption("BACKUP");
    }

    public boolean hasWipeSystem() {
        return hasOption("WIPESYSTEM");
    }

    public boolean hasWipeData() {
        return hasOption("WIPEDATA");
    }

    public boolean hasWipeCaches() {
        return hasOption("WIPECACHES");
    }

    public boolean hasBackup() {
        return hasOption("BACKUP");
    }

    public String getIsCheckedColumn() {
        return "CHECKED";
    }

    public String getLabelColumn() {
        return "TEXT";
    }

    private boolean isOption(String option) {
        for (int i = 0; i < getCount(); i++) {
            if (option.equals(mOption[i])) {
                return mChecked[i] == 1;
            }
        }
        return false;
    }

    private boolean hasOption(String option) {
        for (int i = 0; i < getCount(); i++) {
            if (option.equals(mOption[i])) {
                return true;
            }
        }
        return false;
    }

    private int getText(String option) {
        if (SettingsHelper.INSTALL_BACKUP.equals(option)) {
            return R.string.backup;
        } else if (SettingsHelper.INSTALL_WIPESYSTEM.equals(option)) {
            return R.string.wipe_system;
        } else if (SettingsHelper.INSTALL_WIPEDATA.equals(option)) {
            return R.string.wipe_data;
        } else if (SettingsHelper.INSTALL_WIPECACHES.equals(option)) {
            return R.string.wipe_caches;
        }
        return -1;
    }
}