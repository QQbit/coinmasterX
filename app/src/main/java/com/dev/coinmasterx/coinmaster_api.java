package com.dev.coinmasterx;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.dev.coinmasterx.model.GrahpError;
import com.dev.coinmasterx.model.Test_Users;
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
import java.util.concurrent.ThreadPoolExecutor;


public class coinmaster_api {
    private OkHttpClient okHttpClient;
    private String TAG = "coinmaster-API:";
    private String deviceId = "";
    private String deviceToken = "";
    private String token = "";
    private String inviteURL = "";

    public coinmaster_api(String inviteURL) {
      this.inviteURL = inviteURL;
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
                        Log.d(TAG, "callback_registerGame : " + jsonObject.toString());
                        deviceToken = jsonObject.getString("deviceToken");
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
    private void FBloginGame(String deviceToken, String fbToken){
        Log.d(TAG, "FBloginGame deviceToken : " + deviceToken);
        Log.d(TAG, "FBloginGame fbToken : " + fbToken);
        Request request = FBlogin(deviceToken,fbToken);
        okHttpClient.newCall(request).enqueue(callback_FBLoginGame(fbToken));
    }

    private void loginGame(String deviceToken){
        Log.d(TAG, "LoginGame : " + deviceToken);
        Request request = login(deviceToken);
        okHttpClient.newCall(request).enqueue(callback_LoginGame());
    }

    private Callback callback_FBLoginGame(final String fbToken){
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
                        Log.d(TAG, "callback_FBLoginGame: "+ jsonObject.toString());
                        String userId = jsonObject.getString("userId");
                        String sessionToken = jsonObject.getString("sessionToken");

                        JSONObject data = new JSONObject();
                        data.put("Device",deviceId);
                        data.put("fbToken",fbToken);
                        data.put("userId",userId);
                        data.put("sessionToken",sessionToken);
                        Invite(inviteURL, data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void Invite(String inviteURL, JSONObject data){
        Request request = new Request.Builder()
                .url(inviteURL)
                .get()
                .build();

        try {
          Response getUser = okHttpClient.newCall(request).execute();
          String response = getUser.body().string();
         // Log.d(TAG, "Invite GetUser: "+ response);
          String[] shared_link = response.split("&amp;c=");
          shared_link = shared_link[1].split("&amp;");
          String User =  shared_link[0];
          Log.d(TAG, "Invite GetUser: "+ User);

          Request requestInvite = AcceptInvite(data, User);
          okHttpClient.newCall(requestInvite).enqueue(AcceptInvite(data));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    private Callback AcceptInvite(final JSONObject data){
        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "onFailure: "+ e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    String raw = response.body().string();
                    Log.d(TAG, "AcceptInvite: "+ raw);
                    if(raw.contains("name")){
                        spinMaster(data);
                    }else{
                        Log.e(TAG, "User not found: "+ raw);
                        try {
                            Log.e(TAG, "UserId: "+ data.getString("userId"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.e(TAG, "Token Fails!");
                        return;
                    }
                }

            }
        };
    }

    private void spinMaster(JSONObject data){
        try {
            int count = 1;
            while (true) {
                Request request = spinMasterRequest(data, count++);
                Response response = okHttpClient.newCall(request).execute();
                JSONObject roundSpin = new JSONObject(response.body().string());
                int spins = roundSpin.getInt("spins");
                Log.d(TAG, "Spin: " + spins);
                if(spins >= 50){
                    Request requestUpgrade = spinUpgradeRequest(data);
                    Response upgrade = okHttpClient.newCall(requestUpgrade).execute();
                    Log.d(TAG, "upgrade: " + upgrade.body().string());
                    break;
                }
            }
            return;

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private Request spinUpgradeRequest(JSONObject data) throws JSONException {

        String upgrade = "Device[udid]="+data.getString("Device")+"&API_KEY=viki&API_SECRET=coin&Device[change]=20201008_4&fbToken="+data.getString("fbToken")+"&locale=th&item=House&state=0&include[0]=pets";


        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), upgrade);

        return new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization","Bearer " + data.getString("sessionToken"))
                .addHeader("X-CLIENT-VERSION","3.5.170")
                .url("https://vik-game.moonactive.net/api/v1/users/"+data.getString("userId")+"/upgrade")
                .post(requestBody)
                .build();
    }

    private Request spinMasterRequest(JSONObject data, int count) throws JSONException {

        String spin = "Device[udid]="+data.getString("Device")+"&API_KEY=viki&API_SECRET=coin&Device[change]=20201008_4&fbToken="+data.getString("fbToken")+"&locale=th&seq="+count+"&auto_spin=False&bet=1&Client[version]=3.5.170_fband";


        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), spin);

        return new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization","Bearer " + data.getString("sessionToken"))
                .addHeader("X-CLIENT-VERSION","3.5.170")
                .url("https://vik-game.moonactive.net/api/v1/users/"+data.getString("userId")+"/spin")
                .post(requestBody)
                .build();
    }

    private Request AcceptInvite(JSONObject data, String User) throws JSONException {

        String block_login = "Device[udid]="+data.getString("Device")+"&API_KEY=viki&API_SECRET=coin&Device[change]=20201008_4&fbToken="+data.getString("fbToken")+"&locale=th&inviter="+User;


        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), block_login);

            return new Request.Builder()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Authorization","Bearer " + data.getString("sessionToken"))
                    .addHeader("X-CLIENT-VERSION","3.5.170")
                    .url("https://vik-game.moonactive.net/api/v1/users/"+data.getString("userId")+"/accept_invitation")
                    .post(requestBody)
                    .build();
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
                        Log.d(TAG, "callback_LoginGame: "+ jsonObject.toString());
                        String userId = jsonObject.getString("userId");
                        String sessionToken = jsonObject.getString("sessionToken");
                        update_fb_data(userId, sessionToken); //update fb data

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else {
                    Log.d(TAG, "callback_LoginGame fail: " + response.body().string());
                }
            }
        };
    }



    private void update_fb_data(String userId, String sessionToken){
        Request request = updateFBData(userId,sessionToken);
        if(request == null)
            return;
        okHttpClient.newCall(request).enqueue(callback_update_fb_data());
    }

    private Callback callback_update_fb_data(){
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
                        Log.d(TAG, "callback_update_fb_data: "+ jsonObject.toString());
                        String fbToken = jsonObject.getString("fbToken");
                        FBloginGame(deviceToken,fbToken); //login Game
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        };
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

    private Request FBlogin(String DeviceToken, String fbToken) {

        String block_login = "Device[udid]="+deviceId+"&API_KEY=viki&API_SECRET=coin&Device[change]=20201008_4&fbToken="+fbToken+"&locale=th&Device[os]=Android&Client[version]=3.5_fband&Device[version]=5.1.1&seq=1";


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
        fb_api grahpAPI = new fb_api();
        Test_Users testUsers = null;
        try {
          String tokenTest = grahpAPI.generateTokenTest();
          if(tokenTest.contains("error")){
              GrahpError grahpError = grahpAPI.grahpErrorModel(tokenTest);
              Log.e(TAG, "fbGraph: "+ grahpError.getMessage());
              if(grahpError.getCode() == 2900){
                  //in progress try again
                  requestNewFBApp();
              }
              return null;
          }
          testUsers = grahpAPI.TestUsersModel(tokenTest);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if(testUsers == null)
            return null;


        String updateFB_Profile = "Device[udid]="+deviceId+"&API_KEY=viki&API_SECRET=coin&Device[change]=20201008_4&fbToken=&locale=th&User[fb_token]="+testUsers.getAccess_token()+"&p=fb&Client[version]=3.5.170_fband";


        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), updateFB_Profile);

        return new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization","Bearer " + sessionToken)
                .addHeader("X-CLIENT-VERSION","3.5.170")
                .url("https://vik-game.moonactive.net/api/v1/users/"+userId+"/update_fb_data")
                .post(requestBody)
                .build();
    }

    private boolean requestNewFBApp(){
        return true;
    }

}

