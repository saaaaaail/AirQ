package com.sail.airq;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.sail.airq.gson.AirMessage;

import com.sail.airq.gson.JvHeData;
import com.sail.airq.gson.LifeStyle;
import com.sail.airq.gson.Weather;
import com.sail.airq.service.AutoUpdateService;
import com.sail.airq.util.HttpUtil;
import com.sail.airq.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class HisFragment extends Fragment {


    SwipeRefreshLayout.OnRefreshListener refreshListener;
    SharedPreferences prefs;

    private MainActivity mActivity;

    public SwipeRefreshLayout hisSwipeRefresh;
    private String currentCity;
    private String currentParCity;
    private Button navButton;
    private Button menuButton;
    List<Integer> aqiIList = new ArrayList<>();
    List<String> aqiSList = new ArrayList<>();
    List<String> dateList = new ArrayList<>();

    private int totalDays = 20;//总共有多少天的数据显示
    private float minY = 0f;//Y轴坐标最小值
    private float maxY = 100f;//Y轴坐标最大值

    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisValues = new ArrayList<AxisValue>();

    LineChartView lineChart;
    LineChartData data;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_his,container,false);
        Log.v("FragmentHis", "initViews: ");


        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*
        currentCity = savedInstanceState.getString("current_city");
        currentParCity = savedInstanceState.getString("current_parcity");

        */
        Log.v("HisFragOnActivity:","11");
        aqiIList.clear();
        aqiSList.clear();
        dateList.clear();
        mAxisValues.clear();
        mPointValues.clear();

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        currentCity = prefs.getString("current_city",null);
        currentParCity = prefs.getString("current_parcity",null);
        Log.v("HisFragment:",currentCity);

        requestHis(currentParCity);

    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString("current_city",currentCity);
        outState.putString("current_parcity",currentParCity);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof MainActivity){
            mActivity = (MainActivity)activity;

            menuButton = (Button)mActivity.findViewById(R.id.title_menu);
            //mtitleCity = (TextView)mActivity.findViewById(R.id.title_city);
           // bingPicImg = (ImageView)mActivity.findViewById(R.id.bing_pic_img);

        }

    }

    @Override
    public void onHiddenChanged(boolean hidd) {
        if (hidd) {
            //隐藏时所作的事情

        } else {
            //显示时所作的事情
            menuButton.setVisibility(View.GONE);
            aqiIList.clear();
            aqiSList.clear();
            dateList.clear();
            mAxisValues.clear();
            mPointValues.clear();
            currentCity = prefs.getString("current_city",null);
            currentParCity = prefs.getString("current_parcity",null);
            requestHis(currentParCity);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("HisFragOnResume:","11");

    }

    protected void initViews(View view){

        Log.v("FragmentNow", "initViews: ");

        //mainActivity = (MainActivity) getActivity();

        lineChart = (LineChartView)view.findViewById(R.id.line_chart);

        hisSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.his_swipe_refresh);
        hisSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        menuButton.setVisibility(View.INVISIBLE);
        refreshListener = new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                Log.v("His:OnRefresh:","11");
                aqiIList.clear();
                aqiSList.clear();
                dateList.clear();
                mAxisValues.clear();
                mPointValues.clear();

                prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                currentCity = prefs.getString("current_city",null);
                currentParCity = prefs.getString("current_parcity",null);
                requestHis(currentParCity);



                hisSwipeRefresh.setRefreshing(false);
            }
        };

        hisSwipeRefresh.setOnRefreshListener(refreshListener);

    }

    public void requestHis(String city){
        String Url = "http://web.juhe.cn:8080/environment/air/cityair?city="+city+"&key=1aee5b0387503b8c8645faf7b366e375";

        HttpUtil.sendOkHttpRequest(Url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mActivity,"获取信息失败，请检查网络",Toast.LENGTH_SHORT);
                        hisSwipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.v("HisFragment:",responseText);

                final JvHeData jvHeData = Utility.handleJvHeResponse(responseText);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (jvHeData!=null){
                            Log.v("HisFragment:","SSSSSSSSS");
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mActivity).edit();
                            editor.putString("jvHeData",responseText);
                            editor.apply();
                            StoreInfo(jvHeData);
                        }else {
                            Toast.makeText(mActivity,"获取空气质量信息失败",Toast.LENGTH_SHORT);
                            prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            String JvHeString = prefs.getString("JvHeData",null);
                            JvHeData jvHeDatatmp =  Utility.handleJvHeResponse(JvHeString);
                            StoreInfo(jvHeDatatmp);
                        }
                        hisSwipeRefresh.setRefreshing(false);
                    }
                });
                }
        });
    }

    protected void StoreInfo(JvHeData jvHe){
        //
        Log.v("Storeinfo",jvHe.lastTwoWeeks.before1day.AQI);
        dateList.add(jvHe.lastTwoWeeks.before1day.date);
        dateList.add(jvHe.lastTwoWeeks.before2day.date);
        dateList.add(jvHe.lastTwoWeeks.before3day.date);
        dateList.add(jvHe.lastTwoWeeks.before4day.date);
        dateList.add(jvHe.lastTwoWeeks.before5day.date);
        dateList.add(jvHe.lastTwoWeeks.before6day.date);
        dateList.add(jvHe.lastTwoWeeks.before7day.date);
        dateList.add(jvHe.lastTwoWeeks.before8day.date);
        dateList.add(jvHe.lastTwoWeeks.before9day.date);
        dateList.add(jvHe.lastTwoWeeks.before10day.date);
        dateList.add(jvHe.lastTwoWeeks.before11day.date);
        dateList.add(jvHe.lastTwoWeeks.before12day.date);
        dateList.add(jvHe.lastTwoWeeks.before13day.date);
        dateList.add(jvHe.lastTwoWeeks.before14day.date);
        dateList.add(jvHe.lastTwoWeeks.before15day.date);
        dateList.add(jvHe.lastTwoWeeks.before16day.date);
        dateList.add(jvHe.lastTwoWeeks.before17day.date);
        dateList.add(jvHe.lastTwoWeeks.before18day.date);
        dateList.add(jvHe.lastTwoWeeks.before19day.date);
        dateList.add(jvHe.lastTwoWeeks.before20day.date);
        dateList.add(jvHe.lastTwoWeeks.before21day.date);
        dateList.add(jvHe.lastTwoWeeks.before22day.date);
        dateList.add(jvHe.lastTwoWeeks.before23day.date);
        dateList.add(jvHe.lastTwoWeeks.before24day.date);
        dateList.add(jvHe.lastTwoWeeks.before25day.date);
        dateList.add(jvHe.lastTwoWeeks.before26day.date);
        dateList.add(jvHe.lastTwoWeeks.before27day.date);
        dateList.add(jvHe.lastTwoWeeks.before28day.date);

        //
        aqiSList.add(jvHe.lastTwoWeeks.before1day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before2day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before3day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before4day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before5day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before6day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before7day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before8day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before9day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before10day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before11day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before12day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before13day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before14day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before15day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before16day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before17day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before18day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before19day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before20day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before21day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before22day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before23day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before24day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before25day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before26day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before27day.AQI);
        aqiSList.add(jvHe.lastTwoWeeks.before28day.AQI);

        //hisLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(mActivity, AutoUpdateService.class);
        mActivity.startService(intent);
        change(aqiSList);

        getAxisLables();
        getAxisPoints();
        initLineChart();
        initSet();
    }

    private void change(List<String> L){

        for(String i:L){
            int tmp = Integer.parseInt(i);
            Integer I = new Integer(tmp);
            aqiIList.add(I);
        }

    }

    private void initLineChart() {
        Line line = new Line(mPointValues).setColor(Color.WHITE).setCubic(false);  //折线的颜色
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.SQUARE）
        line.setCubic(false);//曲线是否平滑
        line.setPointColor(Color.GRAY);
        line.setFilled(false);//是否填充曲线的面积
		line.setHasLabels(true);//曲线的数据坐标是否加上备注
        // line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用直线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示
        lines.add(line);
        data = new LineChartData();
        data.setLines(lines);

    }

    private void initSet(){
        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);
        axisX.setTextColor(Color.WHITE);  //设置字体颜色
        axisX.setName("历史空气质量");  //表格名称

        axisX.setTextSize(9);//设置字体大小
        axisX.setMaxLabelChars(7);  //最多几个X轴坐标
        axisX.setValues(mAxisValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
//	    data.setAxisXTop(axisX);  //x 轴在顶部

        Axis axisY = new Axis();  //Y轴
        axisY.setMaxLabelChars(10); //默认是3，只能看最后三个数字
        axisY.setHasLines(false);
        //axisY.setName("AQI");//y轴标注
        axisY.setTextSize(7);//设置字体大小

        //data.setAxisYLeft(axisY);  //Y轴设置在左边
//	    data.setAxisYRight(axisY);  //y轴设置在右边

        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);

        Viewport v = new Viewport(lineChart.getMaximumViewport());

        v.bottom = minY;
       // v.top = maxY;
        //固定Y轴的范围,如果没有这个,Y轴的范围会根据数据的最大值和最小值决定,这不是我想要的
        lineChart.setMaximumViewport(v);

        //这2个属性的设置一定要在lineChart.setMaximumViewport(v);这个方法之后,不然显示的坐标数据是不能左右滑动查看更多数据的
        v.left = totalDays-6;
        v.right = totalDays - 1;
        lineChart.setCurrentViewport(v);

    }


    /**
     * X 轴的显示
     */
    private void getAxisLables(){
        Log.v("getAxisLables():","11");
        for (int i = 0; i < dateList.size(); i++) {
            Log.v("for",dateList.get(i));
            mAxisValues.add(new AxisValue(i).setLabel(dateList.get(i)));
        }
    }

    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints(){
        Log.v("getAxisPoints():","11");
        for (int i = 0; i < aqiIList.size(); i++) {
            mPointValues.add(new PointValue(i, aqiIList.get(i)));
        }
    }

}
