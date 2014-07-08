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
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.paranoid.paranoidota.MainActivity;
import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.Utils;
import com.paranoid.paranoidota.updater.GappsUpdater;
import com.paranoid.paranoidota.updater.RomUpdater;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;
import com.paranoid.paranoidota.updater.Updater.UpdaterListener;
import com.paranoid.paranoidota.widget.Card;
import com.paranoid.paranoidota.widget.Item;
import com.paranoid.paranoidota.widget.Item.OnItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdatesCard extends Card implements UpdaterListener, OnCheckedChangeListener {

    private static final String ROMS = "ROMS";
    private static final String GAPPS = "GAPPS";

    private RomUpdater mRomUpdater;
    private GappsUpdater mGappsUpdater;
    private LinearLayout mLayout;
    private TextView mInfo;
    private TextView mError;
    private LinearLayout mAdditional;
    private TextView mAdditionalText;
    private Item mCheck;
    private Item mDownload;
    private ProgressBar mWaitProgressBar;
    private String mErrorRom;
    private String mErrorGapps;
    private int mNumChecked = 0;

    public UpdatesCard(Context context, AttributeSet attrs, RomUpdater romUpdater,
            GappsUpdater gappsUpdater, Bundle savedInstanceState) {
        super(context, attrs, savedInstanceState);

        mRomUpdater = romUpdater;
        mRomUpdater.addUpdaterListener(this);
        mGappsUpdater = gappsUpdater;
        mGappsUpdater.addUpdaterListener(this);

        if (savedInstanceState != null) {
            List<PackageInfo> mRoms = (List) savedInstanceState.getSerializable(ROMS);
            List<PackageInfo> mGapps = (List) savedInstanceState.getSerializable(GAPPS);

            mRomUpdater.setLastUpdates(mRoms.toArray(new PackageInfo[mRoms.size()]));
            mGappsUpdater.setLastUpdates(mGapps.toArray(new PackageInfo[mGapps.size()]));
        }

        setLayoutId(R.layout.card_updates);

        mLayout = (LinearLayout) findLayoutViewById(R.id.layout);
        mInfo = (TextView) findLayoutViewById(R.id.info);
        mError = (TextView) findLayoutViewById(R.id.error);
        mCheck = (Item) findLayoutViewById(R.id.check);
        mDownload = (Item) findLayoutViewById(R.id.download);
        mWaitProgressBar = (ProgressBar) findLayoutViewById(R.id.wait_progressbar);

        mAdditional = (LinearLayout) findLayoutViewById(R.id.additional);
        mAdditionalText = (TextView) findLayoutViewById(R.id.additional_text);

        mCheck.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onClick(int id) {
                MainActivity activity = (MainActivity) getContext();
                activity.checkUpdates();
            }

        });

        mDownload.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onClick(int id) {
                MainActivity activity = (MainActivity) getContext();
                activity.setState(MainActivity.STATE_DOWNLOAD, true, getPackages(), null, null,
                        false, false);
            }

        });

        mErrorRom = null;
        mErrorGapps = null;

        if (isExpanded()) {
            mAdditional.setVisibility(View.VISIBLE);
        }

        updateText();
    }

    @Override
    public void expand() {
        if ((mRomUpdater != null && mRomUpdater.isScanning())
                || (mGappsUpdater != null && mGappsUpdater.isScanning())) {
            return;
        }
        super.expand();
        if (mAdditional != null) {
            mAdditional.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void collapse() {
        super.collapse();
        mAdditional.setVisibility(View.GONE);
    }

    @Override
    public void saveState(Bundle outState) {
        super.saveState(outState);
        ArrayList<PackageInfo> mRoms = new ArrayList<PackageInfo>();
        ArrayList<PackageInfo> mGapps = new ArrayList<PackageInfo>();

        mRoms.addAll(Arrays.asList(mRomUpdater.getLastUpdates()));
        mGapps.addAll(Arrays.asList(mGappsUpdater.getLastUpdates()));

        outState.putSerializable(ROMS, mRoms);
        outState.putSerializable(GAPPS, mGapps);
    }

    private void updateText() {

        mLayout.removeAllViews();

        mNumChecked = 0;
        mDownload.setEnabled(false);
        mCheck.setEnabled(!mRomUpdater.isScanning() && !mGappsUpdater.isScanning());

        for (int i = mAdditional.getChildCount() - 1; i >= 0; i--) {
            if (mAdditional.getChildAt(i) instanceof TextView) {
                mAdditional.removeViewAt(i);
            }
        }

        Context context = getContext();
        Resources res = context.getResources();

        if (mRomUpdater.isScanning() || mGappsUpdater.isScanning()) {
            if (!mLayout.equals(mWaitProgressBar.getParent())) {
                mLayout.addView(mWaitProgressBar);
            }
            setTitle(R.string.updates_checking);
            mAdditional.addView(mAdditionalText);
        } else {
            mLayout.addView(mInfo);
            PackageInfo[] roms = mRomUpdater.getLastUpdates();
            PackageInfo[] gapps = mGappsUpdater.getLastUpdates();
            if ((roms == null || roms.length == 0) && (gapps == null || gapps.length == 0)) {
                setTitle(R.string.updates_uptodate);
                mInfo.setText(R.string.no_updates_found);
                mAdditional.addView(mAdditionalText);
            } else {
                setTitle(R.string.updates_found);
                mInfo.setText(res.getString(R.string.system_update));
                addPackages(roms);
                addPackages(gapps);
            }
            Utils.setRobotoThin(context, mLayout);
        }
        String error = mErrorRom;
        if (mErrorGapps != null) {
            if (error != null) {
                error += "\n" + mErrorGapps;
            } else {
                error = mErrorGapps;
            }
        }
        if (error != null) {
            mError.setText(error);
            mLayout.addView(mError);
        }
    }

    @Override
    public void startChecking(boolean isRom) {
        if (isRom) {
            mErrorRom = null;
        } else {
            mErrorGapps = null;
        }
        collapse();
        updateText();
    }

    @Override
    public void versionFound(PackageInfo[] info, boolean isRom) {
        updateText();
    }

    @Override
    public void checkError(String cause, boolean isRom) {
        if (isRom) {
            mErrorRom = cause;
        } else {
            mErrorGapps = cause;
        }
        updateText();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mNumChecked++;
        } else {
            mNumChecked--;
        }
        mDownload.setEnabled(mNumChecked > 0);
        if (!isChecked) {
            return;
        }
        PackageInfo info = (PackageInfo) buttonView.getTag(R.id.title);
        unckeckCheckBoxes(info);
    }

    private void unckeckCheckBoxes(PackageInfo master) {
        String masterFileName = master.getFilename();
        boolean masterIsGapps = master.isGapps();
        for (int i = 0; i < mLayout.getChildCount(); i++) {
            View view = mLayout.getChildAt(i);
            if (view instanceof CheckBox) {
                PackageInfo info = (PackageInfo) view.getTag(R.id.title);
                String fileName = info.getFilename();
                boolean isGapps = info.isGapps();
                if (masterIsGapps == isGapps && !masterFileName.equals(fileName)) {
                    ((CheckBox) view).setChecked(false);
                }
            }
        }
    }

    private PackageInfo[] getPackages() {
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        for (int i = 0; i < mLayout.getChildCount(); i++) {
            View view = mLayout.getChildAt(i);
            if (view instanceof CheckBox) {
                if (((CheckBox) view).isChecked()) {
                    PackageInfo info = (PackageInfo) view.getTag(R.id.title);
                    list.add(info);
                }
            }
        }
        return list.toArray(new PackageInfo[list.size()]);
    }

    private void addPackages(PackageInfo[] packages) {
        Context context = getContext();
        Resources res = context.getResources();
        for (int i = 0; packages != null && i < packages.length; i++) {
            CheckBox check = new CheckBox(context, null);
            check.setTag(R.id.title, packages[i]);
            check.setText(packages[i].getFilename());
            check.setOnCheckedChangeListener(this);
            check.setChecked(i == 0);
            mLayout.addView(check);
            TextView text = new TextView(context);
            text.setText(packages[i].getFilename());
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    res.getDimension(R.dimen.card_medium_text_size));
            text.setTextColor(R.color.card_text);
            text.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            mAdditional.addView(text);
            text = new TextView(context);
            text.setText(packages[i].getSize());
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    res.getDimension(R.dimen.card_small_text_size));
            text.setTextColor(R.color.card_text);
            text.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            mAdditional.addView(text);
            text = new TextView(context);
            text.setText(res.getString(R.string.update_host, packages[i].getHost()));
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    res.getDimension(R.dimen.card_small_text_size));
            text.setTextColor(R.color.card_text);
            text.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            mAdditional.addView(text);
        }
    }

}
