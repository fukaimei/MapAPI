package com.fukaimei.mapapi.task;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.fukaimei.mapapi.http.HttpRequestUtil;
import com.fukaimei.mapapi.http.tool.HttpReqData;
import com.fukaimei.mapapi.http.tool.HttpRespData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;

public class GetAddressTask extends AsyncTask<Location, Void, String> {
    private final static String TAG = "GetAddressTask";
    private static String mAddressUrl = "http://maps.google.cn/maps/api/geocode/json?latlng={0},{1}&sensor=true&language=zh-CN";
    private OnAddressListener mListener;

    public GetAddressTask() {
        super();
    }

    @Override
    protected String doInBackground(Location... params) {
        Location location = params[0];
        String url = MessageFormat.format(mAddressUrl, location.getLatitude(), location.getLongitude());
        HttpReqData req_data = new HttpReqData(url);
        HttpRespData resp_data = HttpRequestUtil.getData(req_data);
        Log.d(TAG, "return json = " + resp_data.content);

        String address = "当前定位地址解析失败，请开启网络方可解析！";
        if (resp_data.err_msg.length() <= 0) {
            try {
                JSONObject obj = new JSONObject(resp_data.content);
                JSONArray resultArray = obj.getJSONArray("results");
                if (resultArray.length() > 0) {
                    JSONObject resultObj = resultArray.getJSONObject(0);
                    address = resultObj.getString("formatted_address");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "address = " + address);
        return address;
    }

    @Override
    protected void onPostExecute(String address) {
        mListener.onFindAddress(address);
    }

    public void setOnAddressListener(OnAddressListener listener) {
        mListener = listener;
    }

    public interface OnAddressListener {
        void onFindAddress(String address);
    }

}
