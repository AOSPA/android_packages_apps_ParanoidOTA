package com.paranoid.paranoidota.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.paranoid.paranoidota.R;

public class PAGooglePlus extends Fragment {
	Context context;
	WebView wv;

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().requestWindowFeature(
				Window.FEATURE_INDETERMINATE_PROGRESS);

		final View view = inflater.inflate(R.layout.pa_gplus, container, false);

		WebView wv = (WebView) view.findViewById(R.id.GPlusWebView);

		wv.loadUrl("https://plus.google.com/communities/112514149478109338346");

		wv.clearCache(true);

		WebSettings webSettings = wv.getSettings();

		wv.getSettings().setPluginState(PluginState.ON);

		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);

		webSettings.setJavaScriptEnabled(true);

		webSettings.setDomStorageEnabled(true);

		wv.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, final int progress) {

			}

		});

		wv.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {

			}

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
