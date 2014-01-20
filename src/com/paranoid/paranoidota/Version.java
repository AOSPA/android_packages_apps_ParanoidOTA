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

import java.io.Serializable;

/**
 * Class to manage different versions in the zip name.
 *
 * Format
 * pa_A-B-C.DE-FG-H.zip
 * where
 * A = device name, required
 * B = extra information, not required (for gapps)
 * C = major, integer from 0 to n, required
 * D = minor, integer from 0 to 9, required
 * E = maintenance, integer from 0 to n, not required
 * F = phase, possible values are A, B or RC, not required, default is gold/production
 * G = phase number, integer from 0 to n, not required
 * H = date, YYYYMMDD, not required, the format can be YYYYMMDDx where x is a letter (for gapps)
 *
 * All the default values not specified above are 0
 *
 * Examples
 * pa_find5-3.99-RC2-20130923.zip
 * pa_gapps-modular-mini-4.3-20131010-signed.zip
 *
 */
public class Version implements Serializable {

    private final String[] STATIC_REMOVE = { ".zip", "pa_" };
    private final String[] PHASES = { "ALPHA", "BETA", "RELEASE CANDIDATE", "GOLD" };

    private static final String SEPARATOR = "-";

    private static final int ALPHA = 0;
    private static final int BETA = 1;
    private static final int RELEASE_CANDIDATE = 2;
    private static final int GOLD = 3;

    private String mDevice;
    private int mMajor = 0;
    private int mMinor = 0;
    private int mMaintenance = 0;
    private int mPhase = GOLD;
    private int mPhaseNumber = 0;
    private int mDate = 0;

    public Version() {
    }

    public Version(String fileName) {

        for (int i = 0; i < STATIC_REMOVE.length; i++) {
            fileName = fileName.replace(STATIC_REMOVE[i], "");
        }

        String[] split = fileName.split(SEPARATOR);

        mDevice = split[0];

        // remove gapps extra names (modular, full, mini, etc)
        while (split[1].matches ("\\w+\\.?")) {
            String[] newSplit = new String[split.length - 1];
            newSplit[0] = split[0];
            for (int i = 2; i < split.length; i++) {
                newSplit[i - 1] = split[i];
            }
            split = newSplit;
            if (split.length <= 1) {
                break;
            }
        }

        if (split.length <= 1) {
            // malformed version
            return;
        }

        String version = split[1];
        int index = -1;
        if ((index = version.indexOf(".")) > 0) {
            mMajor = Integer.parseInt(version.substring(0, index));
            version = version.substring(index + 1);
            if (version.length() > 0) {
                mMinor = Integer.parseInt(version.substring(0, 1));
            }
            if (version.length() > 1) {
                String maintenance = version.substring(1);
                if (maintenance.startsWith(".")) {
                    maintenance = maintenance.substring(1);
                }
                mMaintenance = Integer.parseInt(maintenance);
            }
        } else {
            mMajor = Integer.parseInt(version);
        }

        if (!Utils.isNumeric(split[2].substring(0, 1))) {
            version = split[2];
            if (version.startsWith("A")) {
                mPhase = ALPHA;
                if (version.startsWith("ALPHA")) {
                    version = version.substring(5);
                } else {
                    version = version.substring(1);
                }
            } else if (version.startsWith("B")) {
                mPhase = BETA;
                if (version.startsWith("BETA")) {
                    version = version.substring(4);
                } else {
                    version = version.substring(1);
                }
            } else if (version.startsWith("RC")) {
                mPhase = RELEASE_CANDIDATE;
                version = version.substring(2);
            }
            if (!version.isEmpty()) {
                mPhaseNumber = Integer.parseInt(version);
            }
            mDate = Integer.parseInt(split[3]);
        } else {
            mDate = Integer.parseInt(split[2]);
        }
    }

    public String getDevice() {
        return mDevice;
    }

    public int getMajor() {
        return mMajor;
    }

    public int getMinor() {
        return mMinor;
    }

    public int getMaintenance() {
        return mMaintenance;
    }

    public int getPhase() {
        return mPhase;
    }

    public String getPhaseName() {
        return PHASES[mPhase];
    }

    public int getPhaseNumber() {
        return mPhaseNumber;
    }

    public int getDate() {
        return mDate;
    }

    public boolean isEmpty() {
        return mMajor == 0;
    }

    public String toString() {
        return mDevice + "-" + mMajor + "." + mMinor + (mMaintenance > 0 ? mMaintenance : "")
                + (mPhase != GOLD ? "-" + mPhase + mPhaseNumber : "") + "-" + mDate;
    }

    public static Version fromGapps(String platform, long version) {
        if (version <= 0L) {
            return new Version();
        }
        return new Version("gapps-" + platform.substring(0, 1) + "."
                + (platform.length() > 1 ? platform.substring(1) : "") + "-" + version);
    }

    public static int compare(Version v1, Version v2) {
        if (v1.getMajor() != v2.getMajor()) {
            return v1.getMajor() < v2.getMajor() ? -1 : 1;
        }
        if (v1.getMinor() != v2.getMinor()) {
            return v1.getMinor() < v2.getMinor() ? -1 : 1;
        }
        if (v1.getMaintenance() != v2.getMaintenance()) {
            return v1.getMaintenance() < v2.getMaintenance() ? -1 : 1;
        }
        if (v1.getPhase() != v2.getPhase()) {
            return v1.getPhase() < v2.getPhase() ? -1 : 1;
        }
        if (v1.getPhaseNumber() != v2.getPhaseNumber()) {
            return v1.getPhaseNumber() < v2.getPhaseNumber() ? -1 : 1;
        }
        if (v1.getDate() != v2.getDate()) {
            return v1.getDate() < v2.getDate() ? -1 : 1;
        }
        return 0;
    }
}
