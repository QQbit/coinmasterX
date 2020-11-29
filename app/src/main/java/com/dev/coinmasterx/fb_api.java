package com.dev.coinmasterx;

import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;

import com.dev.coinmasterx.model.GrahpError;
import com.dev.coinmasterx.model.Test_Users;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

public class fb_api {

    //auto gen token https://graph.facebook.com/800973937107927/accounts/test-users?access_token=800973937107927|67b80d757c673c9c69afb6506b2377d6&installed=true&permissions=read_stream&method=post
    public String generateTokenTest() throws IOException{
        String requestToken = "https://graph.facebook.com/"+getAppId()+"/accounts/test-users?installed=true&permissions=read_stream"+
                "&locale=en_US&method=post&access_token="+getAppId()+"|"+getAppSecretKey();
        //String requestToken = "https://graph.facebook.com/983739412127363/accounts/test-users?access_token=983739412127363|OR3FsghdD6IdEA3K7Urb0vRudo8&installed=true&permissions=read_stream&method=post";
        Request request = appGetTestUserToken(requestToken);
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();
        if(response.isSuccessful()){
            Log.d("fb_api", "generateTokenTest: ok");
            return response.body().string();
        }else if(response.code() == 400){
            Log.d("fb_api", "generateTokenTest: code 400");
            return response.body().string();
        }
        Log.d("fb_api", "generateTokenTest: Fail ? " + response.body().string());
        return response.body().string();
    }

    public Test_Users TestUsersModel(String raw) throws JSONException {
        Test_Users obj = new Test_Users();
        JSONObject jsonObject = new JSONObject(raw);
            obj.setId(jsonObject.getString("id"));
            obj.setAccess_token(jsonObject.getString("access_token"));
            obj.setLogin_url(jsonObject.getString("login_url"));
            obj.setEmail(jsonObject.getString("email"));
            obj.setPassword(jsonObject.getString("password"));

        return obj;
    }

    public GrahpError grahpErrorModel(String raw) throws JSONException {
        GrahpError error = new GrahpError();
        JSONObject jsonObject = new JSONObject(raw);
        jsonObject =  jsonObject.getJSONObject("error");
        error.setMessage(jsonObject.getString("message"));
        error.setType(jsonObject.getString("type"));
        error.setCode(jsonObject.getInt("code"));
        error.setFbtrace_id(jsonObject.getString("fbtrace_id"));

        return error;
    }

    private Request appGetTestUserToken(String requestToken) {

        return new Request.Builder()
                .url(requestToken)
                .get()
                .build();
    }

    //syn AppId from server
    public String getAppId(){
        return "800973937107927";
    }

    public String getAppSecretKey(){
        return "67b80d757c673c9c69afb6506b2377d6";
    }

}


