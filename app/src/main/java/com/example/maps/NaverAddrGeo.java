package com.example.maps;

import android.location.Address;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.naver.maps.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NaverAddrGeo extends AsyncTask<String, String, String> {
    private StringBuilder urlBuilder;
    private URL url;
    private HttpURLConnection conn;
    private MainActivity mMainActivity;

    public NaverAddrGeo(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... strings) {

        StringBuilder sb = new StringBuilder();

        urlBuilder = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + strings[0]); /* URL */
        try {
            url = new URL(urlBuilder.toString());

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "maps1/json");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "ufsa1q151p");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", "ktWnzeeTBfGcvTRobl3qRz3GyqAQajuMSz5Pgx80");

            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();

        } catch (Exception e) {
            return null;
        }
        return sb.toString();
    }

    @Override
    public void onPostExecute(String jsonStr) {
        super.onPostExecute(jsonStr);

        String Address = getAdd(jsonStr);

        mMainActivity.viewMarker(Address);
    }

    private String getAdd(String jsonStr) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject=(JsonObject) jsonParser.parse(jsonStr);
        JsonArray jsonArray = (JsonArray) jsonObject.get("addresses");
        jsonObject = (JsonObject) jsonArray.get(0);
        jsonArray = (JsonArray) jsonObject.get("addressElements");
        String Address=jsonObject.get("x").getAsString();

        jsonObject = (JsonObject) jsonParser.parse(jsonStr);
        jsonArray = (JsonArray) jsonObject.get("addresses");
        jsonObject = (JsonObject) jsonArray.get(0);
        jsonArray = (JsonArray) jsonObject.get("addressElements");
        Address = Address + ','+jsonObject.get("y").getAsString();

        return Address;
    }

    private String makeStringNum(String number) {
        String strNum = "";
        for (int i = 0; i < 4 - number.length(); i++) {
            strNum = strNum + "0";
        }
        strNum = strNum + number;
        return strNum;
    }
}

