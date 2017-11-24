package com.yixitongda.amapdemo;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.yixitongda.amapdemo.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW;
import static com.amap.api.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;

public class MainActivity extends AppCompatActivity {
    private BottomSheetDialog mBottomSheetDialog;

    private static final String TAG = "chen debug";
    private AMapLocationClient locationClient;
    //用于定位模式和相关参数的设置
    private AMapLocationClientOption aMapLocationClientOption;
    //定位的监听器
    private AMapLocationListener listener;

    private MapView mMapView = null;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private UiSettings uiSettings;
    private Button zoom_bt, zoom_inbt, amap_Marker;
    private ArrayList<LatLng> latLngs;
    private MarkerOptions options;
    private Marker marker;
    private ArrayList<Marker> markers;
    private boolean isFirst = true;
    private final ImageView imageView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtils.setFullScreen(this);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);

        zoom_bt = (Button) findViewById(R.id.amap_change_zoom);
        zoom_inbt = (Button) findViewById(R.id.amap_change_zoomin);
        amap_Marker = (Button) findViewById(R.id.amap_marker);

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //获取Amap对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        //1.改变地图的缩放级别和中心点
        changeAmapConfig();

        //控件交互
        uiSettings = aMap.getUiSettings();


        initAmap();
        initLocation();
        initConfig();
        initGongju();


        mMapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e(TAG, "正在处理这个点击事件");
                return false;
            }
        });

        mMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "正在处理点击事件");
            }
        });

    }

    private void initGongju() {
        Log.e(TAG, "initGongju: ");
        zoom_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "正在测试点击事件,改变地图的中心点");
                //这里我们重新定义了一个CameraUpdate  这里应该可以重新定义多个CameraUpdate  然后调用Amap对象的方法
                CameraUpdate cameraUpdate_zoom = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(30.5460000828, 114.2937755585), 18, 0, 0));
                aMap.moveCamera(cameraUpdate_zoom);
            }
        });

        zoom_inbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "正在测试改变地图的缩放级别");
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(10);
                aMap.moveCamera(cameraUpdate);
            }
        });

        amap_Marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMarker();
            }
        });

        //设置Marker点击的回调数据
        aMap.setOnMarkerClickListener(markerClickListener);
    }

    private void changeAmapConfig() {
        Log.e(TAG, "正在测试改变地图更新");
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(18);
        aMap.moveCamera(cameraUpdate);
    }

    /**
     * 控制地图的UI控件
     */
    private void initConfig() {
        uiSettings.setScaleControlsEnabled(true);//开启比例尺控件
        uiSettings.setZoomControlsEnabled(false);
    }

    /**
     * 开始定位的地图请求
     */
    private void initLocation() {
        //用于接受异步返回的结果
        listener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //可在其中解析amapLocation获取相应内容。
                        Log.i(TAG, "城市信息:" + aMapLocation.getCity() + "\n经纬度消息:" + aMapLocation.getLatitude() + "\n" + aMapLocation.getLongitude() + "\n"
                                + "城区消息:" + aMapLocation.getDistrict() + "\n" + "城市编码:" + aMapLocation.getCityCode() + "\n" + "POI消息:" + aMapLocation.getPoiName());

                    } else {
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.i("AmapError", "location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                    if (isFirst) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 15, 0, 0));
                        aMap.animateCamera(cameraUpdate, 3, new AMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                Log.e(TAG, "成功完成定位中心的回调");
                                isFirst = false;
                            }

                            @Override
                            public void onCancel() {
                                Log.e(TAG, "onCancel: ");
                            }
                        });
                    } else {
                        //     Log.e(TAG, "onLocationChanged: 不是第一次了");
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
        //定位的模式出现问题  我们需要更改定位的模式
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.amap_location_xiaolandain));
        myLocationStyle.strokeColor(Color.parseColor("#00FF0000"));
        myLocationStyle.radiusFillColor(Color.parseColor("#00ffffff"));
        myLocationStyle.strokeWidth(0);
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        //  myLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        /**
         * 为地图开启室内地图
         *
         */
        aMap.showIndoorMap(true);


        /**
         * 添加点标记  也就是我们的优惠券的图片
         */

        latLngs = new ArrayList<>();
        markers = new ArrayList<>();
        initDate();

        for (int i = 0; i < latLngs.size(); i++) {
            options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.con_youhuiquan_moren_1));
            options.position(latLngs.get(i));
            marker = aMap.addMarker(options);
            markers.add(marker);
        }


    }

    private void initDate() {
        latLngs.add(0, new LatLng(22.5332104597, 113.9359474182));
        latLngs.add(1, new LatLng(22.5406227692, 113.9461827278));
        latLngs.add(2, new LatLng(22.5388787321, 113.9477705956));
        latLngs.add(3, new LatLng(22.5374517764, 113.9453673363));
        latLngs.add(4, new LatLng(22.5340230585, 113.9463114738));
        latLngs.add(5, new LatLng(22.5328537075, 113.9433503151));
        latLngs.add(6, new LatLng(22.5404840397, 113.9401531219));
        latLngs.add(7, new LatLng(22.5403056732, 113.9346170425));
        latLngs.add(8, new LatLng(22.5369959402, 113.9536929131));
        latLngs.add(9, new LatLng(22.5318032651, 113.9449596405));
        latLngs.add(10, new LatLng(22.5419109642, 113.9424061775));

    }

    private void clearMarker() {
        if (aMap != null) {
            for (int i = 0; i < markers.size(); i++) {
                markers.get(i).destroy();
            }
        }
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
        Log.e(TAG, "onResume重新开始地图的绘制 ");
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
        Log.e(TAG, "onSaveInstanceState: ");
        mMapView.onSaveInstanceState(outState);
    }

    AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
        // marker 对象被点击时回调的接口
        // 返回 true 则表示接口已响应事件，否则返回false
        @Override
        public boolean onMarkerClick(Marker marker) {
            Log.e(TAG, "成功触发方法");
            LatLng latLng = marker.getPosition();
            Log.e(TAG, "这个点的Mareker的数据:" + latLng.latitude + "\n" + latLng.longitude);
            testwindow();
            return true;
        }
    };


    /**
     * share Dialog
     */
    private void showShareDialog() {
        if (mBottomSheetDialog == null) {
            mBottomSheetDialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_share_dialog, null);

            mBottomSheetDialog.setContentView(view);
            mBottomSheetDialog.setCancelable(true);
            mBottomSheetDialog.setCanceledOnTouchOutside(true);
            // 解决下滑隐藏dialog 后，再次调用show 方法显示时，不能弹出Dialog
            View view1 = mBottomSheetDialog.getDelegate().findViewById(android.support.design.R.id.design_bottom_sheet);
            final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(view1);
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        Log.i("BottomSheet", "onStateChanged");
                        mBottomSheetDialog.dismiss();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
        } else {
            mBottomSheetDialog.show();
        }

    }


    private void testwindow() {
        Log.e(TAG, "testwindow: ");
        final ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.mipmap.amap_youhuiquan);

//        Button button = new Button(this);
//        button.setText("floating button");
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSLUCENT);
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //尝试一下flag属相是否可以解决焦点的问题  确实可以解决焦点的问题
//        params.flags=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
//                 WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //  params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

        params.gravity = Gravity.BOTTOM;
        params.y=dptopx(57);
        //params.windowAnimations=R.anim.amap_float_animation;


        final WindowManager windowManager = getWindowManager();
        Log.e(TAG, "windowManager:" + windowManager.toString());
        windowManager.addView(imageView, params);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "测试删除这个有WindowManager添加的View");

            }
        });



        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                Log.e(TAG,"地图的触摸事件");
                windowManager.removeView(imageView);
            }
        });
    }


    private int dptopx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

}
