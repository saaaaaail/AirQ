package com.sail.airq;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sail.airq.util.ShareUtils;


import java.util.Random;


public class AboutActivity extends AppCompatActivity {



    private Button webButton;
    private Button shareButton;
    private Button feedButton;
    private Button udpButton;

    protected Toolbar toolbar;
    private TextView tvVersion;
    private ImageSwitcher imageSwitcher;
    private String[] imageUrls = {
            "http://7xp1a1.com1.z0.glb.clouddn.com/liyu01.png",
            "http://7xp1a1.com1.z0.glb.clouddn.com/liyu02.png",
            "http://7xp1a1.com1.z0.glb.clouddn.com/liyu03.png",
            "http://7xp1a1.com1.z0.glb.clouddn.com/liyu04.png",
            "http://7xp1a1.com1.z0.glb.clouddn.com/liyu05.png"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initViews(savedInstanceState);

    }

    protected void initViews(Bundle savedInstanceState){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        webButton = (Button)findViewById(R.id.btn_web_home);
        feedButton = (Button)findViewById(R.id.btn_feedback);
        shareButton = (Button)findViewById(R.id.btn_share_app);
        udpButton = (Button)findViewById(R.id.btn_check_update);

        tvVersion = (TextView)findViewById(R.id.tv_app_version);
        tvVersion.setText("v"+BuildConfig.VERSION_NAME);
        toolbar = (Toolbar)findViewById(R.id.toolbar) ;
        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(AboutActivity.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                return imageView;
            }
        });
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                R.anim.zoom_in));
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                R.anim.zoom_out));

        webButton.setOnClickListener(new ButtonListener());
        shareButton.setOnClickListener(new ButtonListener());
        feedButton.setOnClickListener(new ButtonListener());
        udpButton.setOnClickListener(new ButtonListener());

        imageSwitcher.post(new Runnable() {
            @Override
            public void run() {
                loadImage();
            }
        });
    }

    protected class ButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_web_home:
                    Uri uri = Uri.parse("https://github.com/saaaaaail/AirQ");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
                case R.id.btn_feedback:
                    Intent intentf = new Intent(Intent.ACTION_SEND);
                    intentf.putExtra(Intent.EXTRA_EMAIL, "649014081@qq.com");
                    intentf.putExtra(Intent.EXTRA_SUBJECT, "反馈");
                    intentf.putExtra(Intent.EXTRA_TEXT, "我有一个问题");
                    startActivity(Intent.createChooser(intentf, "反馈"));
                    break;
                case R.id.btn_check_update:
                    Toast.makeText(AboutActivity.this,"不用更新",Toast.LENGTH_SHORT);
                    break;
                case R.id.btn_share_app:
                    ShareUtils.shareText(AboutActivity.this, "来不及了，赶紧上车！https://github.com/saaaaaail/AirQ", "分享到");
                    break;
            }
        }
    }


    private void loadImage() {
        Glide.with(this).load(imageUrls[new Random().nextInt(5)]).into(new SimpleTarget<GlideDrawable>(imageSwitcher.getWidth(), imageSwitcher.getHeight()) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                imageSwitcher.setImageDrawable(resource);
            }
        });
    }


}
