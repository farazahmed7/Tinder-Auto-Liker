package com.faraz.autoliker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LikeActivity extends AppCompatActivity {


    Handler mHandler;
    private ArrayList<String> profileList,profileInfo,profileIds;
    HashMap<String,List<String>> profileMap;
    JSONArray profiles;
    int i ,count;
    String tinderToken;
    boolean isSuccessful;
    String likesNumber,fbToken;
    Timestamp mTimestamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        Button button=findViewById(R.id.button);

        isSuccessful=false;
        profileMap=new HashMap<>();
        profileIds=new ArrayList<>();
        mHandler = new Handler();
        SharedPreferences sharedPreferences = getSharedPreferences("api_token", MODE_PRIVATE);
        likesNumber =sharedPreferences.getString("likesNumber","0");
        sharedPreferences = this.getSharedPreferences("api_token", MODE_PRIVATE);
        tinderToken = sharedPreferences.getString("tinder_token", "false");
        SharedPreferences sharedPreferences2=getSharedPreferences("data",MODE_PRIVATE);
        fbToken=sharedPreferences2.getString("fb_token","false");



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getRecsSynchronous();
            }
        });
    }

    void getRecsSynchronous()
    {
        RequestFuture<String> requestFuture=RequestFuture.newFuture();

        StringRequest stringRequest=new StringRequest(Request.Method.GET,"https://api.gotinder.com/user/recs",requestFuture,new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.statusCode == 401) {
                    if (!fbToken.equalsIgnoreCase("false")) {
                        Log.d("errorbc",networkResponse.statusCode+"");
                        authenticte(fbToken);
                    }
                }

            }
        })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("app_version", "6.9.4");
                params.put("platform", "ios");
                params.put("User-agent", "Tinder/7.5.3 (iPhone; iOS 10.3.2; Scale/2.00)");
                params.put("X-Auth-Token", tinderToken);
                return params;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

        try { Log.d("errr","getrecs");

            String response= requestFuture.get(10, TimeUnit.SECONDS);
            validateRecsResponse(response);
            isSuccessful=true;
        } catch (InterruptedException e) {
            Log.d("errr",e.toString());
            isSuccessful=false;
        } catch (ExecutionException e) {
            // exception handling
            Log.d("errr",e.toString());
            isSuccessful=false;
        } catch (TimeoutException e) {
            // exception handling
            Log.d("errr",e.toString());
            isSuccessful=false;
        } catch (Exception e)
        {
            Log.d("errr",e.toString());
            isSuccessful=false;
        }
    }

    void validateRecsResponse(String response)
    {
        //if error response then open FbActivity
        if(response.equalsIgnoreCase("Unauthorized")) {
            if (!fbToken.equalsIgnoreCase("false")) {
                authenticte(fbToken);
            }

        }

        else {
            //  Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
            profileList = new ArrayList<>();
            profileInfo = new ArrayList<>();


            try {
                final JSONObject result = new JSONObject(response);
                profiles = (JSONArray) result.get("results");

                for (int i = 0; i < profiles.length(); ++i) {
                    String id = profiles.getJSONObject(i).getString("_id");
                    if (!profileMap.containsKey(id))

                    {
                        profileInfo = new ArrayList<>();
                        // profileList.add(profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url")+","+profiles.getJSONObject(i).getString("_id")+","+profiles.getJSONObject(i).getString("name"));
                        profileInfo.add(profiles.getJSONObject(i).getString("name"));
                        profileInfo.add(profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url"));
                        profileInfo.add(profiles.getJSONObject(i).getString("birth_date"));
                        profileIds.add(id);
                        profileMap.put(id, profileInfo);
                    }
                }

                autoLikeSynchronous();

            } catch (Exception e) {
                Log.d("getrecs1", e.getMessage());

            }
        }
    }


    void autoLikeSynchronous()
    {

        RequestFuture<String> requestFuture=RequestFuture.newFuture();
        String id=profileIds.get(count);
        count++;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://api.gotinder.com/like/" + id, requestFuture,requestFuture)
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("app_version", "6.9.4");
                params.put("platform", "ios");
                params.put("User-agent", "Tinder/7.5.3 (iPhone; iOS 10.3.2; Scale/2.00)");
                params.put("X-Auth-Token", tinderToken);
                return params;
            }

        };


        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

        try {
            String response= requestFuture.get(3, TimeUnit.SECONDS);

            if(i==4)
            {
                createNotification();
            }
            if (i < 5) {

                Log.d("TinderResponse",response+i);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            i++;
                            autoLikeSynchronous();

                        } catch (Exception e) {
                            Log.d("error10",e.getMessage());
                            getRecsSynchronous();

                        }
                    }
                }, 1000);
            }

        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            // exception handling
        } catch (TimeoutException e) {
            // exception handling
        }
    }

    void getRecs() {
        Log.d("getRecs","getRecs");
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/user/recs", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                //if error response then open FbActivity
                if(response.equalsIgnoreCase("Unauthorized")) {
                    if (!fbToken.equalsIgnoreCase("false")) {
                        authenticte(fbToken);
                    }

                }

                else {
                    //  Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                    profileList = new ArrayList<>();
                    profileInfo = new ArrayList<>();


                    try {
                        final JSONObject result = new JSONObject(response);
                        profiles = (JSONArray) result.get("results");

                        for (int i = 0; i < profiles.length(); ++i) {
                            String id = profiles.getJSONObject(i).getString("_id");
                            if (!profileMap.containsKey(id))

                            {
                                profileInfo = new ArrayList<>();
                                // profileList.add(profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url")+","+profiles.getJSONObject(i).getString("_id")+","+profiles.getJSONObject(i).getString("name"));
                                profileInfo.add(profiles.getJSONObject(i).getString("name"));
                                profileInfo.add(profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url"));
                                profileInfo.add(profiles.getJSONObject(i).getString("birth_date"));
                                profileIds.add(id);
                                profileMap.put(id, profileInfo);
                            }
                        }

                        autoLike();

                    } catch (Exception e) {
                        Log.d("getrecs1", e.getMessage());

                    }
                }





            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("TinderError",error.getMessage());
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.statusCode == 401) {
                    if (!fbToken.equalsIgnoreCase("false")) {
                        authenticte(fbToken);
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("app_version", "6.9.4");
                params.put("platform", "ios");
                params.put("User-agent", "Tinder/7.5.3 (iPhone; iOS 10.3.2; Scale/2.00)");
                params.put("X-Auth-Token", tinderToken);
                return params;
            }


        };

        queue.add(sr2);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();


    }


    void autoLike() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String id=profileIds.get(count);
        count++;
        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/like/" + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                isSuccessful=true;

                if(i==19)
                {
                    createNotification();
                }
                if (i < 20) {

                    Log.d("TinderResponse",response+i);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                i++;
                                autoLike();

                            } catch (Exception e) {
                                Log.d("error10",e.getMessage());
                                getRecs();

                            }
                        }
                    }, 1000);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("errorOnAutoLike",error.getMessage());
                getRecs();


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("app_version", "6.9.4");
                params.put("platform", "ios");
                params.put("User-agent", "Tinder/7.5.3 (iPhone; iOS 10.3.2; Scale/2.00)");
                params.put("X-Auth-Token", tinderToken);
                return params;
            }


        };

        queue.add(sr2);

    }

    private void getRecs2() {
        Log.d("asdasds","dasdasd");

    }

    private void createNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("default",
                    "Channel name",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default")
                    .setContentText(likesNumber+ " people liked")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setBadgeIconType(R.mipmap.ic_launcher);

            notificationManager.notify(101, notificationBuilder.build());
        }
    }



    void authenticte(final String fbToken) {
        RequestFuture<String> requestFuture = RequestFuture.newFuture();
        {
            final JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("token", fbToken);
                jsonBody.put("id", "12238876");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            final String requestBody = jsonBody.toString();

            StringRequest sr = new StringRequest(Request.Method.POST, "https://api.gotinder.com/v2/auth", requestFuture, requestFuture) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("app_version", "6.9.4");
                    params.put("platform", "ios");
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


            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(sr);

            try {
                Log.d("errr", "getrecs");

                String response = requestFuture.get(10, TimeUnit.SECONDS);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject = (JSONObject) jsonObject.get("data");
                    String token = jsonObject.getString("api_token");
                    Log.d("auth", token);
                    SharedPreferences sharedPreferences=getSharedPreferences("api_token",MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("tinder_token",token).commit();
                    tinderToken = token;
                    getRecsSynchronous();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getRecsSynchronous();
                isSuccessful = true;
            } catch (InterruptedException e) {
                Log.d("errr", e.toString());
                isSuccessful = false;
            } catch (ExecutionException e) {
                // exception handling
                Log.d("errr", e.toString());
                isSuccessful = false;
            } catch (TimeoutException e) {
                // exception handling
                Log.d("errr", e.toString());
                isSuccessful = false;
            } catch (Exception e) {
                Log.d("errr", e.toString());
                isSuccessful = false;
            }

        }
    }


}
