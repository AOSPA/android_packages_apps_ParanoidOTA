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

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.paranoid.paranoidota.IOUtils;
import com.paranoid.paranoidota.MainActivity;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.helpers.DownloadHelper;
import com.paranoid.paranoidota.helpers.SettingsHelper;
import com.paranoid.paranoidota.updater.GappsUpdater;
import com.paranoid.paranoidota.updater.RomUpdater;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;
import com.paranoid.paranoidota.updater.Updater.UpdaterListener;

public class InstallFragment extends android.preference.PreferenceFragment {

    private Context mContext;
    private PreferenceCategory mLocalRoot;
    private PreferenceCategory mExtrasRoot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(mContext);

        mLocalRoot = new PreferenceCategory(mContext);
        mLocalRoot.setTitle(R.string.local);
        root.addPreference(mLocalRoot);

        mExtrasRoot = new PreferenceCategory(mContext);
        mExtrasRoot.setTitle(R.string.extras);
        root.addPreference(mExtrasRoot);

        setPreferenceScreen(root);

        Preference info1 = new Preference(mContext);
        info1.setTitle("pa_3.69_20130724.zip");
        info1.setSummary("Rom, 3.69 - July 22, 2013");
        info1.setIcon(R.drawable.ic_download);
        info1.setSelectable(false);
        mLocalRoot.addPreference(info1);

        Preference info2 = new Preference(mContext);
        info2.setTitle("pa_gapps_20130724_offical.zip");
        info2.setSummary("Google Apps, July 22, 2013");
        info2.setIcon(R.drawable.ic_download);
        info2.setSelectable(false);
        mLocalRoot.addPreference(info2);

        Preference info3 = new Preference(mContext);
        info3.setTitle("open_pdroid_whatever.zip");
        info3.setSummary("Addition file, MD5: 87438HJGHJF76");
        info3.setIcon(R.drawable.ic_download);
        info3.setSelectable(false);
        mExtrasRoot.addPreference(info3);

        // to do: needs an install button in the action bar plus a + button to add
        // extra-files. a way to remove those aswell, maybe long-press.

        // in the updates fragement, when a file has been updatedhe shows a pin, a
        // click opens the flash-dialog. it would be enough to just jump into the
        // install fragment if you click it, no dialog needed at all.
    }
}
