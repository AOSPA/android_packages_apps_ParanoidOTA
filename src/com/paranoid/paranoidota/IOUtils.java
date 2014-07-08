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

package com.paranoid.paranoidota;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class IOUtils {

    public static final String SDCARD = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    public static final String DOWNLOAD_PATH = new File(Environment
            .getExternalStorageDirectory(), "paranoidota/").getAbsolutePath();

    private static final String PREFIX = "pa_";
    private static final String SUFFIX = ".zip";

    private static Properties sDictionary;
    private static String sPrimarySdcard;
    private static String sSecondarySdcard;
    private static boolean sSdcardsChecked;

    public static void init(Context context) {
        File downloads = new File(DOWNLOAD_PATH);
        downloads.mkdirs();

        readMounts(context);
    }

    public static String[] getDownloadList(Context context) {
        File downloads = initSettingsHelper(context);
        ArrayList<String> list = new ArrayList<String>();
        try {
            for (File f : downloads.listFiles()) {
                if (isRom(f.getName())) {
                    list.add(f.getName());
                }
            }
        } catch (NullPointerException e) {
            // blah
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] getDownloadSizes(Context context) {
        File downloads = initSettingsHelper(context);
        ArrayList<String> list = new ArrayList<String>();
        for (File f : downloads.listFiles()) {
            if (isRom(f.getName())) {
                list.add(humanReadableByteCount(f.length(), false));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static String getDownloadSize(Context context, String fileName) {
        File downloads = initSettingsHelper(context);
        for (String file : getDownloadList(context)) {
            if (fileName.equals(file)) {
                File f = new File(downloads, fileName);
                return humanReadableByteCount(f.length(), false);
            }
        }
        return "0";
    }

    public static boolean isOnDownloadList(Context context, String fileName) {
        for (String file : getDownloadList(context)) {
            if (fileName.equals(file))
                return true;
        }
        return false;
    }

    public static boolean isRom(String name) {
        return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isInSecondaryStorage(String path) {
        return !path.startsWith(sPrimarySdcard) && !path.startsWith("/sdcard")
                && !path.startsWith("/mnt/sdcard");
    }

    public static boolean hasSecondarySdCard() {
        return sSecondarySdcard != null;
    }

    public static String getPrimarySdCard() {
        return sPrimarySdcard;
    }

    public static String getSecondarySdCard() {
        return sSecondarySdcard;
    }

    private static void readMounts(Context context) {
        if (sSdcardsChecked) {
            return;
        }

        ArrayList<String> mounts = new ArrayList<String>();
        ArrayList<String> vold = new ArrayList<String>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("/proc/mounts"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("/dev/block/vold/")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[1];

                    mounts.add(element);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        boolean addExternal = mounts.size() == 1 && isExternalStorageAvailable();
        if (mounts.size() == 0 && addExternal) {
            mounts.add("/mnt/sdcard");
        }
        File fstab = findFstab();
        scanner = null;
        if (fstab != null) {
            try {

                scanner = new Scanner(fstab);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }

                        if (element.toLowerCase().indexOf("usb") < 0) {
                            vold.add(element);
                        }
                    } else if (line.startsWith("/devices/platform")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }

                        if (element.toLowerCase().indexOf("usb") < 0) {
                            vold.add(element);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }
        if (addExternal && (vold.size() == 1 && isExternalStorageAvailable())) {
            mounts.add(vold.get(0));
        }
        if (vold.size() == 0 && isExternalStorageAvailable()) {
            vold.add("/mnt/sdcard");
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            File root = new File(mount);
            if (!vold.contains(mount)
                    || (!root.exists() || !root.isDirectory() || !root.canWrite())) {
                mounts.remove(i--);
            }
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            if (mount.indexOf("sdcard0") < 0 && !mount.equalsIgnoreCase("/mnt/sdcard")
                    && !mount.equalsIgnoreCase("/sdcard")) {
                sSecondarySdcard = mount;
            } else {
                sPrimarySdcard = mount;
            }
        }

        if (sPrimarySdcard == null) {
            sPrimarySdcard = "/sdcard";
        }

        sSdcardsChecked = true;
    }

    private static File findFstab() {
        File file = null;

        file = new File("/system/etc/vold.fstab");
        if (file.exists()) {
            return file;
        }

        String fstab = Utils
                .exec("grep -ls \"/dev/block/\" * --include=fstab.* --exclude=fstab.goldfish");
        if (fstab != null) {
            String[] files = fstab.split("\n");
            for (int i = 0; i < files.length; i++) {
                file = new File(files[i]);
                if (file.exists()) {
                    return file;
                }
            }
        }

        return null;
    }

    public static double getSpaceLeft() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocksLong()
                * (double) stat.getBlockSizeLong();
        // One binary gigabyte equals 1,073,741,824 bytes.
        return sdAvailSize / 1073741824;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMG" : "KMG").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre).replace(",", ".");
    }

    public static String md5(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    public static Properties getDictionary(Context context) {
        if (sDictionary == null) {
            sDictionary = new Properties();
            try {
                sDictionary.load(context.getAssets().open("dictionary.properties"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sDictionary;
    }

    private static File initSettingsHelper(Context context) {
        File downloads = new File(DOWNLOAD_PATH);
        downloads.mkdirs();
        return downloads;
    }

    public static boolean hasAndroidSecure() {
        return folderExists(SDCARD + "/.android-secure");
    }

    public static boolean hasSdExt() {
        return folderExists("/sd-ext");
    }

    public static boolean folderExists(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}
