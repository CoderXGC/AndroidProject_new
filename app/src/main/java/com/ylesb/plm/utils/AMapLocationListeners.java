package com.ylesb.plm.utils;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.ylesb.plm.manager.ActivityManager;

import java.util.List;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class AMapLocationListeners implements LifecycleObserver {


    private Context mContext;
    public AMapLocationListeners(Context context) {
        this.mContext = context;
    }

    private AMapLocationClient locationClient;
    //声明mLocationOption对象
    private AMapLocationClientOption locationOption = null;

    private LocationListener listener;

    public void setListener(LocationListener listener) {
        this.listener = listener;
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void createLocation(){
        initLocation();
        startLocation();

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void destroyLocal(){
        destroyLocation();
    }

    private void initLocation() {

        //初始化client
        try {
            locationClient = new AMapLocationClient(ActivityManager.getInstance().getApplication());
        } catch (Exception e) {
            e.printStackTrace();
        }
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(5 * 1000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        //mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    /**
     * 定位监听
     */
    com.amap.api.location.AMapLocationListener locationListener = new com.amap.api.location.AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {

                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    if (listener != null) {
                        listener.success(location);
                        // 如果需要一直获取定位监听权限的话，就把下面这一行注释掉
                        locationClient.stopLocation();
                    }
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                    listener.error();
                }
            }
        }
    };

    private void startLocation() {

                        locationClient.startLocation();


    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            //locationClient.disableBackgroundLocation(true);
            locationClient.stopLocation();
            locationClient.unRegisterLocationListener(locationListener);
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    public interface LocationListener{
        void success(AMapLocation mapLocation);

        void error();
    }

    /**
     * 回到自己的定位
     */
//    public void moveTolocation(AMap aMap) {
//        if (aMap != null && myLatlng != null) {
//            //将地图移动到定位点
//            aMap.moveCamera(CameraUpdateFactory.changeLatLng(myLatlng));
//        }
//    }

}

