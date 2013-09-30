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
package com.paranoid.paranoidota.updater.server;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.paranoid.paranoidota.updater.Server;
import com.paranoid.paranoidota.updater.UpdatePackage;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

public class PaServer implements Server {

    private static final String URL = "http://api.paranoidandroid.co/updates/%s?v=%s";

    private String mDevice = null;
    private String mError = null;

    @Override
    public String getUrl(String device, long version) {
        mDevice = device;
        return String.format(URL, new Object[] { device, version });
    }

    @Override
    public List<PackageInfo> createPackageInfoList(String buffer) throws Exception {
        mError = null;
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        if (buffer != null && !buffer.isEmpty()) {
            JSONObject updateInfo = new JSONObject(buffer);
            mError = updateInfo.optString("error");
            if (mError == null || mError.isEmpty()) {
                JSONArray updates = updateInfo.getJSONArray("updates");
                for (int i = updates.length() - 1; i >= 0; i--) {
                    JSONObject update = updates.getJSONObject(i);
                    list.add(new UpdatePackage(mDevice, update.getString("name"), update
                            .getLong("version"), update.getString("size"), update.getString("url"),
                            update.getString("md5"), false));
                }
            }
        }
        return list;
    }

    @Override
    public String getError() {
        return mError;
    }

}
