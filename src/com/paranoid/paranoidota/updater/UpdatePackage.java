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

import com.paranoid.paranoidota.Version;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;

public class UpdatePackage implements PackageInfo, Serializable {

    private String md5 = null;
    private String incremental_md5 = null;
    private String filename = null;
    private String incremental_filename = null;
    private String path = null;
    private String size = null;
    private String incremental_path = null;
    private Version version;
    private boolean isDelta = false;
    private boolean isGapps = false;

    public UpdatePackage(String device, String name, Version version, String size, String url,
            String md5, boolean gapps) {
        this.filename = name;
        this.version = version;
        this.size = size;
        this.path = url;
        this.md5 = md5;
        this.isGapps = gapps;
    }

    @Override
    public boolean isDelta() {
        return isDelta;
    }

    @Override
    public String getDeltaFilename() {
        return incremental_filename;
    }

    @Override
    public String getDeltaPath() {
        return incremental_path;
    }

    @Override
    public String getDeltaMd5() {
        return incremental_md5;
    }

    @Override
    public String getMd5() {
        return md5;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String getSize() {
        return size;
    }

    @Override
    public boolean isGapps() {
        return isGapps;
    }
}