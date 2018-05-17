package com.sail.airq.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.sail.airq.AppGlobal;
import com.sail.airq.MainActivity;
import com.sail.airq.gson.AirMessage;
import com.sail.airq.gson.Weather;
import com.sail.airq.util.HttpUtil;
import com.sail.airq.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by sail on 2018/5/4.
 */

public class MyLocationListener implements BDLocationListener{

    String parcity;
    String city;
    String tmpcity="";
    SharedPreferences sp ;

    @Override
    public void onReceiveLocation(BDLocation location) {
        StringBuffer sb = new StringBuffer(256);

        sb.append("time : ");
        sb.append(location.getTime());    //获取定位时间

        sb.append("\nerror code : ");
        sb.append(location.getLocType());    //获取类型类型

        sb.append("\nlatitude : ");
        sb.append(location.getLatitude());    //获取纬度信息

        sb.append("\nlontitude : ");
        sb.append(location.getLongitude());    //获取经度信息

        sb.append("\nradius : ");
        sb.append(location.getRadius());    //获取定位精准度

        if (location.getLocType() == BDLocation.TypeGpsLocation){

            // GPS定位结果
            sb.append("\nspeed : ");
            sb.append(location.getSpeed());    // 单位：公里每小时

            sb.append("\nsatellite : ");
            sb.append(location.getSatelliteNumber());    //获取卫星数

            sb.append("\nheight : ");
            sb.append(location.getAltitude());    //获取海拔高度信息，单位米

            sb.append("\ndirection : ");
            sb.append(location.getDirection());    //获取方向信息，单位度

            sb.append("\naddr : ");
            sb.append(location.getAddrStr());    //获取地址信息

            sb.append("\ncityname:");           //获取城市
            sb.append(location.getCity());



            sb.append("\ndescribe : ");
            sb.append("gps定位成功");

        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){

            // 网络定位结果
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());    //获取地址信息

            sb.append("\ncitycode:");
            sb.append(location.getCityCode());

            sb.append("\ncity:");
            sb.append(location.getCity());

            sb.append("\noperationers : ");
            sb.append(location.getOperators());    //获取运营商信息

            sb.append("\ndescribe : ");
            sb.append("网络定位成功");

        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

            // 离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");

        } else if (location.getLocType() == BDLocation.TypeServerError) {

            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");

        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {

            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

        }

        sb.append("\nlocationdescribe : ");
        sb.append(location.getLocationDescribe());    //位置语义化信息

        List<Poi> list = location.getPoiList();    // POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
            }


        }

        Log.i("BaiduLocationApiDem", sb.toString());

        //现在已经定位成功，可以将定位的数据保存下来，这里我新建的一个Const类，保存全局静态变量
        AppGlobal.LONGITUDE = location.getLongitude();
        AppGlobal.LATITUDE = location.getLatitude();
        AppGlobal.ADDRESS = location.getAddrStr();
        AppGlobal.CITY = location.getCity();

        if(tmpcity.equals(AppGlobal.CITY)) {


        }else {
            tmpcity = AppGlobal.CITY;
            selectArea();
        }
    }

    public void selectArea(){
        String airUrl = "https://free-api.heweather.com/s6/weather?location="+AppGlobal.LATITUDE+","+AppGlobal.LONGITUDE+"&key=047ec486703a4d408ce358b46bf14fa5";
        Log.v("MyLocation", "request: "+airUrl);

        HttpUtil.sendOkHttpRequest(airUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.v("AirQ:airActivity", "onResponse: "+responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);

                        if (weather!=null && "ok".equals(weather.status)) {
                            parcity = weather.basic.parentcity;
                            city = weather.basic.city;
                            AppGlobal.LOCALPARCITY  =parcity;
                            AppGlobal.LOCALCITY = city;

                            Log.v("AppGlobal:",AppGlobal.LOCALCITY );
                        }
            }
        });

    }
}
