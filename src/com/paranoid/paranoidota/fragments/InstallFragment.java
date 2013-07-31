/*
 * Copyright (C) 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use mContext file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paranoid.paranoidota.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.activities.RequestFileActivity;
import com.paranoid.paranoidota.activities.RequestFileActivity.RequestFileCallback;

public class InstallFragment extends android.preference.PreferenceFragment
        implements RequestFileCallback {

    private static List<File> sFiles = new ArrayList<File>();

    public static void clearFiles() {
        sFiles.clear();
    }

    public static void addFile(File file) {
        if (sFiles.indexOf(file) >= 0) {
            sFiles.remove(file);
        }
        sFiles.add(file);
    }

    public static String[] getFiles() {
        List<String> files = new ArrayList<String>();
        for (File file : sFiles) {
            files.add(file.getAbsolutePath());
        }
        return files.toArray(new String[files.size()]);
    }

    private Context mContext;
    private OnPreferenceClickListener mListener;
    private PreferenceCategory mLocalRoot;
    private PreferenceCategory mExtrasRoot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mListener = new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                showRemoveDialog(preference);
                return false;
            }
        };

        RequestFileActivity.setRequestFileCallback(this);

        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(mContext);

        mLocalRoot = new PreferenceCategory(mContext);
        mLocalRoot.setTitle(R.string.local);
        root.addPreference(mLocalRoot);

        mExtrasRoot = new PreferenceCategory(mContext);
        mExtrasRoot.setTitle(R.string.extras);
        root.addPreference(mExtrasRoot);

        setPreferenceScreen(root);

        update();
    }

    @Override
    public void fileRequested(String filePath) {
        addFile(new File(filePath));
        update();
    }

    private void update() {
        mLocalRoot.removeAll();
        mExtrasRoot.removeAll();
        for (File file : sFiles) {
            Preference pref = new Preference(mContext);
            pref.setTitle(file.getName());
            pref.setSummary(getSummary(file, true));
            pref.setIcon(R.drawable.ic_download);
            pref.getExtras().putString("filePath", file.getAbsolutePath());
            pref.setOnPreferenceClickListener(mListener);
            if (IOUtils.isOnDownloadList(mContext, file.getName())) {
                mLocalRoot.addPreference(pref);
            } else {
                mExtrasRoot.addPreference(pref);
            }
        }

        if (mLocalRoot.getPreferenceCount() == 0) {
            Preference empty = new Preference(mContext);
            empty.setTitle(R.string.update_no_local_files);
            empty.setSummary(R.string.update_no_local_files_summary);
            empty.setIcon(R.drawable.ic_info);
            empty.setSelectable(false);
            mLocalRoot.addPreference(empty);
        }

        if (mExtrasRoot.getPreferenceCount() == 0) {
            Preference empty = new Preference(mContext);
            empty.setTitle(R.string.update_no_extra_files);
            empty.setSummary(R.string.update_no_extra_files_summary);
            empty.setIcon(R.drawable.ic_info);
            empty.setSelectable(false);
            mExtrasRoot.addPreference(empty);
        }
    }

    private String getSummary(File file, boolean isDownloaded) {
        if (isDownloaded) {
            String name = file.getName();
            name = name.replace("-full", "").replace("-signed", "");
            return Utils.getReadableVersion(name);
        } else {
            String path = file.getAbsolutePath();
            return path.substring(0, path.lastIndexOf("/"));
        }
    }

    private void showRemoveDialog(final Preference preference) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(R.string.remove_file_title);
        alert.setMessage(R.string.remove_file_summary);
        alert.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                File file = new File(preference.getExtras().getString("filePath"));
                sFiles.remove(file);
                update();
            }
        });
        alert.show();
    }
}
