package com.faraz.autoliker;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    String id,imageUrl,tinderToken;
    ImageView photo;
    List<String> images;
    ViewPager mViewPager;
    CarouselView carouselView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        images=new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("api_token", MODE_PRIVATE);
        tinderToken = sharedPreferences.getString("tinder_token", "false");

        carouselView = (CarouselView) findViewById(R.id.carouselView);

        photo=(ImageView) findViewById(R.id.image);
        id=getIntent().getStringExtra("id");
        imageUrl=getIntent().getStringExtra("imageUrl");

      //  mViewPager.setAdapter(new ImageAdapter());

        getUser();

    }



    // used for subseuent loading of profiles
    void getUser() {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/user/"+id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject jsonObject=new JSONObject(response);
                    JSONArray photosJson =jsonObject.getJSONObject("results").getJSONArray("photos");

                    for(int i=0;i<photosJson.length();++i)
                    {
                        JSONObject obj=(JSONObject)photosJson.get(i);
                        obj=(JSONObject) obj.getJSONArray("processedFiles").get(0);
                        String img=obj.getString("url");
                        images.add(img);
                    }
                    Log.d("response",images.get(0));


                    // setting viewpager

                    ImageListener imageListener = new ImageListener() {
                        @Override
                        public void setImageForPosition(int position, ImageView imageView) {
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            Picasso.with(UserActivity.this).load(images.get(position)).into(imageView);

                        }
                    };


                    carouselView.setImageListener(imageListener);
                    carouselView.setPageCount(images.size());



                } catch (JSONException e) {
                    e.printStackTrace();
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
