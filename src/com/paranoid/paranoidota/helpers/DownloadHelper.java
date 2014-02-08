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

package com.paranoid.paranoidota.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.R;

public class DownloadHelper {

    private static Context sContext;
    private static SettingsHelper sSettingsHelper;
    private static Handler sUpdateHandler = new Handler();

    private static DownloadManager sDownloadManager;
    private static DownloadCallback sCallback;

    private static boolean sDownloadingRom = false;
    private static boolean sDownloadingGapps = false;

    public static interface DownloadCallback {

        public abstract void onDownloadStarted();

        public abstract void onDownloadProgress(int progress);

        public abstract void onDownloadFinished(Uri uri, String md5, boolean isRom);

        public abstract void onDownloadError(String reason);
    }

    private static Runnable sUpdateProgress = new Runnable() {

        public void run() {
            if (!sDownloadingRom && !sDownloadingGapps) {
                return;
            }

            long idRom = sSettingsHelper.getDownloadRomId();
            long idGapps = sSettingsHelper.getDownloadGappsId();

            long[] statusRom = sDownloadingRom ? getDownloadProgress(idRom, true) : new long[] {
                    DownloadManager.STATUS_SUCCESSFUL,
                    0,
                    0,
                    -1 };
            long[] statusGapps = sDownloadingGapps ? getDownloadProgress(idGapps, false)
                    : new long[] { DownloadManager.STATUS_SUCCESSFUL, 0, 0, -1 };

            int status = DownloadManager.STATUS_SUCCESSFUL;
            if (statusRom[0] == DownloadManager.STATUS_FAILED
                    && statusGapps[0] == DownloadManager.STATUS_FAILED) {
                status = DownloadManager.STATUS_FAILED;
            } else if (statusRom[0] == DownloadManager.STATUS_PENDING
                    && statusGapps[0] == DownloadManager.STATUS_PENDING) {
                status = DownloadManager.STATUS_PENDING;
            }

            switch (status) {
                case DownloadManager.STATUS_PENDING:
                    sCallback.onDownloadProgress(-1);
                    break;
                case DownloadManager.STATUS_FAILED:
                    int error = (int) statusRom[3];
                    if (error == -1) {
                        error = (int) statusGapps[3];
                    }
                    sCallback.onDownloadError(error == -1 ? null : sContext.getResources()
                            .getString(error));
                    break;
                default:
                    long totalBytes = statusRom[1] + statusGapps[1];
                    long downloadedBytes = statusRom[2] + statusGapps[2];
                    long percent = totalBytes == -1 && downloadedBytes == -1 ? -1 : downloadedBytes
                            * 100 / totalBytes;
                    if (totalBytes != -1 && downloadedBytes != -1 && percent != -1) {
                        sCallback.onDownloadProgress((int) percent);
                    }
                    break;
            }

            if (status != DownloadManager.STATUS_FAILED) {
                sUpdateHandler.postDelayed(this, 1000);
            }
        }
    };

    public static void init(Context context, DownloadCallback callback) {
        sContext = context;
        if (sDownloadManager == null) {
            sDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        sSettingsHelper = new SettingsHelper(sContext);
        registerCallback(callback);
        checkIfDownloading();
    }

    public static void registerCallback(DownloadCallback callback) {
        sCallback = callback;
        sUpdateHandler.post(sUpdateProgress);
    }

    private static void readdCallback() {
        sUpdateHandler.post(sUpdateProgress);
    }

    public static void unregisterCallback() {
        sUpdateHandler.removeCallbacks(sUpdateProgress);
    }

    public static void checkDownloadFinished(Context context, long downloadId) {
        sContext = context;
        if (sDownloadManager == null) {
            sDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        sSettingsHelper = new SettingsHelper(sContext);
        checkDownloadFinished(downloadId, true, true);
        checkDownloadFinished(downloadId, false, true);
    }

    public static void clearDownloads() {
        long id = sSettingsHelper.getDownloadRomId();
        checkDownloadFinished(id, true, false);
        id = sSettingsHelper.getDownloadGappsId();
        checkDownloadFinished(id, false, false);
    }

    private static void checkDownloadFinished(long downloadId, boolean isRom,
            boolean installIfFinished) {
        long id = isRom ? sSettingsHelper.getDownloadRomId() : sSettingsHelper.getDownloadGappsId();
        if (id == -1L || (downloadId != 0 && downloadId != id)) {
            return;
        }
        String md5 = isRom ? sSettingsHelper.getDownloadRomMd5() : sSettingsHelper
                .getDownloadGappsMd5();
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = sDownloadManager.query(query);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    removeDownload(id, isRom, true);
                    int reasonText = getDownloadError(cursor);
                    sCallback.onDownloadError(sContext.getResources().getString(reasonText));
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    if (installIfFinished) {
                        String uriString = cursor.getString(cursor
                                .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        sCallback.onDownloadFinished(Uri.parse(uriString), md5, isRom);
                    }
                    downloadSuccesful(id, isRom);
                    break;
                default:
                    cancelDownload(id, isRom);
                    break;
            }
        } else {
            removeDownload(id, isRom, true);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public static boolean isDownloading(boolean rom) {
        return rom ? sDownloadingRom : sDownloadingGapps;
    }

    public static boolean isDownloading(boolean rom, String fileName) {
        if (sDownloadingRom) {
            String downloadName = sSettingsHelper.getDownloadRomName();
            return fileName.equals(downloadName);
        }
        if (sDownloadingGapps) {
            String downloadName = sSettingsHelper.getDownloadGappsName();
            return fileName.equals(downloadName);
        }
        return false;
    }

    public static void downloadFile(final String url, final String fileName, final String md5,
            final boolean isRom) {

        sUpdateHandler.post(sUpdateProgress);
        sCallback.onDownloadStarted();

        if (url.contains("goo.im")) {

            if (sSettingsHelper.isLogged()) {

                String login = sSettingsHelper.getLogin();
                String lurl = url + "&hash=" + login;
                realDownloadFile(lurl, fileName, md5, isRom);

            } else {

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        InputStream is = null;
                        try {
                            URL getUrl = new URL(url);
                            URLConnection conn = getUrl.openConnection();
                            conn.connect();
                            is = new BufferedInputStream(conn.getInputStream());
                            byte[] buf = new byte[4096];
                            while (is.read(buf) != -1) {
                            }
                            try {
                                Thread.sleep(10500);
                            } catch (InterruptedException e) {
                            }
                            realDownloadFile(url, fileName, md5, isRom);
                            readdCallback();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (Exception e) {
                                }
                            }
                        }
                        return null;
                    }

                }.execute((Void)null);
            }

        } else {
            realDownloadFile(url, fileName, md5, isRom);
        }

    }

    private static void realDownloadFile(String url, String fileName, String md5, boolean isRom) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(sContext.getResources().getString(R.string.download_title,
                new Object[] { fileName }));
        File file = new File(IOUtils.DOWNLOAD_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        request.setDestinationUri(Uri.fromFile(new File(IOUtils.DOWNLOAD_PATH, fileName)));

        long id = sDownloadManager.enqueue(request);
        if (isRom) {
            sDownloadingRom = true;
            sSettingsHelper.setDownloadRomId(id, md5, fileName);
        } else {
            sDownloadingGapps = true;
            sSettingsHelper.setDownloadGappsId(id, md5, fileName);
        }
    }

    private static void removeDownload(long id, boolean isRom, boolean removeDownload) {
        if (isRom) {
            sDownloadingRom = false;
            sSettingsHelper.setDownloadRomId(null, null, null);
        } else {
            sDownloadingGapps = false;
            sSettingsHelper.setDownloadGappsId(null, null, null);
        }
        if (removeDownload) {
            sDownloadManager.remove(id);
        }
        sUpdateHandler.removeCallbacks(sUpdateProgress);
        sCallback.onDownloadFinished(null, null, isRom);
    }

    private static void downloadSuccesful(long id, boolean isRom) {
        if (isRom) {
            sDownloadingRom = false;
            sSettingsHelper.setDownloadRomId(null, null, null);
        } else {
            sDownloadingGapps = false;
            sSettingsHelper.setDownloadGappsId(null, null, null);
        }
        sUpdateHandler.removeCallbacks(sUpdateProgress);
    }

    private static void cancelDownload(final long id, final boolean isRom) {
        new AlertDialog.Builder(sContext)
                .setTitle(R.string.cancel_download_alert_title)
                .setMessage(
                        isRom ? R.string.cancel_rom_download_alert_summary
                                : R.string.cancel_gapps_download_alert_summary)
                .setPositiveButton(R.string.cancel_download_alert_yes,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                removeDownload(id, isRom, true);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancel_download_alert_no,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    private static long[] getDownloadProgress(long id, boolean isRom) {
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(id);

        Cursor cursor = sDownloadManager.query(q);
        int status;

        if (cursor == null || !cursor.moveToFirst()) {
            status = DownloadManager.STATUS_FAILED;
        } else {
            status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        }

        long error = -1;
        long totalBytes = -1;
        long downloadedBytes = -1;

        switch (status) {
            case DownloadManager.STATUS_PAUSED:
            case DownloadManager.STATUS_RUNNING:
                downloadedBytes = cursor.getLong(cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                totalBytes = cursor.getLong(cursor
                        .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                break;
            case DownloadManager.STATUS_FAILED:
                if (isRom) {
                    sDownloadingRom = false;
                } else {
                    sDownloadingGapps = false;
                }
                error = getDownloadError(cursor);
                break;
        }

        if (cursor != null) {
            cursor.close();
        }

        return new long[] { status, totalBytes, downloadedBytes, error };
    }

    private static void checkIfDownloading() {

        long romId = sSettingsHelper.getDownloadRomId();
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(romId);
        Cursor cursor = sDownloadManager.query(query);
        sDownloadingRom = cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        if (romId >= 0L && !sDownloadingRom) {
            removeDownload(romId, true, false);
        }

        long gappsId = sSettingsHelper.getDownloadGappsId();
        query = new DownloadManager.Query();
        query.setFilterById(gappsId);
        cursor = sDownloadManager.query(query);
        sDownloadingGapps = cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        if (gappsId >= 0L && !sDownloadingGapps) {
            removeDownload(romId, false, false);
        }
    }

    private static int getDownloadError(Cursor cursor) {
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);
        int reasonText = -1;
        switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME:
                reasonText = R.string.error_cannot_resume;
                break;
            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                reasonText = R.string.error_device_not_found;
                break;
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                reasonText = R.string.error_file_already_exists;
                break;
            case DownloadManager.ERROR_FILE_ERROR:
                reasonText = R.string.error_file_error;
                break;
            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                reasonText = R.string.error_http_data_error;
                break;
            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                reasonText = R.string.error_insufficient_space;
                break;
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                reasonText = R.string.error_too_many_redirects;
                break;
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                reasonText = R.string.error_unhandled_http_code;
                break;
            case DownloadManager.ERROR_UNKNOWN:
            default:
                reasonText = R.string.error_unknown;
                break;
        }
        return reasonText;
    }
}
