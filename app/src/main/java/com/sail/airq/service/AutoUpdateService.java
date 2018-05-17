package com.sail.airq.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.sail.airq.AppGlobal;
import com.sail.airq.gson.AirMessage;
import com.sail.airq.gson.Huanbaobu;
import com.sail.airq.gson.JvHeData;
import com.sail.airq.gson.Weather;
import com.sail.airq.util.HttpUtil;
import com.sail.airq.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateBingPic();
        updateWeather();
        updateAir();
        updateHuanbaobu();
        updateHis();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 3 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * update weather data
     */
    private void updateAir() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String airString = prefs.getString("air", null);
        if (airString != null) {
            //有缓存也直接解析天气数据，因为要更新
            AirMessage air = Utility.handleAirResponse(airString);
            final String cityId = air.basic.cityId;
            String airUrl ="https://free-api.heweather.com/s6/air?location="+ cityId+"&key=047ec486703a4d408ce358b46bf14fa5";
            HttpUtil.sendOkHttpRequest(airUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    AirMessage air = Utility.handleAirResponse(responseText);
                    if (air != null && "ok".equals(air.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("air", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存也直接解析天气数据，因为要更新
            Weather weather = Utility.handleWeatherResponse(weatherString);
            final String cityId = weather.basic.cityId;
            String weatherUrl ="https://free-api.heweather.com/s6/weather?location="+cityId+"&key=047ec486703a4d408ce358b46bf14fa5";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }


    private void updateHuanbaobu() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String huanbaobuString = prefs.getString("airforecast", null);
        String city = prefs.getString("current_city",null);
        if (huanbaobuString != null) {
            //有缓存也直接解析天气数据，因为要更新
            String Url ="http://airforcast.market.alicloudapi.com/api/v1/air_forecast/city?city="+city+"市";
            String Appcode = "APPCODE eb96ca5e4735425eb656dcab46ad2750";
            HttpUtil.sendOkHttpForecastRequest(Url,Appcode, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Huanbaobu huanbaobu = Utility.handleHuanbaobuResponse(responseText);
                    if (huanbaobu != null && "true".equals(huanbaobu.success)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("airforecast", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }


    private void updateHis() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String jvString = prefs.getString("jvHeData", null);
        if (jvString != null) {
            //有缓存也直接解析天气数据，因为要更新
            JvHeData jvHeData = Utility.handleJvHeResponse(jvString);
            final String city = jvHeData.now.city;
            String Url ="http://web.juhe.cn:8080/environment/air/cityair?city="+city+"&key=1aee5b0387503b8c8645faf7b366e375";
            HttpUtil.sendOkHttpRequest(Url,new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    JvHeData jvHeDatatmp = Utility.handleJvHeResponse(responseText);
                    if (jvHeDatatmp!= null) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("jvHeData", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }
    /**
     * update bing pic from GuoLin API
     */
//    private void updateBingPicGL() {
//        String requestBingPic = "http://guolin.tech/api/bing_pic";
//        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String bingPic = response.body().string();
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
//                editor.putString("bing_pic", bingPic);
//                editor.apply();
//            }
//        });
//    }

    /**
     * update bing pic
     */
    private void updateBingPic() {
        String bingPicUrl = "http://cn.bing.com/HPImageArchive.aspx?format=js&n=1";
        HttpUtil.sendOkHttpRequest(bingPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    final String bingPic = Utility.handleBingPicResponse(response.body().string());
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
            }
        });
    }
}
