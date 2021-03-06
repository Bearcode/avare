/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ds.avare.webinfc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ds.avare.storage.Preferences;
import com.ds.avare.touch.LongTouchDestination;
import com.ds.avare.utils.GenericCallback;
import com.ds.avare.utils.WeatherHelper;
import com.ds.avare.weather.Airep;

/**
 * 
 * @author zkhan
 * This class feeds the WebView with data
 */
public class WebAppMapInterface {
    private Context mContext;
    private WebView mWebView;
    private Preferences mPref;
    private GenericCallback mCallback;

    private static final int MSG_SET_DATA = 1;
    private static final int MSG_ACTION = 2;

    /**
     * Instantiate the interface and set the context
     */
    public WebAppMapInterface(Context c, WebView v, GenericCallback cb) {
        mWebView = v;
        mContext = c;
        mPref = new Preferences(c);
        mCallback = cb;
    }

    /**
     * Do something on a button press
     */
    @JavascriptInterface
    public void doAction(String action) {
        Message m = mHandler.obtainMessage();
        m.obj = action;
        m.what = MSG_ACTION;
        mHandler.sendMessage(m);
    }


    public void setData(LongTouchDestination data) {
        Message m = mHandler.obtainMessage();
        m.obj = data;
        m.what = MSG_SET_DATA;
        mHandler.sendMessage(m);
    }



    /**
     * This leak warning is not an issue if we do not post delayed messages, which is true here.
     * Must use handler for functions called from JS, but for uniformity, call all JS from this handler
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (MSG_SET_DATA == msg.what) {


                LongTouchDestination data = (LongTouchDestination)msg.obj;
                String taf = "";
                if(data.taf != null) {
                    taf = "<hr><font color=\"yellow\">TAF </font>" + WeatherHelper.formatVisibilityHTML(WeatherHelper.formatTafHTML(WeatherHelper.formatWindsHTML(data.taf.rawText, mPref.isWeatherTranslated()), mPref.isWeatherTranslated())).replace("'", "\"");
                }

                String metar = "";
                if(data.metar != null) {
                    metar = WeatherHelper.formatMetarHTML(data.metar.rawText, mPref.isWeatherTranslated());
                    metar = "<hr><font color=\"yellow\">METAR </font>" + "<font color=\"" + WeatherHelper.metarColorString(data.metar.flightCategory) + "\">" + metar +  "</font>";
                }

                String airep = "";
                if(data.airep != null) {
                    for(Airep a : data.airep) {
                        String p = WeatherHelper.formatPirepHTML(a.rawText, mPref.isWeatherTranslated());
                        airep += p + "<br><br>";
                    }
                    if(!airep.equals("")) {
                        airep = "<hr><font color=\"yellow\">PIREP</font><br>" + airep;
                    }
                }

                String sua = "";
                if(data.sua != null) {
                    sua = "<hr><font color=\"yellow\">Special Use Airspace</font><br>";
                    sua += data.sua.replace("\n", "<br>");
                }

                String tfr = "";
                if(data.tfr != null) {
                    if(!data.tfr.equals("")) {
                        tfr = "<hr><font color=\"yellow\">TFR</font><br>";
                        tfr += data.tfr.replace("\n", "<br>");
                    }
                }

                String layer = "";
                if(data.layer != null) {
                    if(!data.layer.equals("")) {
                        layer = "<hr><font color=\"yellow\">Layer Time</font><br>";
                        layer += data.layer;
                    }
                }

                String mets = "";
                if(data.mets != null) {
                    if(!data.mets.equals("")) {
                        mets = "<hr><font color=\"yellow\">SIG/AIRMETs</font><br>";
                        mets += data.mets.replace("\n", "<br>");
                    }
                }

                String performance = "";
                if(data.performance != null) {
                    if(!data.performance.equals("")) {
                        performance = "<hr><font color=\"yellow\">Performance</font><br>";
                        performance += data.performance.replace("\n", "<br>");
                    }
                }

                String winds = "";
                if(data.wa != null) {
                    winds = "<hr><font color=\"yellow\">Winds/Temp. Aloft</font><br>";
                    winds += data.wa.station + data.wa.time + "<br>";
                    winds += "@ 03000 ft: " + WeatherHelper.decodeWind(data.wa.w3k) + "<br>";
                    winds += "@ 06000 ft: " + WeatherHelper.decodeWind(data.wa.w6k) + "<br>";
                    winds += "@ 09000 ft: " + WeatherHelper.decodeWind(data.wa.w9k) + "<br>";
                    winds += "@ 12000 ft: " + WeatherHelper.decodeWind(data.wa.w12k) + "<br>";
                    winds += "@ 18000 ft: " + WeatherHelper.decodeWind(data.wa.w18k) + "<br>";
                    winds += "@ 24000 ft: " + WeatherHelper.decodeWind(data.wa.w24k) + "<br>";
                    winds += "@ 30000 ft: " + WeatherHelper.decodeWind(data.wa.w30k) + "<br>";
                    winds += "@ 34000 ft: " + WeatherHelper.decodeWind(data.wa.w34k) + "<br>";
                    winds += "@ 39000 ft: " + WeatherHelper.decodeWind(data.wa.w39k);
                }

                mWebView.loadUrl("javascript:plan_clear()");
                String func = "javascript:setData('" +
                        data.airport + "','" +
                        "<font color=\"yellow\">Position " + "</font>" + data.info + "','" +
                        metar + "','" +
                        taf + "','" +
                        airep + "','" +
                        tfr + "','" +
                        sua + "','" +
                        mets + "','" +
                        performance + "','" +
                        winds + "','" +
                        layer +
                        "')";



                mWebView.loadUrl(func);
            }
            else if (MSG_ACTION == msg.what) {
                mCallback.callback((String)msg.obj, null);
            }
        }
    };
}