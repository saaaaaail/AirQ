package com.sail.airq;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sail.airq.gson.AirMessage;
import com.sail.airq.gson.Weather;
import com.sail.airq.location.BDLocationUtils;
import com.sail.airq.util.HttpUtil;
import com.sail.airq.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;
import org.litepal.tablemanager.Connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    SharedPreferences.Editor editor;
    Fragment foundFragment;

    private SharedPreferences prefs;
    private BDLocationUtils bdLocationUtils;
    DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private String currentFragmentTag;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button navButton;
    private Button menuButton;
    private String mCityId;
    TextView titlecity;
    private ImageView bingPicImg;

    private String currentCity;
    private String currentParCity;

    private String localCity;
    private String localParCity;

    private String chooseCity;
    private String chooseParCity;

    private static final String FRAGMENT_NOW = "AirQuality";
    private static final String FRAGMENT_HISTORY = "History";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("Main",FRAGMENT_NOW);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏
        }
        initDB();
        initBDlocation();
        setContentView(R.layout.activity_main);
        initBDpermission();
        initViews(savedInstanceState);
        initFragment();
    }

    protected void initViews(Bundle saveInstanceState){
        fragmentManager = getSupportFragmentManager();
       // nowFragment = (NowFragment)fragmentManager.findFragmentById(R.id.contentLayout);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        navButton = (Button)findViewById(R.id.nav_button);
        menuButton = (Button)findViewById(R.id.title_menu);
        initNavigationViewHeader();


        editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);




        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);//第二个参数是绑定的那个view
                //获取菜单填充器
                MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.main, popup.getMenu());
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.city_choose:
                                Toast.makeText(MainActivity.this, "citychoose", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this,CityChooseActivity.class);
                                startActivityForResult(intent,1);break;
                            case R.id.city_local:
                                Toast.makeText(MainActivity.this, "citylocal", Toast.LENGTH_SHORT).show();
                                Log.v("onitem:",AppGlobal.LOCALCITY);
                                editor.putString("current_city",AppGlobal.LOCALCITY);
                                editor.putString("current_parcity",AppGlobal.LOCALPARCITY);
                                editor.putString("local_city",AppGlobal.LOCALCITY);
                                editor.putString("local_parcity",AppGlobal.LOCALPARCITY);
                                editor.apply();

                                break;
                        }
                        return false;
                    }
                });
                //显示(这一行代码不要忘记了)
                popup.show();

            }
        });
    }

    protected void initFragment(){

        Log.v("Main",FRAGMENT_NOW);
        navigationView.getMenu().getItem(0).setChecked(true);
        switchContent(FRAGMENT_NOW);

    }

    protected void initDB(){
        LitePal.getDatabase();
    }

    protected void initBDlocation(){
        bdLocationUtils = new BDLocationUtils(MainActivity.this);
        bdLocationUtils.doLocation();
    }

    protected void initBDpermission(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }
    }

    private void requestLocation(){

        bdLocationUtils.mLocationClient.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意权限",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AppGlobal.CURRENT_INDEX, currentFragmentTag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    chooseCity = data.getStringExtra("choose_city");
                    chooseParCity = data.getStringExtra("choose_parcity");

                    editor.putString("choose_city",chooseCity);
                    editor.putString("choose_parcity",chooseParCity);
                    editor.putString("current_city",chooseCity);
                    editor.putString("current_parcity",chooseParCity);
                    editor.apply();
                }
                break;
            default:
        }

    }

    protected void initNavigationViewHeader(){
        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.inflateHeaderView(R.layout.drawer_header);
        navigationView.setNavigationItemSelectedListener(new NavigationItemSelected());
    }

    class NavigationItemSelected implements NavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int mitem = item.getItemId();
            switch (mitem){
                case R.id.navigation_item_1:

                    switchContent(FRAGMENT_NOW);
                    break;

                case R.id.navigation_item_2:

                    switchContent(FRAGMENT_HISTORY);
                    break;

                case R.id.navigation_item_settings:
                 //   startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    break;
                case R.id.navigation_item_about:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    break;
            }
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.drawer);
            navigationView.getMenu().findItem(mitem).setChecked(true);
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
    }

    public void switchContent(String name){

        if (currentFragmentTag != null && currentFragmentTag.equals(name))
            return;

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        Fragment currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag);
        if (currentFragment != null) {
            ft.hide(currentFragment);
        }

        foundFragment = fragmentManager.findFragmentByTag(name);

        if (foundFragment == null) {
            switch (name) {
                case FRAGMENT_NOW:
                    foundFragment = new NowFragment();
                    break;
                case FRAGMENT_HISTORY:
                    foundFragment = new HisFragment();
                    break;

            }
        }
        if (foundFragment == null){


        } else if (foundFragment.isAdded()) {
            ft.show(foundFragment);
        } else {
            ft.add(R.id.contentLayout, foundFragment, name);
        }
        ft.commit();
        currentFragmentTag = name;
        Log.v("switchContent", "switchContent: "+ currentFragmentTag);
    }

}
