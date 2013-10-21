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

package com.paranoid.paranoidota.activities;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import com.paranoid.paranoidota.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

public class RequestFileActivity extends Activity {

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
        test.setType("file/*");
        List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                PackageManager.GET_ACTIVITIES);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setType("file/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_PICK_FILE);
        } else {
            // No app installed to handle the intent - file explorer
            // required
            Toast.makeText(this, R.string.install_file_manager_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_FILE) {
            if (data == null) {
                // Nothing returned by user, probably pressed back button in
                // file manager
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
