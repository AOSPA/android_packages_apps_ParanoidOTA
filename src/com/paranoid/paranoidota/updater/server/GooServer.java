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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.updater.Server;
import com.paranoid.paranoidota.updater.UpdatePackage;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

public class GooServer implements Server {

    private static final String URL = "http://goo.im/json2&path=/devs/paranoidandroid/roms/%s&ro_board=%s";

    private String mDevice = null;
    private String mError = null;
    private long mVersion = 0L;

    @Override
    public String getUrl(String device, long version) {
        mDevice = device;
        mVersion = version;
        return String.format(URL, new Object[] { device, device });
    }

    @Override
    public List<PackageInfo> createPackageInfoList(String buffer) throws Exception {
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        mError = null;
        if (buffer != null && !buffer.isEmpty()) {
            JSONObject result = new JSONObject(buffer);
            JSONObject update = null;
            try {
                update = result.getJSONObject("update_info");
            } catch (JSONException ex) {
                update = result;
            }
            JSONArray updates = update.optJSONArray("list");
            if (updates == null) {
                mError = "Device not found";
            }
            for (int i = 0; updates != null && i < updates.length(); i++) {
                JSONObject file = updates.getJSONObject(i);
                String filename = file.optString("filename");
                if (filename != null && !filename.isEmpty() && filename.endsWith(".zip")) {
                    String stripped = filename.replace(".zip", "");
                    stripped = stripped.replace("-signed", "");
                    String[] parts = stripped.split("-");
                    boolean isNew = parts.length < 2 ? true : parts[parts.length - 2]
                            .matches("[-+]?\\d*\\.?\\d+");
                    if (!isNew) {
                        continue;
                    }
                    long version = Utils.parseRomVersion(filename);
                    if (version > mVersion) {
                        list.add(new UpdatePackage(mDevice, filename, version, "0", "http://goo.im"
                                + file.getString("path"), file.getString("md5"), false));
                    }
                }
            }
        }
        Collections.sort(list, new Comparator<PackageInfo>() {

            @Override
            public int compare(PackageInfo lhs, PackageInfo rhs) {
                long v1 = lhs.getVersion();
                long v2 = rhs.getVersion();
                return v1 < v2 ? 1 : -1;
            }

        });
        return list;
    }

    @Override
    public String getError() {
        return mError;
    }

}
