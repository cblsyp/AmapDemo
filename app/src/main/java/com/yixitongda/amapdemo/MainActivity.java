package com.yixitongda.amapdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.yixitongda.amapdemo.utils.StatusBarUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AMapLocationClient locationClient;
    //用于定位模式和相关参数的设置
    private AMapLocationClientOption aMapLocationClientOption;
    //定位的监听器
    private AMapLocationListener listener;

    private MapView mMapView = null;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private UiSettings uiSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtils.setFullScreen(this);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //获取Amap对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        //控件交互
        uiSettings = aMap.getUiSettings();

        initAmap();
        initLocation();


    }

    private void initLocation() {
     //用于接受异步返回的结果
        listener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
     //可在其中解析amapLocation获取相应内容。
                        Log.e(TAG, "城市信息:" + aMapLocation.getCity() + "\n经纬度消息:" + aMapLocation.getLatitude() + "\n" + aMapLocation.getLongitude()+"\n"
                        +"城区消息:"+aMapLocation.getDistrict()+"\n"+"城市编码:"+aMapLocation.getCityCode()+"\n"+"POI消息:"+aMapLocation.getPoiName());

                    } else {
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError", "location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                }
            }
        };
        //全局变量的context  官网推荐的
        locationClient = new AMapLocationClient(this);
        locationClient.setLocationListener(listener);

        aMapLocationClientOption = new AMapLocationClientOption();
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
       // aMapLocationClientOption.setMockEnable(true);

        locationClient.setLocationOption(aMapLocationClientOption);
        //开始定位请求
        locationClient.startLocation();
    }

    private void initAmap() {
        Log.e(TAG, "正在进行定位小蓝点的初始化");
        //定位蓝点的设置   蓝点样式设计  蓝点设置 蓝点开启
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        //  myLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        locationClient.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        Log.e(TAG, "onResume重新开始地图的绘制 " );
        locationClient.startLocation();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        Log.e(TAG, "onPause中暂停地图的绘制");
        locationClient.stopLocation();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
}
