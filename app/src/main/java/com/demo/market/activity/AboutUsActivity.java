package com.demo.market.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;

import demo.market.R;

/**
 * 关于我们Activity
 */
public class AboutUsActivity extends AppCompatActivity {
    /**
     * 返回按钮
     */
    private ImageView mBtnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        //初始化绑定控件
        init();
    }

    /**
     * 初始化绑定控件
     */
    private void init() {
        mBtnBack = findViewById(R.id.back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
