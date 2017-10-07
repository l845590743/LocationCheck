package com.example.locationservice;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by lzm on 2016/11/4.
 */
public class LocationService extends Service implements LocationListener {

    private static final String TAG = "LocationService";
    private LocationManager locationManager;
    private boolean isSave;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            saveLocationData((String) msg.obj);
            super.handleMessage(msg);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onStart(Intent intent, int startId) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "go to");
            if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            else if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }
        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Get the current position \n" + location);
        String addressLine = "";
        Geocoder gc = new Geocoder(this, Locale.getDefault());
        List<Address> locationList = null;
        String locationAddress = "";
        try {
            locationList = gc.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (locationList != null && locationList.size() > 0) {
            Address address = locationList.get(0);//得到Address实例
            String countryName = address.getCountryName();//得到国家名称：中国
            //Log.i(TAG, "countryName = " + countryName);
            String locality = address.getLocality();//得到城市名称：北京市
            //Log.i(TAG, "locality = " + locality);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; address.getAddressLine(i) != null; i++) {
                //得到周边信息
                addressLine = address.getAddressLine(i);
                builder.append(addressLine);
                //Log.i(TAG, "addressLine = " + addressLine);
            }
            Log.i(TAG, "address = " + builder.toString());
            locationAddress = builder.toString();
            saveLocationData(locationAddress);
        }
        // 定位一次，就移除监听，停掉服务。
//        locationManager.removeUpdates(this);
//        stopSelf();
    }

    private long time = 0;
    private void saveLocationData(String addr) {
        if (new Date().getTime() > time + 20000) {
            Log.d(TAG,"time  = " + new Date().getTime());
            String currentTime = getCurrentTime();
            String data = currentTime + "," + addr + ";";
            FileOutputStream out = null;
            BufferedWriter writer = null;
            try {
                out = openFileOutput("locationData", Context.MODE_APPEND);
                writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(data);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            time = new Date().getTime();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date());
    }
}
