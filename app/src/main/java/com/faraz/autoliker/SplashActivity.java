package com.faraz.autoliker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
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

public class SplashActivity extends AppCompatActivity {

    String tinderToken,fbToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SharedPreferences sharedPreferences=getSharedPreferences("api_token",MODE_PRIVATE);
        tinderToken=sharedPreferences.getString("tinder_token","false");
        SharedPreferences.Editor editor=sharedPreferences.edit();
        sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        fbToken=sharedPreferences.getString("fb_token","false");



        if(tinderToken.equalsIgnoreCase("false"))
            startActivity(new Intent(this,FBLoginActivity.class));


        else
        {
            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest sr2 = new StringRequest(Request.Method.GET,"https://api.gotinder.com/profile", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("response",response);

                    //if error response then open FbActivity
                    if(response.equalsIgnoreCase("Unauthorized")) {
                        if (!fbToken.equalsIgnoreCase("false")) {
                            Toast.makeText(SplashActivity.this, fbToken, Toast.LENGTH_SHORT).show();
                            authenticte(fbToken);
                        }
                        else
                         startActivity(new Intent(SplashActivity.this, FBLoginActivity.class));
                    }


                    //else if successful then send fb_token to updateActivity
                    else
                    {
                        try {
                            JSONObject json=new JSONObject(response);
                             String dob=json.getString("birth_date");
                             Intent i=new Intent(SplashActivity.this,MainFragmentActivity.class);
                             i.putExtra("dob",dob);
                            startActivity(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse=error.networkResponse;
                    if (networkResponse != null && networkResponse.statusCode == 401)
                    {
                        if (!fbToken.equalsIgnoreCase("false")) {
                            Toast.makeText(SplashActivity.this, fbToken, Toast.LENGTH_SHORT).show();
                            authenticte(fbToken);
                        }
                        else
                            startActivity(new Intent(SplashActivity.this, FBLoginActivity.class));
                    }

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type","application/json");
                    params.put("app_version","6.9.4");
                    params.put("platform","ios");
                    params.put("User-agent","Tinder/7.5.3 (iPhone; iOS 10.3.2; Scale/2.00)");
                    params.put("X-Auth-Token",tinderToken);
                    return params;
                }


            };

            queue.add(sr2);
        }
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
                    String dob=jsonObject.getString("birth_date");
                    Intent i=new Intent(SplashActivity.this,MainFragmentActivity.class);
                    i.putExtra("dob",dob);
                    startActivity(i);

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


}
