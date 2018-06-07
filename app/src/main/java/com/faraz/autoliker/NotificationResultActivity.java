package com.faraz.autoliker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationResultActivity extends AppCompatActivity {

    private ArrayList<String> profileList,profileInfo,profileIds;
    JSONArray profiles;
    String tinderToken;
    HashMap<String,List<String>> profileMap;
    Calendar dob,today;
    RecyclerView mRecyclerView;
    private AlertDialog.Builder builder;
    ArrayList<String> images;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_result);

        SharedPreferences sharedPreferences =getSharedPreferences("api_token", MODE_PRIVATE);
        today=Calendar.getInstance();
        dob=Calendar.getInstance();
        tinderToken = sharedPreferences.getString("tinder_token", "false");
         profileMap = (HashMap<String, List<String>>)getIntent().getSerializableExtra("profileMap");
        profileIds=getIntent().getStringArrayListExtra("profileIds");

        mRecyclerView=findViewById(R.id.rv);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
        List<String> info = profileMap.get(profileIds.get(0));
        String x=profileMap.get(profileIds.get(0)).get(1);
        String y=profileMap.get(profileIds.get(1)).get(1);
        Toast.makeText(this, profileMap.keySet()+"", Toast.LENGTH_SHORT).show();
        Log.d("liked",profileIds.get(0)+"  "+profileIds.get(1));
        Log.d("keyset",profileMap.get(profileIds.get(0))+"  "+ profileMap.get(profileIds.get(1)));

         mRecyclerView.setAdapter(new ListAdapter());
    }





    class ListHolder extends RecyclerView.ViewHolder
    {

        TextView nameTextView;
        ImageView image,star;
        int pos;
        String id;
        RelativeLayout parent;
        String imageUrl;


        public ListHolder(View itemView) {
            super(itemView);
            image=(ImageView)itemView.findViewById(R.id.image);
            nameTextView=(TextView) itemView.findViewById(R.id.name);
            parent=(RelativeLayout) itemView.findViewById(R.id.parent);

            image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    builder=new AlertDialog.Builder(NotificationResultActivity.this);
                    Intent i=new Intent(NotificationResultActivity.this,UserActivity.class);
                    i.putExtra("id",id);
                    i.putExtra("imageUrl",imageUrl);
                    View v=getLayoutInflater().inflate(R.layout.activity_user,null);

                    CarouselView carouselView = v. findViewById(R.id.carouselView);


                    builder.setView(v);
                    getUser(id,carouselView);

                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }
    }



    class ListAdapter extends RecyclerView.Adapter<ListHolder>
    {


        @NonNull
        @Override
        public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater=LayoutInflater.from(NotificationResultActivity.this);
            View v=layoutInflater.inflate(R.layout.rec_recyclerview_list,parent,false);
            return new ListHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ListHolder holder, int position) {



            List<String> info = profileMap.get(profileIds.get(position));
            Picasso.with(NotificationResultActivity.this).load(info.get(1)).into(holder.image);
            holder.pos=position;
            holder.id=profileIds.get(position);
            holder.imageUrl=info.get(1);
            String dobString=info.get(2);
            dob.set(Integer.parseInt(dobString.substring(0,4)),Integer.parseInt(dobString.substring(5,7)),Integer.parseInt(dobString.substring(8,10)));
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                age--;
            }
            holder.nameTextView.setText(info.get(0)+"     "+age+"");


        }

        @Override
        public int getItemCount() {
            return profileMap.size();
        }
    }


    // modyfing gridlayout
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0 && parent.getChildLayoutPosition(view) == 1) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }

    void getUser(String id, final CarouselView carouselView) {

        images = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(NotificationResultActivity.this);

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
                            Picasso.with(NotificationResultActivity.this).load(images.get(position)).into(imageView);

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
