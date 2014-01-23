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

package com.paranoid.paranoidota.widget;

import com.paranoid.paranoidota.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Item extends LinearLayout {

    public static interface OnItemClickListener {

        public void onClick(int id);
    };

    private TextView mTitleView;
    private int mDownColor;
    private int mDownTextColor;
    private OnItemClickListener mItemClickListener;
    private ColorStateList mDefaultColors;

    public Item(final Context context, AttributeSet attrs) {
        super(context, attrs);

        String title = null;
        Drawable icon = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Item);

        CharSequence s = a.getString(R.styleable.Item_itemTitle);
        if (s != null) {
            title = s.toString();
        }
        Drawable d = a.getDrawable(R.styleable.Item_itemIcon);
        if (d != null) {
            icon = d;
        }

        mDownColor = a.getColor(R.styleable.Item_itemDownColor, android.R.color.holo_blue_dark);
        mDownTextColor = a.getColor(R.styleable.Item_itemDownTextColor, android.R.color.white);

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item, this, true);

        mTitleView = (TextView) view.findViewById(R.id.title);
        mTitleView.setText(title);
        mDefaultColors = mTitleView.getTextColors();

        ImageView iView = (ImageView) view.findViewById(R.id.icon);
        iView.setImageDrawable(icon);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (!isEnabled()) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBackgroundColor(mDownColor);
                        mTitleView.setTextColor(mDownTextColor);
                        break;
                    case MotionEvent.ACTION_UP:
                        setBackgroundColor(context.getResources().getColor(
                                android.R.color.transparent));
                        mTitleView.setTextColor(mDefaultColors);
                        if (mItemClickListener != null) {
                            mItemClickListener.onClick(Item.this.getId());
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setTitle(int resourceId) {
        mTitleView.setText(resourceId);
    }

    public void setTitle(String text) {
        mTitleView.setText(text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mTitleView != null) {
            if (enabled) {
                mTitleView.setTextColor(mDefaultColors);
            } else {
                mTitleView.setTextColor(R.color.card_text);
            }
        }
    }
}