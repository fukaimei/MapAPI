package com.fukaimei.mapapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.fukaimei.mapapi.http.tool.DateUtil;
import com.fukaimei.mapapi.task.GetAddressTask;

public class HttpRequestActivity extends AppCompatActivity implements GetAddressTask.OnAddressListener {
    private final static String TAG = "HttpRequestActivity";
    private TextView tv_location;
    private String mLocation = "";
    private LocationManager mLocationMgr;
    private Criteria mCriteria = new Criteria();
    private Handler mHandler = new Handler();
    private boolean bLocationEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_request);
        // 申请定位的动态权限
        locationPermissions();
        initWidget();
        initLocation();
        mHandler.postDelayed(mRefresh, 100);
    }

    // 定义定位的动态权限
    private void locationPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
    }

    /**
     * 重写onRequestPermissionsResult方法
     * 获取动态权限请求的结果,再开启定位
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initLocation();
        } else {
            Toast.makeText(this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initWidget() {
        tv_location = (TextView) findViewById(R.id.tv_location);
        mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        mCriteria.setAltitudeRequired(true);
        mCriteria.setBearingRequired(true);
        mCriteria.setCostAllowed(true);
        mCriteria.setPowerRequirement(Criteria.POWER_LOW);
    }

    private void initLocation() {
        String bestProvider = mLocationMgr.getBestProvider(mCriteria, true);
        if (bestProvider == null) {
            bestProvider = LocationManager.NETWORK_PROVIDER;
        }
        if (mLocationMgr.isProviderEnabled(bestProvider)) {
            tv_location.setText("正在获取" + bestProvider + "定位对象");
            mLocation = String.format("当前定位类型：%s", bestProvider);
            beginLocation(bestProvider);
            bLocationEnable = true;
        } else {
            tv_location.setText("\n" + bestProvider + "定位不可用");
            bLocationEnable = false;
        }
    }

    private String mAddress = "";

    @Override
    public void onFindAddress(String address) {
        mAddress = address;
    }

    @SuppressLint("DefaultLocale")
    private void setLocationText(Location location) {
        if (location != null) {
            String desc = String.format("%s" + "\n定位时间：%s" + "\n经度：%f，纬度：%f" +
                            "\n海拔高度：%d米，定位精度：%d米" + "\n定位地址：%s",
                    mLocation, DateUtil.getNowDateTime("yyyy/MM/dd HH:mm:ss"),
                    location.getLongitude(), location.getLatitude(),
                    Math.round(location.getAltitude()), Math.round(location.getAccuracy()), mAddress);
            Log.d(TAG, desc);
            tv_location.setText(desc);
            GetAddressTask addressTask = new GetAddressTask();
            addressTask.setOnAddressListener(this);
            addressTask.execute(location);
        } else {
            tv_location.setText(mLocation + "\n暂未获取到定位对象");
        }
    }

    private void beginLocation(String method) {
        mLocationMgr.requestLocationUpdates(method, 300, 0, mLocationListener);
        Location location = mLocationMgr.getLastKnownLocation(method);
        setLocationText(location);
    }

    // 位置监听器
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setLocationText(location);
        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    };

    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            if (bLocationEnable == false) {
                initLocation();
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (mLocationMgr != null) {
            mLocationMgr.removeUpdates(mLocationListener);
        }
        super.onDestroy();
    }

}
