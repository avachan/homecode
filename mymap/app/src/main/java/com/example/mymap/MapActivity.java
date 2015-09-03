package com.example.mymap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionResult.SuggestionInfo;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.List;

//author zb,qq954884146
public class MapActivity extends Activity {
    MapView mMapView = null;
    BaiduMap mBaiduMap;
    boolean isFirstLoc = true;
    private String address;
    private String city;
    GeoCoder mSearch;
    private EditText et_Conaddress;
    private ListView lv_ListView;
    private Button confirm;
    private TextView tv_Longitude;
    private TextView tv_Latitude;
    private Button btn_Search;
    private double latitudeValue, longitudeValue;
    SuggestionSearch mSuggestionSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext,注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        btn_Search = (Button) findViewById(R.id.Btn_Search);
        lv_ListView = (ListView) findViewById(R.id.Lv_ListView);

        et_Conaddress = (EditText) findViewById(R.id.Et_Conaddress);
        et_Conaddress.setText(address);
        tv_Longitude = (TextView) findViewById(R.id.Tv_Longitude);
        tv_Latitude = (TextView) findViewById(R.id.Tv_Latitude);
        // 获取地图控件引用
        mMapView = (MapView) findViewById(R.id.BigMapView);
        mBaiduMap = mMapView.getMap();
        mSearch = GeoCoder.newInstance();
        // 设置地图初始Zoom大小
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17.0f));
        mMapView.showScaleControl(false);    // 隐藏比例尺
        mMapView.showZoomControls(false);    // 隐藏缩放按钮
        mMapView.removeViewAt(1);           // 隐藏百度logo
        // 开启定位图层
        // mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        // popWin(new LatLng(latitudeValue, longitudeValue));
        // mSearch.geocode(new GeoCodeOption().city("").address(address));
        LocationClient mLocClient = new LocationClient(this);
        MyLocationListenner myListener = new MyLocationListenner();
        mSuggestionSearch = SuggestionSearch.newInstance();
        mLocClient.registerLocationListener(myListener);
        mSearch.setOnGetGeoCodeResultListener(listener);
        mSuggestionSearch.setOnGetSuggestionResultListener(listener_advice);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setAddrType("all");
        mLocClient.setLocOption(option);
        mLocClient.start();
        OnMapClickListener listener = new OnMapClickListener() {
            /**
             * 地图单击事件回调函数
             *
             * @param point
             *            点击的地理坐标
             */
            public void onMapClick(LatLng point) {
                LatLng ptCenter = point;
                // 反Geo搜索
                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(ptCenter));

                popWin(point);
            }

            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                // TODO 自动生成的方法存根
                return false;
            }
        };
        mBaiduMap.setOnMapClickListener(listener);

        btn_Search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (et_Conaddress.getText().toString().trim().length() == 0)
                    // mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(13.0f));
                    Toast.makeText(MapActivity.this, "请输入您要定位的地址",
                            Toast.LENGTH_SHORT).show();

                else if (et_Conaddress.getText().toString().trim().length() > 3) {
                    mSearch.geocode(new GeoCodeOption().city("").address(
                            et_Conaddress.getText().toString()));
                    setListView(et_Conaddress.getText().toString() + "");
                } else {
                    Toast.makeText(MapActivity.this, "请输入您要定位的详细地址",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mSearch.destroy();
        mSuggestionSearch.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            address = location.getAddrStr();
            city = location.getCity();
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
                et_Conaddress.setText(location.getAddrStr());

                tv_Longitude.setText(location.getLongitude() + "");
                tv_Latitude.setText(location.getLatitude() + "");
                popWin(ll);
                //建议地址
                setListView(location.getAddrStr());
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    // 地理位置建议监听
    OnGetSuggestionResultListener listener_advice = new OnGetSuggestionResultListener() {
        public void onGetSuggestionResult(SuggestionResult res) {
            if (res == null || res.getAllSuggestions() == null) {
                return;
                //未找到相关结果
            }
            List<SuggestionInfo> SuggestionInfos = res.getAllSuggestions();
            String a[] = new String[SuggestionInfos.size()];
            for (int i = 0; i < SuggestionInfos.size(); i++) {
                a[i] = SuggestionInfos.get(i).city + SuggestionInfos.get(i).district + SuggestionInfos.get(i).key;
            }
            lv_ListView.setAdapter(new ArrayAdapter<String>(MapActivity.this,
                    android.R.layout.simple_list_item_1, a));
        }
    };
    // 地理位置检索监听
    OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
        public void onGetGeoCodeResult(GeoCodeResult result) {
            mBaiduMap.clear();
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                // 没有检索到结果
                Toast.makeText(MapActivity.this, "抱歉，未能找到结果",
                        Toast.LENGTH_LONG).show();
                mBaiduMap.clear();
            }
            // 获取地理编码结果
            /*
			 * Toast.makeText(MyMap.this, result.getLocation() + "LLL",
			 * Toast.LENGTH_SHORT).show();
			 */
            else {
                LatLng ll = new LatLng(result.getLocation().latitude,
                        result.getLocation().longitude);
                popWin(ll);
            }
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MapActivity.this, "抱歉，未能找到结果",
                        Toast.LENGTH_LONG).show();
                return;
            }
            mBaiduMap.clear();
            mBaiduMap.addOverlay(new MarkerOptions().position(
                    result.getLocation()).icon(
                    BitmapDescriptorFactory
                            .fromResource(R.drawable.nav_turn_via_1)));
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                    .getLocation()));
            et_Conaddress.setText(result.getAddress());
            //建议地址
            setListView(result.getAddress());
            Toast.makeText(MapActivity.this, result.getAddress(),
                    Toast.LENGTH_LONG).show();

        }
    };

    // 自定义提示图标
    public void popWin(LatLng point) {

        // 定义地图状态 设置中心点，放大倍数
        MapStatus mMapStatus = new MapStatus.Builder().target(point).zoom(15)
                .build();
        // 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mMapStatus);
        // 改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        // 构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.nav_turn_via_1);
        // 构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().position(point)
                .icon(bitmap);
        // 在地图上添加Marker，并显示
        mBaiduMap.clear();
        mBaiduMap.addOverlay(option);
        tv_Longitude.setText(point.longitude + "");
        tv_Latitude.setText(point.latitude + "");

        // mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
    }

    //底部listView显示附件街道信息
    private void setListView(String string) {
        if (string != null)
            mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                    .keyword(string)
                    .city(city));
    }
}