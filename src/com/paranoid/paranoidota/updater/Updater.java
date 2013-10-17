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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.android.volley.Response;

import android.app.Activity;
import android.content.Context;

public abstract class Updater implements Response.Listener<JSONObject>, Response.ErrorListener {

    public interface PackageInfo extends Serializable {

        public String getMd5();
        public String getFilename();
        public String getPath();
        public String getSize();
        public long getVersion();
        public boolean isDelta();
        public String getDeltaFilename();
        public String getDeltaPath();
        public String getDeltaMd5();
        public boolean isGapps();
    }

    public static final String PROPERTY_DEVICE = "ro.pa.device";
    public static final String PROPERTY_DEVICE_EXT = "ro.product.device";

//    public static final int ROM_NOTIFICATION_ID = 122303222;
//    public static final int GAPPS_NOTIFICATION_ID = 122303224;
    public static final int NOTIFICATION_ID = 122303225;

    public static interface UpdaterListener {

        public void startChecking(boolean isRom);

        public void versionFound(PackageInfo[] info, boolean isRom);

        public void checkError(boolean isRom);
    }

    private Context mContext;
    private PackageInfo[] mLastUpdates;
    private List<UpdaterListener> mListeners = new ArrayList<UpdaterListener>();

    public Updater(Context context) {
        mContext = context;
    }

    public abstract long getVersion();

    public abstract void check();

    public abstract boolean isScanning();

    public abstract boolean isRom();

    protected Context getContext() {
        return mContext;
    }

    public PackageInfo[] getLastUpdates() {
        return mLastUpdates;
    }

    public void setLastUpdates(PackageInfo[] infos) {
        mLastUpdates = infos;
    }

    public void addUpdaterListener(UpdaterListener listener) {
        mListeners.add(listener);
    }

    public void removeUpdaterListener(UpdaterListener listener) {
        mListeners.remove(listener);
    }

    protected void fireStartChecking() {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
    
                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.startChecking(isRom());
                    }
                }
            });
        }
    }

    protected void fireCheckCompleted(final PackageInfo[] info) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
    
                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.versionFound(info, isRom());
                    }
                }
            });
        }
    }

    protected void fireCheckError(final boolean isRom) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
    
                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.checkError(isRom);
                    }
                }
            });
        }
    }
}
