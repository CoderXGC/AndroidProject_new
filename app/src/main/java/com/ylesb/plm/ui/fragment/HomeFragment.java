package com.ylesb.plm.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.gyf.immersionbar.ImmersionBar;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.ylesb.base.FragmentPagerAdapter;
import com.ylesb.plm.R;
import com.ylesb.plm.app.AppFragment;
import com.ylesb.plm.app.TitleBarFragment;
import com.ylesb.plm.manager.ActivityManager;
import com.ylesb.plm.ui.activity.HomeActivity;
import com.ylesb.plm.ui.adapter.TabAdapter;
import com.ylesb.plm.utils.AMapLocationListeners;
import com.ylesb.plm.widget.XCollapsingToolbarLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import static com.tencent.bugly.Bugly.applicationContext;
import static com.tencent.map.geolocation.TencentLocationRequest.REQUEST_LEVEL_POI;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2018/10/18
 *    desc   : 首页 Fragment
 */
public final class HomeFragment extends TitleBarFragment<HomeActivity>
        implements TabAdapter.OnTabListener, ViewPager.OnPageChangeListener,
        XCollapsingToolbarLayout.OnScrimsListener ,TencentLocationListener{

    private static final int MY_PERMISSION_REQUEST_CODE = 10000;
    private TencentLocationRequest request;//

    private XCollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;

    private TextView mAddressView;
    private TextView mHintView;
    private AppCompatImageView mSearchView;

    private RecyclerView mTabView;
    private ViewPager mViewPager;

    private TabAdapter mTabAdapter;
    private FragmentPagerAdapter<AppFragment<?>> mPagerAdapter;
   //高德看这里https://lbs.amap.com/api/android-location-sdk/guide/create-project/dev-attention
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new MyAMapLocationListener();
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.home_fragment;
    }
    @Override
    protected void initView() {
        mCollapsingToolbarLayout = findViewById(R.id.ctl_home_bar);
        mToolbar = findViewById(R.id.tb_home_title);
        mAddressView = findViewById(R.id.tv_home_address);
        mHintView = findViewById(R.id.tv_home_hint);
        mSearchView = findViewById(R.id.iv_home_search);

        mTabView = findViewById(R.id.rv_home_tab);
        mViewPager = findViewById(R.id.vp_home_pager);

        mPagerAdapter = new FragmentPagerAdapter<>(this);
        mPagerAdapter.addFragment(StatusFragment.newInstance(), "列表演示");
        mPagerAdapter.addFragment(BrowserFragment.newInstance("https://github.com/coderxgc"), "网页演示");
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        mTabAdapter = new TabAdapter(getAttachActivity());
        mTabView.setAdapter(mTabAdapter);
        // 给这个 ToolBar 设置顶部内边距，才能和 TitleBar 进行对齐
        ImmersionBar.setTitleBar(getAttachActivity(), mToolbar);
        //initLocation();
    }



    @Override
    protected void initData() {
       // setLocation();
        init();
        mTabAdapter.addItem("列表演示");
        mTabAdapter.addItem("网页演示");
        mTabAdapter.setOnTabListener(this);

    }

    @Override
    public boolean isStatusBarEnabled() {
        // 使用沉浸式状态栏
        return !super.isStatusBarEnabled();
    }

    @Override
    public boolean isStatusBarDarkFont() {
        return mCollapsingToolbarLayout.isScrimsShown();
    }

    /**
     * {@link TabAdapter.OnTabListener}
     */

    @Override
    public boolean onTabSelected(RecyclerView recyclerView, int position) {
        mViewPager.setCurrentItem(position);
        return true;
    }

    /**
     * {@link ViewPager.OnPageChangeListener}
     */

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        if (mTabAdapter == null) {
            return;
        }
        mTabAdapter.setSelectedPosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    /**
     * CollapsingToolbarLayout 渐变回调
     *
     * {@link XCollapsingToolbarLayout.OnScrimsListener}
     */
    @SuppressLint("RestrictedApi")
    @Override
    public void onScrimsStateChange(XCollapsingToolbarLayout layout, boolean shown) {
        getStatusBarConfig().statusBarDarkFont(shown).init();
        mAddressView.setTextColor(ContextCompat.getColor(getAttachActivity(), shown ? R.color.black : R.color.white));
        mHintView.setBackgroundResource(shown ? R.drawable.home_search_bar_gray_bg : R.drawable.home_search_bar_transparent_bg);
        mHintView.setTextColor(ContextCompat.getColor(getAttachActivity(), shown ? R.color.black60 : R.color.white60));
        mSearchView.setSupportImageTintList(ColorStateList.valueOf(getColor(shown ? R.color.common_icon_color : R.color.white)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewPager.setAdapter(null);
        mViewPager.removeOnPageChangeListener(this);
        mTabAdapter.setOnTabListener(null);
    }

    public void setLocation() {
        request = TencentLocationRequest.create();
        request.setInterval(60000);//设置定位周期(位置监听器回调周期), 单位为 ms (毫秒)
        request.setRequestLevel(REQUEST_LEVEL_POI); // 0:包含经纬度,1:包含经纬度, 位置名称, 位置地址,3:包含经纬度，位置所处的中国大陆行政区划,4:包含经纬度，位置所处的中国大陆行政区划及周边POI列表
        request.setAllowGPS(true);
        request.setAllowDirection(true);
        request.setIndoorLocationMode(true);

        TencentLocationListener listener = this;
        TencentLocationManager locationManager = TencentLocationManager.getInstance(ActivityManager.getInstance().getApplication());
        int error = locationManager.requestLocationUpdates(request, listener);
        if (error == 0) {
            //注册位置监听器成功
        } else {
            //注册位置监听器失败keytool -v -list -keystore
            mAddressView.setText("定位失败");

        }
    }
    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int error, String s) {
            if (TencentLocation.ERROR_OK == error) { // 定位成功
                //定位成功
                if (tencentLocation != null) {
                    String address = tencentLocation.getStreet();
                    mAddressView.setText(address);
                }
            } else { // 定位失败
                mAddressView.setText("定位失败");
               // Tools.ToastTextThread(LocationActivity.this, "定位失败");
            }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }
    public void checkPermission() {
        //检查是否有相应的权限，根据自己需求，进行添加相应的权限
        boolean isAllGranted = checkPermissionAllGranted(
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }
        );
        // 如果这3个权限全都拥有, 则直接执行备份代码
        if (isAllGranted) {
            setLocation();
        } else {
            // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
            ActivityCompat.requestPermissions(this.getAttachActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this.getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 所有的权限都授予
                setLocation();
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
              //  setLocationDialog("");
            }
        }
    }
    public static String sHA1(Context context){
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        byte[] cert = info.signatures[0].toByteArray();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] publicKey = md.digest(cert);
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < publicKey.length; i++) {
            String appendString = Integer.toHexString(0xFF & publicKey[i])
                    .toUpperCase(Locale.US);
            if (appendString.length() == 1)
                hexString.append("0");
            hexString.append(appendString);
            hexString.append(":");
        }
        String result = hexString.toString();
        return result.substring(0, result.length()-1);
    }
    private void init() {
      //  Log.e("sHA1：", sHA1(this.getActivity()));
        AMapLocationClient.updatePrivacyShow(ActivityManager.getInstance().getApplication(),true,true);
        AMapLocationClient.updatePrivacyAgree(ActivityManager.getInstance().getApplication(),true);
        //初始化定位
        try {
            mLocationClient = new AMapLocationClient(ActivityManager.getInstance().getApplication());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(false);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        //mLocationOption.setOnceLocationLatest(true);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }
    private class MyAMapLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //此处获得成功，可以参照返回值表取需要的参数，我只要了省市县
                    Log.e("位置：", aMapLocation.getAddress());
                  /*  Gprovince = aMapLocation.getProvince();
                    Gcity = aMapLocation.getCity();
                    Gdistrict = aMapLocation.getDistrict();*/
                    mAddressView.setText(aMapLocation.getDistrict());
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                    mAddressView.setText(aMapLocation.getErrorCode());
                }
            }
        }

    }

    /**
     * 设置包含隐私政策，并展示用户授权弹窗 <b>必须在AmapLocationClient实例化之前调用</b>
     *
     * @param context
     * @param isContains: 是隐私权政策是否包含高德开平隐私权政策  true是包含
     * @param isShow:     隐私权政策是否弹窗展示告知用户 true是展示
     * @since 5.6.0
     */
    public static void updatePrivacyShow(Context context, boolean isContains, boolean isShow) {

    }

//    /**
//     * 在onCreate()调用即可
//     */
//    private void initLocation() {
//        //添加定位监听
//        AMapLocationListeners aMapLocationListeners = new AMapLocationListeners(ActivityManager.getInstance().getApplication());
//        aMapLocationListeners.setListener(new AMapLocationListeners.LocationListener() {
//            @Override
//            public void success(AMapLocation mapLocation) {
//                // maplocation存放了很多的定位数据，经纬度省市县详细地址都可以从这里拿
//                mAddressView.setText(mapLocation.getProvince());
//            }
//
//            @Override
//            public void error() {
//
//            }
//        });
//        // 将定位监听事件和acitvity生命周期绑定在一起
//       getLifecycle().addObserver(aMapLocationListeners);
//    }
}