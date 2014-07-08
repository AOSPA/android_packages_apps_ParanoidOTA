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

package com.paranoid.paranoidota.updater.server;

import android.content.Context;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.Version;
import com.paranoid.paranoidota.updater.Server;
import com.paranoid.paranoidota.updater.UpdatePackage;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GooServer implements Server {

    private static final String URL = "http://goo.im/json2&path=/devs/paranoidandroid/roms/%s&ro_board=%s";
    private static final String GAPPS_RESERVED_WORDS = "-signed|-modular|-full|-mini|-micro|-stock";

    private Context mContext;
    private String mDevice = null;
    private String mError = null;
    private Version mVersion;
    private boolean mIsRom;

    public GooServer(Context context, boolean isRom) {
        mContext = context;
        mIsRom = isRom;
    }

    @Override
    public String getUrl(String device, Version version) {
        mDevice = device;
        mVersion = version;
        return String.format(URL, new Object[] {
                device, device
        });
    }

    @Override
    public List<PackageInfo> createPackageInfoList(JSONObject response) throws Exception {
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        mError = null;
        JSONObject update = null;
        try {
            update = response.getJSONObject("update_info");
        } catch (JSONException ex) {
            update = response;
        }
        JSONArray updates = update.optJSONArray("list");
        if (updates == null) {
            mError = mContext.getResources().getString(R.string.error_device_not_found_server);
        }
        for (int i = 0; updates != null && i < updates.length(); i++) {
            JSONObject file = updates.getJSONObject(i);
            String filename = file.optString("filename");
            if (filename != null && !filename.isEmpty() && filename.endsWith(".zip")) {
                String stripped = filename.replace(".zip", "");
                if (!mIsRom) {
                    stripped = stripped.replaceAll("\\b(" + GAPPS_RESERVED_WORDS + ")\\b", "");
                }
                String[] parts = stripped.split("-");
                boolean isNew = parts[parts.length - 1].matches("[-+]?\\d*\\.?\\d+");
                if (!isNew) {
                    if (!mIsRom) {
                        String part = parts[parts.length - 1];
                        isNew = Utils.isNumeric(part)
                                || Utils.isNumeric(part.substring(0,
                                        part.length() - 1));
                        if (!isNew) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                Version version = new Version(filename);
                if (Version.compare(mVersion, version) < 0) {
                    list.add(new UpdatePackage(mDevice, filename, version, file
                            .getLong("filesize"), "http://goo.im"
                            + file.getString("path"), file.getString("md5"),
                            !mIsRom));
                }
            }
        }
        Collections.sort(list, new Comparator<PackageInfo>() {

            @Override
            public int compare(PackageInfo lhs, PackageInfo rhs) {
                return Version.compare(lhs.getVersion(), rhs.getVersion());
            }

        });
        Collections.reverse(list);
        return list;
    }

    @Override
    public String getError() {
        return mError;
    }

}
