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

package com.paranoid.paranoidota;

import java.util.ArrayList;

public class ListItems {

    public ArrayList<PreferenceItem> ITEMS = new ArrayList<PreferenceItem>();

    public ListItems() {
        addItem(new PreferenceItem(R.string.slider_overview, R.drawable.slider_check));
        addItem(new PreferenceItem(R.string.slider_updates, R.drawable.slider_rom));
        addItem(new PreferenceItem(R.string.slider_install, R.drawable.slider_download));
        addItem(new PreferenceItem(R.string.slider_changelog, R.drawable.slider_changelog));
    }

    public static class PreferenceItem {
        public int content;
        public int drawable;

        public PreferenceItem(int content, int drawable) {
            this.content = content;
            this.drawable = drawable;
        }
    }

    private void addItem(PreferenceItem item) {
        ITEMS.add(item);
    }
}
