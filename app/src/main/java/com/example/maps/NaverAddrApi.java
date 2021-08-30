package com.example.maps;

import android.os.AsyncTask;
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

public class NaverAddrApi extends AsyncTask<LatLng, String, String> {
    private StringBuilder urlBuilder;
    private URL url;
    private HttpURLConnection conn;
    private MainActivity mMainActivity;

    public NaverAddrApi(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(LatLng... latLngs) {
        String strCoord = String.valueOf(latLngs[0].longitude) + "," + String.valueOf(latLngs[0].latitude);
        StringBuilder sb = new StringBuilder();

        urlBuilder = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=" +strCoord+ "&sourcecrs=epsg:4326&output=json&orders=addr"); /* URL */
        try {
            url = new URL(urlBuilder.toString());

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "maps1/json");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID","ufsa1q151p");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY","ktWnzeeTBfGcvTRobl3qRz3GyqAQajuMSz5Pgx80");

            BufferedReader rd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
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
        //TOTO 인자로 넘어온 jsonStr을 파싱하여 원하는 값들만 받아온다

        String address = getAdd(jsonStr);

        mMainActivity.viewAddress(address);

//        String pnu = getPnu(jsonStr);
    }

    private String getAdd(String jsonStr){
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject=(JsonObject) jsonParser.parse(jsonStr);
        JsonArray jsonArray = (JsonArray) jsonObject.get("results");
        jsonObject = (JsonObject) jsonArray.get(0);
        jsonObject = (JsonObject) jsonObject.get("region");
        jsonObject = (JsonObject) jsonObject.get("area1");
        String address=jsonObject.get("name").getAsString();

        jsonObject = (JsonObject) jsonParser.parse(jsonStr);
        jsonArray = (JsonArray) jsonObject.get("results");
        jsonObject = (JsonObject) jsonArray.get(0);
        jsonObject = (JsonObject) jsonObject.get("region");
        jsonObject = (JsonObject) jsonObject.get("area2");
        address = address + " " + jsonObject.get("name").getAsString();

        jsonObject = (JsonObject) jsonParser.parse(jsonStr);
        jsonArray = (JsonArray) jsonObject.get("results");
        jsonObject = (JsonObject) jsonArray.get(0);
        jsonObject = (JsonObject) jsonObject.get("region");
        jsonObject = (JsonObject) jsonObject.get("area3");
        address = address + " " + jsonObject.get("name").getAsString();

        jsonObject = (JsonObject) jsonParser.parse(jsonStr);
        jsonArray = (JsonArray) jsonObject.get("results");
        jsonObject = (JsonObject) jsonArray.get(0);
        jsonObject = (JsonObject) jsonObject.get("land");
        String number1=jsonObject.get("number1").getAsString();
        String number2=jsonObject.get("number2").getAsString();
        address=address + " "  + makeStringNum(number1) + " - " + makeStringNum(number2);

        return address;
    }


    /*private String getPnu(String jsonStr) {
        JsonParser jsonParser = new JsonParser();

        JsonObject jsonObj = (JsonObject) jsonParser.parse(jsonStr);
        JsonArray jsonArray = (JsonArray) jsonObj.get("results");
        jsonObj = (JsonObject) jsonArray.get(0);
        jsonObj = (JsonObject) jsonObj.get("code");
        String pnu = jsonObj.get("id").getAsString();

        jsonObj = (JsonObject) jsonParser.parse(jsonStr);
        jsonArray = (JsonArray) jsonObj.get("results");
        jsonObj = (JsonObject) jsonArray.get(0);
        jsonObj = (JsonObject) jsonObj.get("land");
        pnu = pnu + jsonObj.get("type").getAsString();
        String number1 = jsonObj.get("number1").getAsString();
        String number2 = jsonObj.get("number2").getAsString();
        pnu = pnu + makeStringNum(number1) + makeStringNum(number2);
        return pnu;
    }
     */

    private String makeStringNum(String number) {
        String strNum="";
        for (int i=0; i<4-number.length(); i++) {
            strNum = strNum + "0";
        }
        strNum=strNum+number;
        return strNum;
    }
}