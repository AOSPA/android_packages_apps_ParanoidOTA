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

package com.paranoid.paranoidota.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.R;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class RequestFileActivity extends Activity {

    private static final String ROOT_ID_PRIMARY_EMULATED = "primary";
    private static final int REQUEST_PICK_FILE = 203;

    public interface RequestFileCallback extends Serializable {

        public void fileRequested(String filePath);
    }

    private static RequestFileCallback sCallback;

    public static void setRequestFileCallback(RequestFileCallback callback) {
        sCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager packageManager = getPackageManager();
        Intent test = new Intent(Intent.ACTION_GET_CONTENT);
        test.setType("application/zip*");
        List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                PackageManager.GET_ACTIVITIES);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setType("application/zip");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_PICK_FILE);
        } else {
            Toast.makeText(this, R.string.file_manager_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_FILE) {
            if (data == null) {
                finish();
                return;
            }

            Uri uri = data.getData();

            String filePath = uri.getPath();

            if (!(new File(filePath)).exists()) {
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(uri, null, null, null, null);
                try {
                    if (cursor.moveToNext()) {
                        int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                        if (index >= 0) {
                            filePath = cursor.getString(index);
                        } else if (Build.VERSION.SDK_INT >= 19
                                && uri.toString().startsWith(ContentResolver.SCHEME_CONTENT)) {
                            String newUri = new Uri.Builder()
                                    .scheme(ContentResolver.SCHEME_CONTENT)
                                    .authority(uri.getAuthority()).appendPath("document")
                                    .build().toString();
                            String path = uri.toString();
                            index = filePath.indexOf(":");
                            if (path.startsWith(newUri) && index >= 0) {
                                String firstPath = filePath.substring(0, index);
                                filePath = filePath.substring(index + 1);
                                String storage = IOUtils.getPrimarySdCard();
                                if (!firstPath.contains(ROOT_ID_PRIMARY_EMULATED)) {
                                    storage = IOUtils.getSecondarySdCard();
                                }
                                filePath = storage + "/" + filePath;
                            } else {
                                filePath = null;
                            }

                        }
                    }
                } finally {
                    cursor.close();
                }
            }

            if (sCallback != null) {
                sCallback.fileRequested(filePath);
            }

        }
        finish();
    }
}
