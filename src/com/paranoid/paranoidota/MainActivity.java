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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.paranoid.paranoidota.Utils.NotificationInfo;
import com.paranoid.paranoidota.activities.SettingsActivity;
import com.paranoid.paranoidota.cards.DownloadCard;
import com.paranoid.paranoidota.cards.InstallCard;
import com.paranoid.paranoidota.cards.SystemCard;
import com.paranoid.paranoidota.cards.UpdatesCard;
import com.paranoid.paranoidota.helpers.DownloadHelper;
import com.paranoid.paranoidota.helpers.RebootHelper;
import com.paranoid.paranoidota.helpers.RecoveryHelper;
import com.paranoid.paranoidota.helpers.DownloadHelper.DownloadCallback;
import com.paranoid.paranoidota.updater.GappsUpdater;
import com.paranoid.paranoidota.updater.RomUpdater;
import com.paranoid.paranoidota.updater.Updater;
import com.paranoid.paranoidota.updater.Updater.PackageInfo;
import com.paranoid.paranoidota.updater.Updater.UpdaterListener;
import com.paranoid.paranoidota.widget.Card;

public class MainActivity extends Activity implements UpdaterListener, DownloadCallback {

    private static final String CHANGELOG = "https://plus.google.com/app/basic/+ParanoidAndroidCorner/posts";
    private static final String STATE = "STATE";

    public static final int STATE_UPDATES = 0;
    public static final int STATE_DOWNLOAD = 1;
    public static final int STATE_INSTALL = 2;

    private RecoveryHelper mRecoveryHelper;
    private RebootHelper mRebootHelper;
    private DownloadCallback mDownloadCallback;

    private SystemCard mSystemCard;
    private UpdatesCard mUpdatesCard;
    private DownloadCard mDownloadCard;
    private InstallCard mInstallCard;

    private RomUpdater mRomUpdater;
    private GappsUpdater mGappsUpdater;
    private NotificationInfo mNotificationInfo;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private LinearLayout mCardsLayout;
    private TextView mContentTitle;
    private MenuItem mCheckMenuItem;

    private Context mContext;
    private Bundle mSavedInstanceState;

    private int mState = STATE_UPDATES;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mSavedInstanceState = savedInstanceState;

        setContentView(R.layout.activity_main);
        setTitle(R.string.updates);

        mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, android.R.id.text1, new String[] {
                        getString(R.string.updates),
                        getString(R.string.install),
                        getString(R.string.changelog) }));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mCardsLayout = (LinearLayout) findViewById(R.id.cards_layout);
        mContentTitle = (TextView) findViewById(R.id.header);

        Utils.setRobotoThin(mContext, findViewById(R.id.mainlayout));

        mRecoveryHelper = new RecoveryHelper(this);
        mRebootHelper = new RebootHelper(mRecoveryHelper);

        mRomUpdater = new RomUpdater(this, false);
        mRomUpdater.addUpdaterListener(this);
        mGappsUpdater = new GappsUpdater(this, false);
        mGappsUpdater.addUpdaterListener(this);

        DownloadHelper.init(this, this);

        Intent intent = getIntent();
        onNewIntent(intent);

        if (mSavedInstanceState == null) {

            IOUtils.init(this);

            mCardsLayout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.up_from_bottom));

            if (mNotificationInfo != null) {
                if (mNotificationInfo.mNotificationId != Updater.NOTIFICATION_ID) {
                    checkUpdates();
                } else {
                    mRomUpdater.setLastUpdates(mNotificationInfo.mPackageInfosRom);
                    mGappsUpdater.setLastUpdates(mNotificationInfo.mPackageInfosGapps);
                }
            } else {
                checkUpdates();
            }
            if (DownloadHelper.isDownloading(true) || DownloadHelper.isDownloading(false)) {
                setState(STATE_DOWNLOAD, true, false);
            } else {
                if (mState != STATE_INSTALL) {
                    setState(STATE_UPDATES, true, false);
                }
            }
        } else {
            setState(mSavedInstanceState.getInt(STATE), false, true);
        }

        if (!Utils.alarmExists(this, true)) {
            Utils.setAlarm(this, true, true);
        }

        if (!Utils.alarmExists(this, false)) {
            Utils.setAlarm(this, true, false);
        }
    }

    public void setDownloadCallback(DownloadCallback downloadCallback) {
        mDownloadCallback = downloadCallback;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE, mState);
        switch (mState) {
            case STATE_UPDATES :
                mSystemCard.saveState(outState);
                mUpdatesCard.saveState(outState);
                break;
            case STATE_DOWNLOAD :
                mDownloadCard.saveState(outState);
                break;
            case STATE_INSTALL :
                mInstallCard.saveState(outState);
                break;
        }
    }

    private void checkUpdates() {
        mRomUpdater.check();
        mGappsUpdater.check();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public void selectItem(int position) {
        switch (position) {
            case 0 :
                setTitle(R.string.updates);
                setState(STATE_UPDATES, true, false);
                mDrawerList.setItemChecked(0, true);
                break;
            case 1 :
                setTitle(R.string.install);
                setState(STATE_INSTALL, true, false);
                mDrawerList.setItemChecked(1, true);
                break;
            case 2 :
                setTitle(R.string.changelog);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CHANGELOG));
                startActivity(browserIntent);
                switch (mState) {
                    case STATE_UPDATES :
                    case STATE_DOWNLOAD :
                        setTitle(R.string.updates);
                        mDrawerList.setItemChecked(0, true);
                        break;
                    case STATE_INSTALL :
                        setTitle(R.string.install);
                        mDrawerList.setItemChecked(1, true);
                        break;
                }
                break;
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mNotificationInfo = null;
        if (intent != null && intent.getExtras() != null) {
            mNotificationInfo = (NotificationInfo) intent.getSerializableExtra(Utils.FILES_INFO);
            if (intent.getBooleanExtra(Utils.CHECK_DOWNLOADS_FINISHED, false)) {
                DownloadHelper.checkDownloadFinished(this,
                        intent.getLongExtra(Utils.CHECK_DOWNLOADS_ID, -1L));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadHelper.registerCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadHelper.unregisterCallback();
    }

    @Override
    public void versionFound(PackageInfo[] info, boolean isRom) {
        boolean checking = mRomUpdater.isScanning() || mGappsUpdater.isScanning();
        mContentTitle.setText(checking ? R.string.title_checking : R.string.app_name);
    }

    @Override
    public void startChecking(boolean isRom) {
        mContentTitle.setText(R.string.title_checking);
        setProgressBarIndeterminate(true);
        setProgressBarVisibility(true);
    }

    @Override
    public void checkError(String cause, boolean isRom) {
        mContentTitle.setText(R.string.app_name);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        mCheckMenuItem = menu.findItem(R.id.action_check);
        if (!drawerOpen) {
            mCheckMenuItem.setVisible(mState == STATE_UPDATES);
        } else {
            mCheckMenuItem.setVisible(!drawerOpen);
        }
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_check:
                checkUpdates();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setState(int state) {
        setState(state, false, false);
    }

    public void setState(int state, boolean animate, boolean fromRotation) {
        setState(state, animate, null, null, null, false, fromRotation);
    }

    public void setState(int state, boolean animate, PackageInfo[] infos,
            Uri uri, String md5, boolean isRom, boolean fromRotation) {
        mState = state;
        switch (state) {
            case STATE_UPDATES :
                mDrawerList.setItemChecked(0, true);
                break;
            case STATE_INSTALL :
                mDrawerList.setItemChecked(1, true);
                break;
            case STATE_DOWNLOAD :
                break;
        }
        if (mCheckMenuItem != null) {
            mCheckMenuItem.setVisible(state == STATE_UPDATES);
        }
        switch (state) {
            case STATE_UPDATES :
                if (mSystemCard == null) {
                    mSystemCard = new SystemCard(mContext, null, mRomUpdater, mGappsUpdater,
                            mSavedInstanceState);
                }
                if (mUpdatesCard == null) {
                    mUpdatesCard = new UpdatesCard(mContext, null, mRomUpdater, mGappsUpdater,
                            mSavedInstanceState);
                }
                addCards(new Card[] { mSystemCard,  mUpdatesCard }, animate, true);
                break;
            case STATE_DOWNLOAD :
                if (mDownloadCard == null) {
                    mDownloadCard = new DownloadCard(mContext, null, infos, mSavedInstanceState);
                } else {
                    mDownloadCard.setInitialInfos(infos);
                }
                addCards(new Card[] { mDownloadCard }, animate, true);
                break;
            case STATE_INSTALL :
                if (mInstallCard == null) {
                    mInstallCard = new InstallCard(mContext, null, mRebootHelper,
                            mSavedInstanceState);
                }
                if (!DownloadHelper.isDownloading(!isRom)) {
                    addCards(new Card[] {mInstallCard}, !fromRotation, true);
                } else {
                    addCards(new Card[] {mInstallCard}, true, false);
                }
                if (uri != null) {
                    mInstallCard.addFile(uri, md5);
                }
                break;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void addCards(Card[] cards, boolean animate, boolean remove) {
        mCardsLayout.clearAnimation();
        if (remove) {
            mCardsLayout.removeAllViews();
        }
        if (animate) {
            mCardsLayout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.up_from_bottom));
        }
        for (Card card: cards){
            mCardsLayout.addView(card);
        }
    }

    @Override
    public void onDownloadStarted() {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadStarted();
        }
    }

    @Override
    public void onDownloadError(String reason) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadError(reason);
        }
    }

    @Override
    public void onDownloadProgress(int progress) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadProgress(progress);
        }
    }

    @Override
    public void onDownloadFinished(Uri uri, final String md5, boolean isRom) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadFinished(uri, md5, isRom);
        }
        if (uri == null) {
            if (!DownloadHelper.isDownloading(!isRom)) {
                setState(STATE_UPDATES, true, false);
            }
        } else {
            setState(STATE_INSTALL, true, null, uri, md5, isRom, false);
        }
    }
}
