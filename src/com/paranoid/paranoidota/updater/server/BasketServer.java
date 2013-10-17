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

import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.updater.Server;
import com.paranoid.paranoidota.updater.UpdatePackage;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

public class BasketServer implements Server {

    private static final String URL = "http://pa.basketbuild.com/horizon.php?device=%s";

    private String mDevice = null;
    private String mError = null;
    private long mVersion = 0L;
    private boolean mIsRom;

    public BasketServer(boolean isRom) {
        mIsRom = isRom;
    }

    @Override
    public String getUrl(String device, long version) {
        mDevice = device;
        mVersion = version;
        return String.format(URL, new Object[] { device });
    }

    @Override
    public List<PackageInfo> createPackageInfoList(JSONObject response) throws Exception {
        mError = null;
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        mError = response.optString("error");
        if (mError == null || mError.isEmpty()) {
            JSONArray updates = response.getJSONArray("updates");
            for (int i = updates.length() - 1; i >= 0; i--) {
                JSONObject file = updates.getJSONObject(i);
                String filename = file.optString("name");
                String stripped = filename.replace(".zip", "");
                stripped = stripped.replace("-signed", "");
                stripped = stripped.replace("-modular", "");
                String[] parts = stripped.split("-");
                int part = parts.length - 2;
                if (parts[part].startsWith("RC")) {
                    part = parts.length - 1;
                }
                boolean isNew = parts[parts.length - 1].matches("[-+]?\\d*\\.?\\d+");
                if (!isNew) {
                    continue;
                }
                long version = Utils.parseRomVersion(filename);
                if (version > mVersion) {
                    list.add(new UpdatePackage(mDevice, filename, file
                            .getLong("version"), file.getString("size"), file.getString("url"),
                            file.getString("md5"), mIsRom));
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
