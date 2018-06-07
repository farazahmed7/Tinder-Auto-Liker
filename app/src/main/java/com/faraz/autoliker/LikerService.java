package com.faraz.autoliker;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */

public class LikerService extends GcmTaskService {

    Handler mHandler;
    ArrayList<String> likedProfiles;
    private ArrayList<String> profileList,profileInfo,profileIds;
    LinkedHashMap<String,List<String>> profileMap;
    JSONArray profiles;
    int i ,count;
    String tinderToken;
    boolean isSuccessful;
    String likesNumber,fbToken;
    Timestamp mTimestamp;
    boolean succes=false;
    boolean hasProfiles;
    private boolean isOutOfLikes;


    @Override
    public void onCreate() {
        super.onCreate();
        isSuccessful=false;
        hasProfiles=true;
        profileMap=new LinkedHashMap<>();
        profileIds=new ArrayList<>();
        likedProfiles=new ArrayList<>();
        mHandler = new Handler();
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
         likesNumber =sharedPreferences.getString("likesNumber","20");
        mTimestamp = new Timestamp(System.currentTimeMillis());
        i=0;
        count=0;

    }


    @Override
    public int onRunTask(TaskParams taskParams) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("api_token", MODE_PRIVATE);
        tinderToken = sharedPreferences.getString("tinder_token", "false");
        SharedPreferences sharedPreferences2=getSharedPreferences("data",MODE_PRIVATE);
        fbToken=sharedPreferences2.getString("fb_token","false");
        SharedPreferences.Editor mEditor =sharedPreferences.edit();
        Log.d("TinderService","Tinder"+likesNumber);

       // getRecs();
        authenticte(fbToken);
        if(succes) {
            Log.d("isSuccesfull",succes+"zczczcx");

            return GcmNetworkManager.RESULT_SUCCESS;
        }
        else {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
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

                int lastSize=profileMap.size();
                if(profiles.length()==0)
                {
                 hasProfiles=false;
                }
                else {
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
                }
                int latestSize=profileMap.size();

                if(latestSize-lastSize>0)
                    hasProfiles=true;
                else
                    hasProfiles=false;

                autoLikeSynchronous();

            } catch (Exception e) {
                Log.d("getrecs1", e.getMessage());
                if(e.getMessage().contains("No value"))
                createNotification(count);

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
            if(response.contains("rate_limited_until"))
                isOutOfLikes=true;


      /*      if(isOutOfLikes && count==1)
            {
             //   createNotification(0);
            }
            if(count>0 && isOutOfLikes)
            {
                //isSuccessful=true;
                succes=true;
                Log.d("mkc",isSuccessful+"");
                createNotification(count+1);
            }
*/
            if(count==Integer.parseInt(likesNumber) && hasProfiles)
            {
                //isSuccessful=true;
                likedProfiles.add(id);
                succes=true;
                Log.d("mkc",isSuccessful+"");
                createNotification(count);
            }
            if (count < Integer.parseInt(likesNumber) && hasProfiles) {

                Log.d("TinderResponse", response + count);
                likedProfiles.add(id);
                autoLikeSynchronous();
            }

        } catch (Exception e) {

            if (hasProfiles)
            getRecsSynchronous();
            else
                createNotification(count);
            isSuccessful=false;
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

                            autoLikeSynchronous();

                        } catch (Exception e) {
                            Log.d("getrecs2", e.getMessage());

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




    private void getRecs2() {
        Log.d("asdasds","dasdasd");

    }



    LinkedHashMap<String,List<String>> profileMapforAutolike2;
    private void createNotification(int peopleLiked) {



        profileMapforAutolike2 = new LinkedHashMap<>();

        int i = 1;
        for (Map.Entry e : profileMap.entrySet()) {

            profileMapforAutolike2.put((String) e.getKey(), (List<String>) e.getValue());
            if (i == Integer.parseInt(likesNumber))
                break;

            i++;

        }

        Intent intent = new Intent(this, NotificationResultActivity.class);
        intent.putStringArrayListExtra("profileIds",likedProfiles);
        intent.putExtra("profileMap", profileMapforAutolike2);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putStringArrayListExtra("likedProfiles",likedProfiles);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);



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
                    .setContentText(peopleLiked+ " people liked")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pendingIntent)
                    .setBadgeIconType(R.mipmap.ic_launcher);

            notificationManager.notify(101, notificationBuilder.build());


        }

        else

        {

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentText(peopleLiked+ " people liked")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pendingIntent)
                    .setBadgeIconType(R.mipmap.ic_launcher);

            Notification noti = new Notification.Builder(this)
                    .setContentTitle(peopleLiked+ " people liked")
                    .setContentText("Subject").setSmallIcon(R.drawable.icon)
                    .setContentIntent(pendingIntent)
                 .build();

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(0, noti);        }
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getRecsSynchronous();
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
