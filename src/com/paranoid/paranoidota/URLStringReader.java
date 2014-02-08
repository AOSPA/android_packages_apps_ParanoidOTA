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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class URLStringReader extends AsyncTask<String, Void, Void> {

    public static interface URLStringReaderListener {

        public void onReadEnd(String buffer);
        public void onReadError(Exception ex);
    };

    private String mBuffer;
    private Exception mException;
    private URLStringReaderListener mListener;

    public URLStringReader(URLStringReaderListener listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... params) {
        mBuffer = null;
        try {
            mBuffer = readString(params[0]);
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mListener != null) {
            if (mBuffer != null) {
                mListener.onReadEnd(mBuffer);
            } else if (mException != null) {
                mListener.onReadError(mException);
            }
        }
        super.onPostExecute(result);
    }

    private String readString(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        URLConnection yc = url.openConnection();
        BufferedReader in = null;
        StringBuffer sb = new StringBuffer();
        try {
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
        } finally {
            if (in != null)
                in.close();
        }
        return sb.toString();
    }
}