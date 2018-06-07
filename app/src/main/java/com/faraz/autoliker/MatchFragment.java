package com.faraz.autoliker;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.faraz.autoliker.R;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatchFragment extends Fragment {



    Calendar dob,today;


    JSONArray resultArray;
    TextView matchno;
    private AlertDialog.Builder builder;
    private ArrayList<String> images;
    SwipeRefreshLayout mSwipeRefreshLayout;

    public MatchFragment() {
        // Required empty public constructor
    }
    RecyclerView mRecyclerView;
    String tinderToken;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v= inflater.inflate(R.layout.fragment_match, container, false);
        mRecyclerView=v.findViewById(R.id.rv);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("api_token", MODE_PRIVATE);
        tinderToken = sharedPreferences.getString("tinder_token", "false");

        matchno=v.findViewById(R.id.matchno);
        today=Calendar.getInstance();
        dob=Calendar.getInstance();


        SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences("data", MODE_PRIVATE);
        String pd=sharedPreferences1.getString("date","0");
        long previousDate=Long.parseLong(pd);
        long currentDate=new Date().getTime();

        mSwipeRefreshLayout=v.findViewById(R.id.swipe);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMatches();
            }
        });

        long secs=(currentDate-previousDate)/1000;
        int hours= (int) (secs/3600);
        secs=secs%3600;
        int mins= (int) (secs/60);
        secs=secs%60;

        Toast.makeText(getActivity(), hours+" "+ mins, Toast.LENGTH_SHORT).show();

        getMatches();
        Log.d("token",tinderToken);
        Log.d("date",getDate());
        return v;

    }

     public class ListHolder extends RecyclerView.ViewHolder
     {

         TextView nameTextView;
         ImageView image,star;
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
                     builder=new AlertDialog.Builder(getActivity());
                     Intent i=new Intent(getActivity(),UserActivity.class);
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


     public class ListAdapter extends RecyclerView.Adapter<ListHolder>
     {

         @Override
         public ListHolder onCreateViewHolder( ViewGroup parent, int viewType)

         {
             LayoutInflater layoutInflater=LayoutInflater.from(getActivity());
             View v=layoutInflater.inflate(R.layout.rec_recyclerview_list,parent,false);
             return new ListHolder(v);
         }

         @Override
         public void onBindViewHolder( ListHolder holder, int position) {

             String name,dobString;
             JSONObject  object;
             int age;

             try {
                object=resultArray.getJSONObject(position).getJSONObject("person");
                 holder.imageUrl=object.getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url");
                 Picasso.with(getActivity()).load(holder.imageUrl).into(holder.image);
                 dobString=object.getString("birth_date");
                 holder.id=object.getString("_id");

                 dob.set(Integer.parseInt(dobString.substring(0,4)),Integer.parseInt(dobString.substring(5,7)),Integer.parseInt(dobString.substring(8,10)));
                  age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                 if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                     age--;
                 }

                 holder.nameTextView.setText(object.getString("name")+"     "+age+"");



             } catch (JSONException e) {
                 e.printStackTrace();
             }


         }

         @Override
         public int getItemCount() {
             return resultArray.length();
         }
     }


    void getMatches()
    {

        final JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("last_activity_date", getDate());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody=jsonBody.toString();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest sr = new StringRequest(Request.Method.POST,"https://api.gotinder.com/updates", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mSwipeRefreshLayout.setRefreshing(false);

                Log.d("responseeee",response);
                try {
                    resultArray=new JSONObject(response).getJSONArray("matches");
                    matchno.setText("Matches in last 6 months("+resultArray.length()+")");
                //    Log.d("response",thumbnail+"");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
               mRecyclerView.setAdapter(new ListAdapter());

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
                params.put("X-Auth-Token",tinderToken);
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

     public String getDate()
     {
         TimeZone tz = TimeZone.getTimeZone("UTC");
         Calendar c=Calendar.getInstance();
         c.setTime(new Date());
         c.add(Calendar.MONTH,-6);
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ms'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
         df.setTimeZone(tz);
         return df.format(c.getTime());
     }


    void getUser(String id, final CarouselView carouselView) {

        images = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(getActivity());

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
                            Picasso.with(getActivity()).load(images.get(position)).into(imageView);

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
