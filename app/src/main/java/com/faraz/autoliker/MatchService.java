package com.faraz.autoliker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by abc on 3/13/2018.
 */

public class MatchService extends GcmTaskService {

    String currentTimeStamp,tinderToken;
    Boolean isSuccessful;
    SharedPreferences mSharedPreferences;
    int matchCount,x;
    @Override
    public void onCreate()
    {
        super.onCreate();
        isSuccessful=false;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        currentTimeStamp=df.format(new Date());
        SharedPreferences sharedPreferences = this.getSharedPreferences("api_token", MODE_PRIVATE);
        tinderToken = sharedPreferences.getString("tinder_token", "false");
        mSharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        matchCount=mSharedPreferences.getInt("matchCount",0);



    }
    @Override
    public int onRunTask(TaskParams taskParams) {
        return 0;
    }

    void checkMatches()
    {
        final JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("last_activity_date", currentTimeStamp);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody=jsonBody.toString();

        RequestFuture<String> requestFuture=RequestFuture.newFuture();
        StringRequest stringRequest=new StringRequest(Request.Method.GET,"https://api.gotinder.com/updates",requestFuture,requestFuture) {

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
        queue.add(stringRequest);

        try {
            String response= requestFuture.get(10, TimeUnit.SECONDS);
            processResponse(response);
            Log.d("response",response);
            isSuccessful=true;
        } catch (InterruptedException e) {
            // exception handling
            isSuccessful=false;
        } catch (ExecutionException e) {
            // exception handling
            isSuccessful=false;
        } catch (TimeoutException e) {
            // exception handling
            isSuccessful=false;
        } catch (Exception e)
        {
            isSuccessful=false;
        }
    }

    private void processResponse(String response) throws Exception{

        JSONObject jsonObject=new JSONObject(response);
        JSONArray jsonArray=jsonObject.getJSONArray("matches");

        SharedPreferences.Editor editor=mSharedPreferences.edit();
        editor.putInt("matchesCount",jsonArray.length()).commit();

        if(matchCount==0)
            x=0;
        else if(jsonArray.length()>matchCount)
        {
            createNotification();
        }

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
                    .setContentText("Congrats you got a new match")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setBadgeIconType(R.mipmap.ic_launcher);

            notificationManager.notify(101, notificationBuilder.build());




        }
    }
}
