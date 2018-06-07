package com.faraz.autoliker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class FBLoginActivity extends AppCompatActivity {

    WebView mWebView;
    String tinderToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView=(WebView)findViewById(R.id.fb_login);

        SharedPreferences mpref=getSharedPreferences("data",MODE_PRIVATE);
        final SharedPreferences.Editor editor=mpref.edit();
        getInfo();
        mWebView.clearCache(true);
        CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean aBoolean) {

            }
        });

        //startActivity(new Intent(this,UpdateActivity.class));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient()
        {
            public void onPageFinished(WebView webView, String str) {

                if (str.contains("access_token=")) {
                    mWebView.setVisibility(View.GONE);
                    String substring = str.substring(str.indexOf("access_token=") + 13, str.indexOf("&expires_in"));
                    Log.d("link",str);
                    if (substring != null && !substring.isEmpty()) {
                        editor.putString("fb_token",substring).commit();
                        authenticte(substring);

                    }
                }
            }

        });
        mWebView.loadUrl("https://www.facebook.com/v2.6/dialog/oauth?redirect_uri=fb464891386855067%3A%2F%2Fauthorize%2F&display=touch&state=%7B%22challenge%22%3A%22IUUkEUqIGud332lfu%252BMJhxL4Wlc%253D%22%2C%220_auth_logger_id%22%3A%2230F06532-A1B9-4B10-BB28-B29956C71AB1%22%2C%22com.facebook.sdk_client_state%22%3Atrue%2C%223_method%22%3A%22sfvc_auth%22%7D&scope=user_birthday%2Cuser_photos%2Cuser_education_history%2Cemail%2Cuser_relationship_details%2Cuser_friends%2Cuser_work_history%2Cuser_likes&response_type=token%2Csigned_request&default_audience=friends&return_scopes=true&auth_type=rerequest&client_id=464891386855067&ret=login&sdk=ios&logger_id=30F06532-A1B9-4B10-BB28-B29956C71AB1&ext=1470840777&hash=AeZqkIcf-NEW6vBd");

    }

    void authenticte(final String fbToken)
    {

        final JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("token", fbToken);
            jsonBody.put("id", "12238876");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody=jsonBody.toString();

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://api.gotinder.com/v2/auth", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject=new JSONObject(response);
                    jsonObject=(JSONObject)jsonObject.get("data");
                    String token=jsonObject.getString("api_token");
                    SharedPreferences sharedPreferences=getSharedPreferences("api_token",MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("tinder_token",token).commit();


                    Toast.makeText(getApplicationContext(),token, Toast.LENGTH_SHORT).show();

                    Intent i=new Intent(FBLoginActivity.this,MainFragmentActivity.class);
                    i.putExtra("fb_token",fbToken);
                    startActivity(i);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/json");
                params.put("app_version","6.9.4");
                params.put("platform","ios");
                // params.put("User-agent","Tinder/7.5.3 (iPhone; iOS 10.3.2; Scale/2.00)");
                //params.put("X-Auth-Token","9e1e7c2b-b5c7-4be6-88ff-48087bd4bea5");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };
        queue.add(sr);

    }

    void getInfo()
    {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest sr2 = new StringRequest(Request.Method.GET,"https://api.gotinder.com/profile", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/json");
                params.put("app_version","6.9.4");
                params.put("platform","ios");
                params.put("User-agent","Tinder/7.5.3 (iPhone; iOS 10.3.2; Scale/2.00)");
                params.put("X-Auth-Token","9e1e7c2b-b5c7-4be6-88ff-48087bd4bea5");
                return params;
            }


        };

        queue.add(sr2);

    }



}
