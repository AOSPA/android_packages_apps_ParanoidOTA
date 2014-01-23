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

package com.paranoid.paranoidota.cards;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paranoid.paranoidota.MainActivity;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.helpers.DownloadHelper;
import com.paranoid.paranoidota.helpers.DownloadHelper.DownloadCallback;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;
import com.paranoid.paranoidota.widget.Card;
import com.paranoid.paranoidota.widget.Item;
import com.paranoid.paranoidota.widget.Item.OnItemClickListener;

public class DownloadCard extends Card implements DownloadCallback {

    private MainActivity mActivity;
    private ProgressBar mWaitProgressBar;
    private ProgressBar mProgressBar;
    private TextView mProgress;
    private Item mCancel;

    public DownloadCard(Context context, AttributeSet attrs, PackageInfo[] infos) {
        super(context, attrs);

        mActivity = (MainActivity) context;
        mActivity.setDownloadCallback(this);

        setTitle(R.string.download_card_title);
        setLayoutId(R.layout.card_download);

        mWaitProgressBar = (ProgressBar) findLayoutViewById(R.id.wait_progressbar);
        mProgressBar = (ProgressBar) findLayoutViewById(R.id.progressbar);
        mProgress = (TextView) findLayoutViewById(R.id.progress);
        mCancel = (Item) findLayoutViewById(R.id.cancel);

        mWaitProgressBar.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);

        mCancel.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onClick(int id) {
                DownloadHelper.clearDownloads();
            }

        });

        String names = "";
        for (int i = 0; i < infos.length; i++) {
            boolean isRom = !infos[i].isGapps();
            names += infos[i].getFilename() + "\n";
            if (DownloadHelper.isDownloading(isRom)) {
                int resId = isRom ? R.string.already_downloading_rom
                        : R.string.already_downloading_gapps;
                Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
                ((MainActivity) context).setState(MainActivity.STATE_UPDATES,
                        true, null, null, null, false);
                return;
            }
        }

        TextView infoView = (TextView) findViewById(R.id.info);
        infoView.setText(context.getResources().getString(R.string.downloading_info, names));

        for (int i = 0; i < infos.length; i++) {
            DownloadHelper.registerCallback(mActivity);
            DownloadHelper.downloadFile(infos[i].getPath(),
                    infos[i].getFilename(), infos[i].getMd5(),
                    !infos[i].isGapps());
        }
    }

    @Override
    protected boolean canExpand() {
        return false;
    }

    @Override
    public void onDownloadStarted() {
        onDownloadProgress(-1);
    }

    @Override
    public void onDownloadError(String reason) {
        mWaitProgressBar.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadProgress(int progress) {
        if (progress < 0) {
            mWaitProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
        } else if (progress > 100) {
            mWaitProgressBar.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
        } else {
            mWaitProgressBar.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(progress);
            mProgress.setText(progress + "%");
        }
    }

    @Override
    public void onDownloadFinished(Uri uri, final String md5, boolean isRom) {
        mWaitProgressBar.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }

}
