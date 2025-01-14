package com.demo.market.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import demo.market.R;
import com.demo.market.activity.AboutUsActivity;
import com.demo.market.activity.BuyHistoryActivity;
import com.demo.market.activity.CartActivity;
import com.demo.market.activity.PersonalActivity;
import com.demo.market.activity.SettingActivity;
import com.demo.market.utils.LoginCheckUtil;
import com.demo.market.utils.SharedPreferencesUtil;

import java.util.Objects;

/**
 * 关于 界面
 */
public class AboutMe extends Fragment {

    private LinearLayout mBtnCart, mBtnBought, mBtnUserInfo, mBtnAbout, mBtnSetting;
    private ImageView mImageView;
    private TextView mName;

    @Override
    public void onResume() {
        Context context = Objects.requireNonNull(getActivity()).getApplicationContext();
        if (LoginCheckUtil.isLogin(context)) {
            //从sp中获取存储的用户昵称
            Object obj = SharedPreferencesUtil
                    .get(context, "userInfo", "username", "新注册用户");
            String name = obj == null ? "" : obj.toString();
            mName.setText(name);

        } else {
            //设置默认名称
            mName.setText("请先登录");
        }
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_me, null);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        //初始化控件并设置点击事件
        mImageView = view.findViewById(R.id.cat_avatar);
        mName = view.findViewById(R.id.cat_title);

        //获得context
        Context context = Objects.requireNonNull(getActivity()).getApplicationContext();
        //设置到默认头像
        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.cutecat));

        //判断是否登录，根据登录情况设置显示用户名
        if (LoginCheckUtil.isLogin(context)) {
            //从sp中获取存储的用户昵称
            Object obj = SharedPreferencesUtil
                    .get(context, "userInfo", "username", "新注册用户");
            String name = obj == null ? "" : obj.toString();

            mName.setText(name);

        } else {
            //设置默认名称
            mName.setText("请先登录");
        }


        //ToolBar
        Toolbar mToolbar = view.findViewById(R.id.toolbar);

        /*菜单们*/
        //购物车按钮
        mBtnCart = view.findViewById(R.id.menu_cart);
        mBtnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginCheckUtil.isLogin(getActivity())) {
                    Intent intent = new Intent(getActivity(), CartActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "您还未登录，请先登录！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //历史购买按钮
        mBtnBought = view.findViewById(R.id.menu_bought);
        mBtnBought.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginCheckUtil.isLogin(getActivity())) {
                    Intent intent = new Intent(getActivity(), BuyHistoryActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "您还未登录，请先登录！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //个人中心按钮
        mBtnUserInfo = view.findViewById(R.id.menu_user);
        mBtnUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginCheckUtil.isLogin(Objects.requireNonNull(getActivity()).getApplicationContext())) {
                    Intent intent = new Intent(getActivity(), PersonalActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "您还未登录！请先登录！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //关于我们按钮
        mBtnAbout = view.findViewById(R.id.menu_about);
        mBtnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(intent);
            }
        });

        //设置按钮
        mBtnSetting = view.findViewById(R.id.menu_setting);
        mBtnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //页面跳转
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //注意本行的FLAG设置
                startActivity(intent);
            }
        });


        return view;
    }
}
