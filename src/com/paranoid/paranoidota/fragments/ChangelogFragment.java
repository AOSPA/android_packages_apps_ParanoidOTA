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

package com.paranoid.paranoidota.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.paranoid.paranoidota.R;

public class ChangelogFragment extends Fragment {

    private static final String CHANGELOG_URL
            = "https://plus.google.com/app/basic/107979589566958860409/posts";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_changelog,
                container, false);
        WebView webView = ((WebView) rootView.findViewById(R.id.changelog));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                // When page is loaded, hide progress bar on activity
                Activity act = getActivity();
                if (act == null) {
                    return;
                }
                act.setProgressBarVisibility(false);
            }

            public void onReceivedError(WebView view, int errorCode, String description,
                                        String failingUrl) {
                getActivity().setProgressBarVisibility(false);
                Toast.makeText(getActivity(), R.string.changelog_error, Toast.LENGTH_SHORT).show();
            }
        });
        if (savedInstanceState == null) {
            webView.loadUrl(CHANGELOG_URL);
        }
        return rootView;
    }

}