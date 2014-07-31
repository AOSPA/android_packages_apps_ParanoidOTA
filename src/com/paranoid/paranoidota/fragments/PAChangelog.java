package com.paranoid.paranoidota.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.paranoid.paranoidota.R;

public class PAChangelog extends Fragment {
	Context context;
	WebView wv;

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.pa_changelog, container,
				false);

		WebView wv = (WebView) view.findViewById(R.id.ChangelogWebView);

		wv.loadUrl("https://plus.google.com/app/basic/+ParanoidAndroidCorner/posts");

		wv.clearCache(true);

		WebSettings webSettings = wv.getSettings();

		wv.getSettings().setPluginState(PluginState.ON);

		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);

		webSettings.setJavaScriptEnabled(true);

		webSettings.setDomStorageEnabled(true);

		wv.setWebChromeClient(new WebChromeClient() {
			ActionBar ab;

			@SuppressWarnings("unused")
			public void onPageFinished(WebView view, String url) {
				ab.setTitle("YOLO");
			}
		});

		wv.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				{
					view.loadUrl(url);
					return true;
				}
			}
		});
		return view;
	}
}
