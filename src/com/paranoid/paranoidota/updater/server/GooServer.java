/*
 * Copyright 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paranoid.paranoidota.updater.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.paranoid.paranoidota.updater.Server;
import com.paranoid.paranoidota.updater.UpdatePackage;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

public class GooServer implements Server {

    private static final String URL = "http://goo.im/json2&path=/devs/paranoidandroid/roms/%s&ro_board=%s";

    private String mDevice = null;
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
        if (buffer != null && !buffer.isEmpty()) {
            JSONObject result = new JSONObject(buffer);
            JSONObject update = null;
            try {
                update = result.getJSONObject("update_info");
            } catch (JSONException ex) {
                update = result;
            }
            JSONArray updates = update.optJSONArray("list");
            for (int i = 0; updates != null && i < updates.length(); i++) {
                JSONObject file = updates.getJSONObject(i);
                String filename = file.optString("filename");
                if (filename != null && !filename.isEmpty() && filename.endsWith(".zip")) {
                    String stripped = filename.replaceAll(".1-RC1-", "-");
                    stripped = stripped.replaceAll("-RC2-", "-");
                    long version = Long.parseLong(stripped.replaceAll("\\D+", ""));
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
        return null;
    }

}
