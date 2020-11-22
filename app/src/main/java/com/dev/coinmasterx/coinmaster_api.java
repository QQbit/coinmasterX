package com.dev.coinmasterx;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class coinmaster_api {
    private OkHttpClient okHttpClient;
    private String TAG = "coinmaster-API:";
    private String deviceId = "";
    private String token = "";

    public coinmaster_api(String inviteURL, int count) {
      okHttpClient = new OkHttpClient();
        Request request = null;
        try {
            request = registerDeviceID(getDeviceID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        okHttpClient.newCall(request).enqueue(callback_registerDeviceID());
    }

    private JSONObject getDeviceID() throws JSONException {
        JSONObject deviceID = new JSONObject();
        deviceId = utility.generateUUID();
        deviceID.put("deviceId",deviceId);
        return deviceID;
    }

    private Callback callback_registerDeviceID() {
        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "onFailure: "+ e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject challengeObj = null;
                    try {
                        String raw = response.body().string();
                        Log.d(TAG, raw);
                        challengeObj = new JSONObject(raw);
                        challengeObj.put("deviceId",deviceId);
                        JSONObject anwserInChallenge =  challengeObj.getJSONObject("challenge");
                        anwserInChallenge.put("answer","2293012");
                        challengeObj.put("challenge",anwserInChallenge); //add new key answer to challenge
                        
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "registerDeviceID : " + challengeObj.toString());
                    Request request = registerGame(challengeObj);
                    okHttpClient.newCall(request).enqueue(callback_registerGame());



                }
            }
        };
    }

    private Callback callback_registerGame(){
        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "onFailure: "+ e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String deviceToken = jsonObject.getString("deviceToken");
                        loginGame(deviceToken); //login Game
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        };
    }

    private void loginGame(String deviceToken){
        Request request = login(deviceToken);
        okHttpClient.newCall(request).enqueue(callback_LoginGame());
    }

    private Callback callback_LoginGame(){
        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "onFailure: "+ e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if(response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        String userId = jsonObject.getString("userId");
                        String sessionToken = jsonObject.getString("sessionToken");
                        update_fb_data(userId, sessionToken); //update fb data

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
    }

    private void update_fb_data(String userId, String sessionToken){
        Request request = updateFBData(userId,sessionToken);
        okHttpClient.newCall(request).enqueue(callback_LoginGame());
    }
    //TODO: Use Retrofit for this
    private Request registerDeviceID(JSONObject json) {

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        return new Request.Builder()
                .url("https://vik-game.moonactive.net/api/v1/authentication/registerStart")
                .post(requestBody)
                .build();
    }

    private Request registerGame(JSONObject json) {

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        return new Request.Builder()
                .url("https://vik-game.moonactive.net/api/v1/authentication/register")
                .post(requestBody)
                .build();
    }

    private Request login(String DeviceToken) {

        String block_login = "Device[udid]="+deviceId+"&API_KEY=viki&API_SECRET=coin&Device[change]=20201007_2&fbToken=&locale=th&Device[os]=Android&Client[version]=3.5_fband&Device[version]=5.1.1&seq=0";


        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), block_login);

        return new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization","Bearer " + DeviceToken)
                .addHeader("X-CLIENT-VERSION","3.5.170")
                .url("https://vik-game.moonactive.net/api/v1/users/login")
                .post(requestBody)
                .build();
    }

    private Request updateFBData(String userId, String sessionToken) {

        String block_login = "Device[udid]="+deviceId+"&API_KEY=viki&API_SECRET=coin&Device[change]=20201008_4&fbToken=&locale=th&User[fb_token]="+token+"&p=fb&Client[version]=3.5.170_fband";


        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), block_login);

        return new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization","Bearer " + sessionToken)
                .addHeader("X-CLIENT-VERSION","3.5.170")
                .url("https://vik-game.moonactive.net/api/v1/users/"+userId+"/update_fb_data")
                .post(requestBody)
                .build();
    }

}

