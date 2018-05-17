package com.sail.airq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sail.airq.gson.AirForecast;
import com.sail.airq.gson.AirMessage;
import com.sail.airq.gson.Huanbaobu;
import com.sail.airq.gson.HuanbaobuData;
import com.sail.airq.gson.LifeStyle;
import com.sail.airq.gson.Weather;
import com.sail.airq.service.AutoUpdateService;
import com.sail.airq.util.HttpUtil;
import com.sail.airq.util.Utility;


import org.w3c.dom.Text;

import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.sail.airq.AppGlobal.aqiInfo;
import static com.sail.airq.AppGlobal.city;
import static com.sail.airq.AppGlobal.cond;
import static com.sail.airq.AppGlobal.pres;


public class NowFragment extends Fragment {


    SwipeRefreshLayout.OnRefreshListener refreshListener;
    SharedPreferences prefs;
    private TextView tmpText;
    private TextView condText;
    private TextView pm10Text;
    private TextView pm25Text;
    private TextView no2Text;
    private TextView so2Text;
    private TextView coText;
    private TextView o3Text;
    private TextView winddirText;
    private TextView windscText;
    private TextView atmpText;
    private TextView humText;
    private TextView visText;
    private TextView cloudText;
    private TextView presText;
    private TextView fdateText;
    private TextView qltyText;
    private TextView faqiText;
    private TextView fmainText;
    private MainActivity mainActivity;
    private ScrollView airLayout;
    public SwipeRefreshLayout swipeRefresh;
    private LinearLayout suggestionLayout;
    private LinearLayout forecastLayout;
    private TextView AQINowText;
    private TextView qltyNowText;
    private TextView mtitleCity
            ;
    private String currentCity=null;
    private String currentParCity=null;

    private String localCity=null;
    private String localParCity=null;

    private String chooseCity=null;
    private String chooseParCity=null;

    private Button menuButton;
    private ImageView bingPicImg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_now,container,false);
        Log.v("FragmentNow", "initViews: ");
        initViews(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
/*
        currentCity = savedInstanceState.getString("current_city");
        currentParCity = savedInstanceState.getString("current_parcity");
        chooseCity = savedInstanceState.getString("choose_city");
        chooseParCity = savedInstanceState.getString("choose_parcity");
        localCity = savedInstanceState.getString("local_city");
        localParCity = savedInstanceState.getString("local_parcity");
        Log.v("onactivity",currentCity+chooseCity+localCity);
*/
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof MainActivity){
            MainActivity mActivity = (MainActivity)activity;

            mtitleCity = (TextView)mActivity.findViewById(R.id.title_city);
            bingPicImg = (ImageView)mActivity.findViewById(R.id.bing_pic_img);
            menuButton = (Button)mActivity.findViewById(R.id.title_menu);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidd) {
        if (hidd) {
            //隐藏时所作的事情

        } else {
            //显示时所作的事情
            menuButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("choose_city",chooseCity);
        outState.putString("choose_parcity",chooseParCity);
        outState.putString("current_city",currentCity);
        outState.putString("current_parcity",currentParCity);
        outState.putString("local_city",localCity);
        outState.putString("local_parcity",localParCity);

    }

    @Override
    public void onResume() {
        super.onResume();
        menuButton.setVisibility(View.VISIBLE);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        chooseCity = prefs.getString("choose_city",null);
        chooseParCity = prefs.getString("choose_parcity",null);
        Log.v("onResume:chooseCity",chooseCity);
        Log.v("onResume:chooseParCity",chooseParCity);
        currentCity = prefs.getString("current_city",null);
        currentParCity = prefs.getString("current_parcity",null);
        //Log.v("onResume:currentCity",currentCity);
        //Log.v("onResume:currentParCity",currentParCity);
        localCity = prefs.getString("local_city",null);
        localParCity = prefs.getString("local_parcity",null);
        //Log.v("onResume:localCity",localCity);
        //Log.v("onResume:localParCity",localParCity);
        String airString = prefs.getString("air",null);
        String weatherString = prefs.getString("weather",null);
        String foreString = prefs.getString("airforecast",null);

        if(currentCity!=null&&chooseCity!=null&&currentCity.equals(chooseCity)){
            mtitleCity.setText(chooseParCity+": "+chooseCity);
            requestAir(chooseCity);
            requestWeather(chooseCity);
            requestHuanbaobu(chooseParCity);
        }else if(currentCity!=null&&localCity!=null&&currentCity.equals(localCity)){
            mtitleCity.setText("▼ "+localParCity+": "+localCity);
            requestAir(localCity);
            requestWeather(localCity);
            requestHuanbaobu(localParCity);
        }else{
            AirMessage air = Utility.handleAirResponse(airString);
            Weather weather = Utility.handleWeatherResponse(weatherString);
            Huanbaobu huanbaobu = Utility.handleHuanbaobuResponse(foreString);
            mtitleCity.setText(air.basic.parentcity+" "+air.basic.city);
            showWeatherNowInfo(weather);
            showAirNowInfo(air);
            showForecast(huanbaobu);
        }
        loadBingPic();

    }

    protected void initViews(View view){

        Log.v("FragmentNow", "initViews: ");

        mainActivity = (MainActivity) getActivity();
        airLayout = (ScrollView)view.findViewById(R.id.air_layout);
        AQINowText = (TextView)view.findViewById(R.id.nowAQI_text);
        qltyNowText = (TextView)view.findViewById(R.id.air_qlty_text);
        pm10Text = (TextView)view.findViewById(R.id.pm10_text);
        pm25Text = (TextView)view.findViewById(R.id.pm25_text);
        no2Text = (TextView)view.findViewById(R.id.no2_text);
        so2Text = (TextView)view.findViewById(R.id.so2_text);
        coText = (TextView)view.findViewById(R.id.co_text);
        o3Text = (TextView)view.findViewById(R.id.o3_text);
        tmpText = (TextView)view.findViewById(R.id.nowtmp_text);
        condText = (TextView)view.findViewById(R.id.cond_text);
        winddirText = (TextView)view.findViewById(R.id.winddir_text);
        windscText = (TextView)view.findViewById(R.id.windsc_text);
        atmpText = (TextView)view.findViewById(R.id.atmp_text);
        humText = (TextView)view.findViewById(R.id.hum_text);
        visText = (TextView)view.findViewById(R.id.vis_text);
        cloudText = (TextView)view.findViewById(R.id.cloud_text);
        presText = (TextView)view.findViewById(R.id.pres_text);


        suggestionLayout = (LinearLayout)view.findViewById(R.id.suggestion_layout);
        forecastLayout = (LinearLayout)view.findViewById(R.id.forecast_layout);


        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        refreshListener = new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                chooseCity = prefs.getString("choose_city",null);
                chooseParCity = prefs.getString("choose_parcity",null);
                //Log.v("onR:chooseCity",chooseCity);
                //Log.v("onR:chooseParCity",chooseParCity);
                currentCity = prefs.getString("current_city",null);
                currentParCity = prefs.getString("current_parcity",null);
                ///Log.v("onR:currentCity",currentCity);
               // Log.v("onR:currentParCity",currentParCity);
                localCity = prefs.getString("local_city",null);
                localParCity = prefs.getString("local_parcity",null);
                //Log.v("onR:localCity",localCity);
                //Log.v("onR:localParCity",localParCity);
                if(currentCity.equals(localCity)) {
                    mtitleCity.setText("▼ "+localParCity+": "+localCity);
                    requestAir(localCity);
                    requestWeather(localCity);
                    requestHuanbaobu(localParCity);
                }else{
                    mtitleCity.setText(chooseParCity+": "+chooseCity);
                    requestAir(chooseCity);
                    requestWeather(chooseCity);
                    requestHuanbaobu(chooseParCity);
                }
                loadBingPic();
            }
        };
        swipeRefresh.setOnRefreshListener(refreshListener);

    }

    public void requestAir(final String cityId){
        String airUrl = "https://free-api.heweather.com/s6/air?location="+cityId+"&key=047ec486703a4d408ce358b46bf14fa5";

        HttpUtil.sendOkHttpRequest(airUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mainActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mainActivity,"获取空气质量信息失败，请检查网络",Toast.LENGTH_SHORT);
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final AirMessage air = Utility.handleAirResponse(responseText);
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (air!=null && "ok".equals(air.status)){
                            currentCity = air.basic.cityId;
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                            editor.putString("air",responseText);
                            editor.apply();

                            showAirNowInfo(air);
                        }else {
                            Toast.makeText(mainActivity,"获取空气质量信息失败",Toast.LENGTH_SHORT);
                            String tmp = prefs.getString("air",null);
                            AirMessage Airtmp = Utility.handleAirResponse(tmp);
                            showAirNowInfo(Airtmp);
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void requestWeather(final String cityId){
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location="+cityId+"&key=047ec486703a4d408ce358b46bf14fa5";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mainActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mainActivity,"获取空气质量信息失败，请检查网络",Toast.LENGTH_SHORT);
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                            editor.putString("weather",responseText);
                            //editor.putString("city_id",mCityId);
                            editor.apply();
                            showWeatherNowInfo(weather);
                        }else {
                            Toast.makeText(mainActivity,"获取空气质量信息失败",Toast.LENGTH_SHORT);
                            String tmp = prefs.getString("weather",null);
                            Weather Weathertmp = Utility.handleWeatherResponse(tmp);
                            showWeatherNowInfo(Weathertmp);
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }
    protected void requestHuanbaobu(String city){
        String airUrl = "http://airforcast.market.alicloudapi.com/api/v1/air_forecast/city?city="+city+"市";
        Log.v("Fragment", "requestLLAir: "+airUrl);
        String Appcode = "APPCODE eb96ca5e4735425eb656dcab46ad2750";
        HttpUtil.sendOkHttpForecastRequest(airUrl,Appcode, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mainActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mainActivity,"获取空气质量信息失败，请检查网络",Toast.LENGTH_SHORT);
                        //swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.v("Huanbaobu:", "onResponse: "+responseText);
                final Huanbaobu H = Utility.handleHuanbaobuResponse(responseText);
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (H!=null && "true".equals(H.success)){
                            Log.v("ss","sssss");
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                            editor.putString("airforecast",responseText);
                            editor.apply();
                            showForecast(H);
                        }else {
                            Toast.makeText(mainActivity,"获取空气质量信息失败",Toast.LENGTH_SHORT);
                            String tmp = prefs.getString("airforecast",null);
                            Huanbaobu Htmp = Utility.handleHuanbaobuResponse(tmp);
                            showForecast(Htmp);
                        }
                        //swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });

    }



    protected void showWeatherNowInfo(Weather weather){

        AppGlobal.atmp = weather.now.apparentTemperature;
        AppGlobal.tmp = weather.now.temperature;
        AppGlobal.cond = weather.now.cond;
        AppGlobal.winddir = weather.now.windDir;
        AppGlobal.windsc = weather.now.windpower;
        AppGlobal.windspd = weather.now.windspeed;
        AppGlobal.hum = weather.now.humidity;
        AppGlobal.pres = weather.now.pressure;
        AppGlobal.pcpn = weather.now.aop;
        AppGlobal.vis = weather.now.visibility;
        AppGlobal.cloud = weather.now.cloud;

        tmpText.setText(AppGlobal.tmp+"℃");
        condText.setText(AppGlobal.cond);

        atmpText.setText(AppGlobal.atmp+"℃");
        winddirText.setText(AppGlobal.winddir);
        humText.setText(AppGlobal.hum+"%");
        presText.setText(AppGlobal.pres+"百帕");
        visText.setText(AppGlobal.vis+"km");
        cloudText.setText(AppGlobal.cloud);

        switch (AppGlobal.windsc){
            case "0":windscText.setText("无风");break;
            case "1":windscText.setText("软风");break;
            case "2":windscText.setText("轻风");break;
            case "3":windscText.setText("微风");break;
            case "4":windscText.setText("和风");break;
            case "5":windscText.setText("清风");break;
            case "6":windscText.setText("强风");break;
            case "7":windscText.setText("疾风");break;
            case "8":windscText.setText("大风");break;
            case "9":windscText.setText("烈风");break;
            case "10":windscText.setText("狂风");break;
            case "11":windscText.setText("暴风");break;
            default:windscText.setText("暴风");
        }

        suggestionLayout.removeAllViews();
        for (LifeStyle lifeStyle:weather.lifeStyleList){
            View view = LayoutInflater.from(mainActivity).inflate(R.layout.suggestion_item,suggestionLayout,false);
            ImageView typePic = (ImageView)view.findViewById(R.id.type_pic);
            TextView brfText = (TextView)view.findViewById(R.id.brf_text);
            TextView txtText = (TextView)view.findViewById(R.id.txt_text);



            switch (lifeStyle.type){
                case "comf":Glide.with(mainActivity).load(R.drawable.ic_comf_pic).into(typePic);break;
                case "cw":Glide.with(mainActivity).load(R.drawable.ic_cw_pic).into(typePic);break;
                case "drsg":Glide.with(mainActivity).load(R.drawable.ic_drsg_pic).into(typePic);break;
                case "flu":Glide.with(mainActivity).load(R.drawable.ic_flu_pic).into(typePic);break;
                case "sport":Glide.with(mainActivity).load(R.drawable.ic_sport_pic).into(typePic);break;
                case "trav":Glide.with(mainActivity).load(R.drawable.ic_trav_pic).into(typePic);break;
                case "uv":Glide.with(mainActivity).load(R.drawable.ic_uv).into(typePic);break;
                case "air":Glide.with(mainActivity).load(R.drawable.ic_air_pic).into(typePic);break;
                default: break;
            }
            brfText.setText(lifeStyle.briefintro);
            txtText.setText(lifeStyle.detailinfo);

            suggestionLayout.addView(view);



        }
        airLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(mainActivity, AutoUpdateService.class);
        mainActivity.startService(intent);
    }

    protected void showAirNowInfo(AirMessage air) {

        //now
        AppGlobal.cityId = air.basic.cityId;
        AppGlobal.parentCity = air.basic.parentcity;
        AppGlobal.city = air.basic.city;
        AppGlobal.updateTime = air.update.updateTime;//更新数据时间
        AppGlobal.aqi = air.airnow.aqi;
        AppGlobal.aqiInfo = air.airnow.qlty;
        AppGlobal.mainPoll = air.airnow.main;
        AppGlobal.pubtime = air.airnow.pubtime;//数据发布时间
        AppGlobal.pm10 = air.airnow.pm10;
        AppGlobal.pm25 = air.airnow.pm25;
        AppGlobal.no2 = air.airnow.no2;
        AppGlobal.so2 = air.airnow.so2;
        AppGlobal.co = air.airnow.co;
        AppGlobal.o3 = air.airnow.o3;

        AQINowText.setText(AppGlobal.aqi);
        qltyNowText.setText(AppGlobal.aqiInfo);
        //mtitleCity.setText(AppGlobal.parentCity);

        pm10Text.setText(AppGlobal.pm10);
        pm25Text.setText(AppGlobal.pm25);
        no2Text.setText(AppGlobal.no2);
        so2Text.setText(AppGlobal.so2);
        coText.setText(AppGlobal.co);
        o3Text.setText(AppGlobal.o3);


/*
        //forecast_dailiy
        for (AirForecast airForecast : air.forecastList) {
            View view = LayoutInflater.from(mainActivity).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView qltyText = view.findViewById(R.id.qlty_text);
            TextView aqiText = view.findViewById(R.id.AQI_text);
            TextView mainText = view.findViewById(R.id.main_text);

            dateText.setText(airForecast.date.substring(5));
            qltyText.setText(airForecast.qlty);
            aqiText.setText(airForecast.aqi);
            mainText.setText(airForecast.main);
            forecastLayout.addView(view);
*/


            airLayout.setVisibility(View.VISIBLE);
            Intent intent = new Intent(mainActivity, AutoUpdateService.class);
            mainActivity.startService(intent);
    }

    protected void showForecast(Huanbaobu huanbaobu) {

        forecastLayout.removeAllViews();
        for (HuanbaobuData huanbaobuData : huanbaobu.data) {
            View view = LayoutInflater.from(mainActivity).inflate(R.layout.forecast_item, forecastLayout, false);

            fdateText = (TextView) view.findViewById(R.id.date_text);
            qltyText = (TextView) view.findViewById(R.id.qlty_text);
            faqiText = (TextView) view.findViewById(R.id.AQI_text);
            fmainText = (TextView) view.findViewById(R.id.main_text);

            fdateText.setText(huanbaobuData.forecast_day);
            qltyText.setText(huanbaobuData.level);
            faqiText.setText(huanbaobuData.aqi);
            fmainText.setText(huanbaobuData.first_pollutant);

            forecastLayout.addView(view);
        }
        airLayout.setVisibility(View.VISIBLE);
    }

    private void loadBingPic(){
        String bingPicUrl = "http://cn.bing.com/HPImageArchive.aspx?format=js&n=1";
        HttpUtil.sendOkHttpRequest(bingPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String bingPic = Utility.handleBingPicResponse(response.body().string());
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mainActivity).load(bingPic).into(bingPicImg);
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
