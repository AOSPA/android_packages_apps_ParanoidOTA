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
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.paranoid.paranoidota.R;
import com.paranoid.paranoidota.updater.GappsUpdater;
import com.paranoid.paranoidota.updater.RomUpdater;
import com.paranoid.paranoidota.widget.Card;

public class SystemCard extends Card {

    private View mAdditional;
    private TextView mAdditionalText;

    public SystemCard(Context context, AttributeSet attrs, RomUpdater romUpdater,
            GappsUpdater gappsUpdater, String recoveryName) {
        super(context, attrs);

        setTitle(R.string.system_title);
        setLayoutId(R.layout.card_system);

        mAdditional = findLayoutViewById(R.id.additional);
        mAdditionalText = (TextView) findLayoutViewById(R.id.additional_text);

        Resources res = context.getResources();

        TextView romView = (TextView) findLayoutViewById(R.id.rom);
        romView.setText(res.getString(R.string.system_rom,
                romUpdater.getVersion().toString(false, true)));

        TextView gappsView = (TextView) findLayoutViewById(R.id.gapps);
        gappsView.setText(res.getString(R.string.system_gapps, gappsUpdater.getType(), gappsUpdater
                .getVersion().toString(false, false)));

        TextView recoveryView = (TextView) findLayoutViewById(R.id.recovery);
        String recoveryUnknown = res.getString(R.string.system_recovery_unknown);
        recoveryView.setText(res.getString(R.string.system_recovery, recoveryName == null
                || recoveryName.equals("") ? recoveryUnknown : recoveryName));

        if (recoveryName == null || "".equals(recoveryName)) {
            mAdditionalText.setText(R.string.system_recovery_unknown_expanded);
        }
    }

    @Override
    public void expand() {
        super.expand();
        mAdditional.setVisibility(View.VISIBLE);
    }

    @Override
    public void collapse() {
        super.collapse();
        mAdditional.setVisibility(View.GONE);
    }

}
