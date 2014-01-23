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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paranoid.paranoidota.R;


public abstract class Card extends LinearLayout {

    private Context mContext;
    private View mView;
    private LinearLayout mCardLayout;
    private TextView mTitleView;
    private View mLayoutView;
    private ImageView mButton;
    private boolean mExpanded = false;

    public Card(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        String title = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Card);

        CharSequence s = a.getString(R.styleable.Card_title);
        if (s != null) {
            title = s.toString();
        }

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.card, this, true);

        mCardLayout = (LinearLayout) mView.findViewById(R.id.card_layout);
        mButton = (ImageView) mView.findViewById(R.id.headerbutton);

        if (canExpand()) {
            mButton.setOnClickListener(new OnClickListener() {
    
                @Override
                public void onClick(View v) {
                    if (mExpanded) {
                        collapse();
                    } else {
                        expand();
                    }
                    mExpanded = !mExpanded;
                }
    
            });
        } else {
            mButton.setVisibility(View.GONE);
        }

        mTitleView = (TextView) mView.findViewById(R.id.title);
        mTitleView.setText(title);

    }

    public void expand() {
        mButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_collapse));
    }

    public void collapse() {
        mButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_expand));
    }

    protected boolean canExpand() {
        return true;
    }

    public void setTitle(int resourceId) {
        mTitleView.setText(resourceId);
    }

    public void setTitle(String text) {
        mTitleView.setText(text);
    }

    protected void setLayoutId(int id) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutView = inflater.inflate(id, mCardLayout, true);
    }

    protected View findLayoutViewById(int id) {
        return mLayoutView.findViewById(id);
    }
}