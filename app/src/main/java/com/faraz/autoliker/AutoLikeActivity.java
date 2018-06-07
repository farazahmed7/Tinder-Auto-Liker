package com.faraz.autoliker;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AutoLikeActivity extends AppCompatActivity {

    String tinderToken;
    TextView mTextView;
    int i = 0;
    FloatingActionButton mFloatingActionButton;
    Handler handler;
    String recsResponse;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_like);
        mTextView = (TextView) findViewById(R.id.response);
        handler = new Handler();
        mImageView=(ImageView) findViewById(R.id.image);
        SharedPreferences sharedPreferences = getSharedPreferences("api_token", MODE_PRIVATE);
        tinderToken = sharedPreferences.getString("tinder_token", "false");
        getRecs();


    }

    void getRecs() {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/user/recs", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                recsResponse = response;
              //  Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();

                try {
                    final JSONObject result = new JSONObject(response);
                    JSONArray profiles = (JSONArray) result.get("results");
                    String id = profiles.getJSONObject(0).getString("_id");
                    like(id);

                } catch (Exception e) {

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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

    void like(String id) {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/like/" + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (i < 5) {
                    try {
                        final JSONObject result = new JSONObject(recsResponse);

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    i++;
                                    JSONArray profiles = (JSONArray) result.get("results");
                                    String id = profiles.getJSONObject(i).getString("_id");
                                    mTextView.setText(profiles.getJSONObject(i).getString("name") + "");
                                    String url=profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url");
                                    Picasso.with(AutoLikeActivity.this).load(url).into(mImageView);
                                    like(id);


                                } catch (Exception e) {

                                }
                            }
                        }, 5000);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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





    }



