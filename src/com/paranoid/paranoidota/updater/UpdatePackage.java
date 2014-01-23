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

package com.paranoid.paranoidota.updater;

import java.io.Serializable;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.Version;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

public class UpdatePackage implements PackageInfo, Serializable {

    private String mMd5 = null;
    private String mIncrementalMd5 = null;
    private String mFilename = null;
    private String mIncrementalFilename = null;
    private String mPath = null;
    private String mHost = null;
    private String mSize = null;
    private String mIncrementalPath = null;
    private Version mVersion;
    private boolean mIsDelta = false;
    private boolean mIsGapps = false;

    public UpdatePackage(String device, String name, Version version, long size, String url,
            String md5, boolean gapps) {
        this(device, name, version,
                IOUtils.humanReadableByteCount(size, false), url, md5, gapps);
    }

    public UpdatePackage(String device, String name, Version version, String size, String url,
            String md5, boolean gapps) {
        this.mFilename = name;
        this.mVersion = version;
        this.mSize = size;
        this.mPath = url;
        this.mMd5 = md5;
        this.mIsGapps = gapps;
        mHost = mPath.replace("http://", "");
        mHost = mHost.replace("https://", "");
        mHost = mHost.substring(0, mHost.indexOf("/"));
    }

    @Override
    public boolean isDelta() {
        return mIsDelta;
    }

    @Override
    public String getDeltaFilename() {
        return mIncrementalFilename;
    }

    @Override
    public String getDeltaPath() {
        return mIncrementalPath;
    }

    @Override
    public String getDeltaMd5() {
        return mIncrementalMd5;
    }

    @Override
    public String getMd5() {
        return mMd5;
    }

    @Override
    public String getFilename() {
        return mFilename;
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public String getHost() {
        return mHost;
    }

    @Override
    public Version getVersion() {
        return mVersion;
    }

    @Override
    public String getSize() {
        return mSize;
    }

    @Override
    public boolean isGapps() {
        return mIsGapps;
    }
}