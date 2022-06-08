package com.demo.market.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import demo.market.R;
import com.demo.market.activity.DetailsActivity;
import com.demo.market.activity.SearchViewActivity;
import com.demo.market.adapter.GlideImageLoader;
import com.demo.market.adapter.HomePageGridViewAdapter;
import com.demo.market.adapter.HomePageViewPagerAdapter;
import com.demo.market.adapter.HotGridViewAdapter;
import com.demo.market.adapter.RecommendGridViewAdapter;
import com.demo.market.constant.Constants;
import com.demo.market.models.CommodityModel;
import com.demo.market.models.CommodityTypeModel;
import com.demo.market.utils.ListUtil;
import com.demo.market.utils.NetUtil;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 首页
 *
 */

public class HomePage extends Fragment {
    /**
     * 轮播
     */
    private Banner banner;

    /**
     * GridView小圆点指示器
     */
    private ViewGroup points;

    /**
     * 小圆点指示器图片集合
     */
    private ImageView[] ivPoints;

    /**
     * viewPager
     */
    private ViewPager viewPager;

    /**
     * 总的页数
     */
    private int totalPage = 0;

    /**
     * 每页显示的最大数量
     */
    private int mPageSize = 10;

    /**
     * 商品数据源
     */
    private List<CommodityTypeModel> listData = new ArrayList<>();

    /**
     * GridView作为一个View对象添加到ViewPager集合中
     */
    private List<View> viewPagerList;

    /**
     * 热评产品
     */
    private GridView hotProductGridView;

    /**
     * 推荐产品
     */
    private GridView recommendProductGridView;

    /**
     * 热评和推荐商品的两个进度条
     */
    private ProgressBar hotProgressBar, recommendProgressBar;

    private HotGridViewAdapter hotProductGridViewAdapter;
    private RecommendGridViewAdapter recommendProductGridViewAdapter;

    private SearchView searchView;
    private ArrayList<String> imageList = new ArrayList<>();

    /**
     * 一次请求到分页商品数据
     */
    private List<CommodityModel> listItem = new ArrayList<>();

    /**
     * 热评商品List
     */
    private List<CommodityModel> listItemHot = new ArrayList<>();

    /**
     * 推荐商品List
     */
    private List<CommodityModel> listItemRecommend = new ArrayList<>();

    /**
     * 下拉刷新的layout
     */
    private SwipeRefreshLayout mRefreshLayout;
    /**
     * 为了刷新不增加小点增加的记录的变量
     */
    private int firstFlag = 0;

    /**
     * 定义一个自己的context
     */
    private Context mContext;

    /**
     * 用于从网络初始化UI的handler
     */
    @SuppressLint("HandlerLeak")
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    //网络请求失败
                    Toast.makeText(mContext, "网络请求失败！获取商品类别失败！", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    //网络请求成功，但是返回状态为失败
                    Toast.makeText(mContext, msg.obj == null ? "获取商品数据失败！" : msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    //网络请求商品类别返回成功时，初始化控件
                    initViewPage();
                    break;
                case 2:
                    //随机两款商品作为热销商品
                    listItemHot = ListUtil.getRandomList(listItem, 2);
                    //模拟获取热评商品
                    hotProductGridViewAdapter = new HotGridViewAdapter(HomePage.this.getContext(), listItemHot);
                    hotProductGridView.setAdapter(hotProductGridViewAdapter);
                    //设置gridview点击事件
                    hotProductGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), DetailsActivity.class);
                            //将对应的产品id传到详情界面
                            intent.putExtra("id", listItemHot.get(position).getId() + "");
                            startActivity(intent);
                        }
                    });

                    //停止显示热评商品加载动画，显示商品信息
                    hotProgressBar.setVisibility(View.GONE);
                    hotProductGridView.setVisibility(View.VISIBLE);

                    //随机两款商品作为热销商品
                    listItemRecommend = ListUtil.getRandomList(listItem, 2);
                    //模拟获取推荐商品
                    recommendProductGridViewAdapter = new RecommendGridViewAdapter(HomePage.this.getContext(), listItemRecommend);
                    recommendProductGridView.setAdapter(recommendProductGridViewAdapter);
                    //设置gridView点击事件
                    recommendProductGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), DetailsActivity.class);
                            //将对应的产品id传到详情界面
                            intent.putExtra("id", listItemRecommend.get(position).getId() + "");
                            startActivity(intent);
                        }
                    });

                    //停止显示热评商品加载动画，显示商品信息
                    recommendProgressBar.setVisibility(View.GONE);
                    recommendProductGridView.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

            //判断刷新动画
            if (mRefreshLayout.isRefreshing()) {
                //停止动画
                mRefreshLayout.setRefreshing(false);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homepage, null);
        //        初始化控件
        initView(view);

        //转存context
        mContext = Objects.requireNonNull(getActivity()).getApplicationContext();

        //判断网络状态，并初始化数据
        if (NetUtil.isNetworkAvailable(Objects.requireNonNull(getContext()))) {
            //获取分类
            initCategoryClassification();
            //加载轮播图
            initBanner();
            //获取热评商品
            initCommodityList(Constants.HOME_PAGE_LIMIT, 0, true);
        } else {
            Toast.makeText(getActivity().getApplicationContext()
                    , "当前网络环境不正常，商品 信息可能无法显示"
                    , Toast.LENGTH_LONG).show();
        }

        //设置searchView点击事件
        setSearchViewIntent();

        // 刷新监听。
        mRefreshLayout.setOnRefreshListener(mRefreshListener);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 初始化广告页
     */
    private void initBanner() {
        //清空旧数据
        imageList.clear();
        imageList.add("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic2.zhimg.com%2Fv2-0726d82f1ec776c5805ffa9eade09644_1440w.jpg%3Fsource%3D172ae18b&refer=http%3A%2F%2Fpic2.zhimg.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1657279729&t=84b999d85b7f8fd31926d0bcce7308ec");
        imageList.add("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg1.mydrivers.com%2Fimg%2F20220104%2Fs_cc37963b7b744bd586c15b311d6f430c.jpg&refer=http%3A%2F%2Fimg1.mydrivers.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1657279688&t=be41454fefbbb122d74dac78fb958b4a");
        banner.setImages(imageList);
        banner.start();
    }

    /**
     * 刷新
     */
    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            //获取分类
            initCategoryClassification();
            //加载轮播图
            initBanner();
            //获取商品分页数据
            initCommodityList(Constants.HOME_PAGE_LIMIT, 0, true);
        }
    };

    /**
     * 获取商品分页数据
     *
     * @param limit        拉取数据条数（首页建议为6）
     * @param page         页码
     * @param isRandomPage 是否采用随机页码
     */
    private void initCommodityList(int limit, int page, boolean isRandomPage) {
        //初始化随机页码
        int randomPage = (int) (Math.random() * (Constants.MAX - Constants.MIN)) + Constants.MIN;

        //构造请求URL
        String url = Constants.BASE_URL + Constants.GET_COMMODITY_URL
                + "?limit=" + limit;

        //判断是否需要随机生成页码
        if (isRandomPage) {
            url += "&page=" + randomPage;
        } else {
            url += "&page=" + page;
        }

        NetUtil.doGet(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("TAG", "错误信息：" + e.toString());
                        Message message = Message.obtain();
                        message.what = -1;
                        uiHandler.sendMessage(message);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string().trim();
                        com.alibaba.fastjson.JSONObject object = JSON.parseObject(result);

                        Message message = Message.obtain();

                        if (object.getBoolean("flag")) {
                            com.alibaba.fastjson.JSONObject data = (com.alibaba.fastjson.JSONObject) object.get("data");
                            com.alibaba.fastjson.JSONArray array = data.getJSONArray("lists");
                            //获得商品list
                            listItem = com.alibaba.fastjson
                                    .JSONArray.parseArray(array.toString(), CommodityModel.class);
                            message.what = 2;
                        } else {
                            message.what = 0;
                        }

                        uiHandler.sendMessage(message);
                    }
                }
        );
    }

    /**
     * 初始化GridView
     */
    private void initViewPage() {
        LayoutInflater inflater2 = LayoutInflater.from(this.getContext());
        //总的页数，取整（这里有三种类型：Math.ceil(3.5)=4:向上取整，只要有小数都+1  Math.floor(3.5)=3：向下取整  Math.round(3.5)=4:四舍五入）
        //这个被tjs改过
        //if(totalPage>0){
        totalPage = (int) Math.ceil(listData.size() * 1.0 / mPageSize);
        //}

        viewPagerList = new ArrayList<>();
        for (int i = 0; i < totalPage; i++) {
            //每个页面都是inflate出一个新实例
            GridView gridView = (GridView) inflater2.inflate(R.layout.gridview_layout, viewPager, false);
            gridView.setAdapter(new HomePageGridViewAdapter(this.getContext(), listData, i, mPageSize));
            //每一个GridView作为一个View对象添加到ViewPager集合中
            viewPagerList.add(gridView);
        }
        //设置ViewPager适配器
        viewPager.setAdapter(new HomePageViewPagerAdapter(viewPagerList));
        //小圆点指示器
        if (firstFlag == 0) {
            ivPoints = new ImageView[totalPage];
            for (int i = 0; i < ivPoints.length; i++) {
                ImageView imageView = new ImageView(getContext());
                //设置图片的宽高
                imageView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));
                if (i == 0) {
                    imageView.setBackgroundResource(R.drawable.page__selected_indicator);
                } else {
                    imageView.setBackgroundResource(R.drawable.page__normal_indicator);
                }
                ivPoints[i] = imageView;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                layoutParams.leftMargin = 10;//设置点点点view的左边距
                layoutParams.rightMargin = 10;//设置点点点view的右边距
                points.addView(imageView, layoutParams);
            }
        }
        if (totalPage > 0) {
            firstFlag++;
        }


        //设置ViewPager滑动监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //改变小圆圈指示器的切换效果
                setImageBackground(position);
//                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 初始化获取商品分类
     */
    private void initCategoryClassification() {
        String categoryTypeUrl = Constants.BASE_URL + Constants.GET_COMMODITY_TYPES_URL;
        NetUtil.doGet(categoryTypeUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("网络请求失败！", "网络请求失败！获取商品类别列表失败！");
                Message message = Message.obtain();
                message.what = -1;
                uiHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string().trim();
                com.alibaba.fastjson.JSONObject obj = JSON.parseObject(result);

                Message message = Message.obtain();
                if (obj.getBoolean("flag")) {
                    message.what = 1;
                    //获得商品类型数据
                    List<CommodityTypeModel> types = com.alibaba.fastjson.JSONArray
                            .parseArray(JSON.toJSONString(obj.get("data"))
                                    , CommodityTypeModel.class);
                    listData = types;
                } else {
                    message.what = 0;
                }

                message.obj = obj.getString("message");
                uiHandler.sendMessage(message);
            }
        });
    }


    /**
     * 初始化绑定控件
     *
     * @param view view
     */
    public void initView(View view) {

        mRefreshLayout = view.findViewById(R.id.refresh_layout);
        viewPager = view.findViewById(R.id.viewPager);
        //初始化小圆点指示器
        points = view.findViewById(R.id.points);

        //绑定两个进度条
        hotProgressBar = view.findViewById(R.id.hot_progress);
        recommendProgressBar = view.findViewById(R.id.recommend_progress);


        banner = view.findViewById(R.id.banner);
        //设置banner样式
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());


        hotProductGridView = view.findViewById(R.id.hot_gridview);
        recommendProductGridView = view.findViewById(R.id.recommend_gridview);
        searchView = view.findViewById(R.id.home_serachview);

    }

    /**
     * searchView点击跳转
     */
    private void setSearchViewIntent() {
        searchView.setFocusable(false);
        searchView.clearFocus();

        //设置搜索框焦点
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    searchView.clearFocus();
                else {
                    Intent intent = new Intent(getActivity(), SearchViewActivity.class);
                    intent.putExtra("fatherName", ".HomePage");
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    /**
     * 设置GridView图片背景
     *
     * @param selectItems 选中项index
     */
    private void setImageBackground(int selectItems) {
        for (int i = 0; i < ivPoints.length; i++) {
            if (i == selectItems) {
                ivPoints[i].setBackgroundResource(R.drawable.page__selected_indicator);
            } else {
                ivPoints[i].setBackgroundResource(R.drawable.page__normal_indicator);
            }
        }
    }
}
