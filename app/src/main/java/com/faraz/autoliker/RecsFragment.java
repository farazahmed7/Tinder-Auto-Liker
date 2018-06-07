package com.faraz.autoliker;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecsFragment extends Fragment {


    AlertDialog.Builder builder;
    boolean repeat;
    LayoutInflater mLayoutInflater;
    boolean liked;
    ProgressDialog mProgressDialog;
    List<String> images;
    HashMap<Integer,Boolean> isLiked,isSuperLiked;
    HashMap<String,List<String>> profileMap;
    Handler handler;
    List<String> profileInfo;
    ArrayList<String> profileList;
    List<String> profileIds;
    ProgressBar mProgressBar;
    RecyclerView mList;
    String tinderToken;
    private EndlessRecyclerViewScrollListener scrollListener;
    ListAdapter mListAdapter;
    JSONArray profiles,profilesForAutoLike;
    private FloatingActionButton mFloatingActionButton;
    int i = 0,count=0;
    String dobString;
    Calendar dob,today;
    SwipeRefreshLayout mSwipeRefreshLayout;
    int index;
    private boolean hasProfiles;
    String likesNumber;
    String showTut;
    private boolean superLiked;


    public RecsFragment() {
        // Required empty public constructor
    }



    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(Purchase purchase) {

            Log.d("purchase",purchase.toString());
            SharedPreferences sharedPreferences=getActivity().getSharedPreferences("data",MODE_PRIVATE);

            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean("isInApp",true).commit();

        }

        @Override
        public void onError(int response, Exception e) {
            // handle errors here
        }
    }

    private final ActivityCheckout mCheckout = Checkout.forActivity(getActivity(), App.get().getBilling());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_recs, container, false);
        mLayoutInflater=LayoutInflater.from(getActivity());
        isLiked=new HashMap<>();
        isSuperLiked=new HashMap<>();
        images=new ArrayList<>();
        mList=(RecyclerView)v.findViewById(R.id.rec_list);
        liked=true;
        superLiked=true;
        profilesForAutoLike=new JSONArray();
        mProgressDialog=new ProgressDialog(getActivity());
        handler=new Handler();
        mProgressBar=(ProgressBar) v.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("api_token", MODE_PRIVATE);
        tinderToken = sharedPreferences.getString("tinder_token", "false");
        likesNumber=getActivity().getSharedPreferences("data",MODE_PRIVATE).getString("inApplikesNumber","100");

        showTut=getArguments().getString("showTut");
        final GridLayoutManager linear=new GridLayoutManager(getActivity(),2);
        mList.setLayoutManager(linear);
        mListAdapter=new ListAdapter();
        mFloatingActionButton=(FloatingActionButton)v.findViewById(R.id.fab);
        dobString=getArguments().getString("dob");
        dob=Calendar.getInstance();
        today = Calendar.getInstance();
        mSwipeRefreshLayout=v.findViewById(R.id.swipeLayout);



        Log.d("token",tinderToken);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRecs();
                isLiked.clear();
                isSuperLiked.clear();
            }
        });

        SharedPreferences sharedPreferences1=getActivity().getSharedPreferences("data",MODE_PRIVATE);
        final SharedPreferences.Editor mEditor=sharedPreferences1.edit();
        final String lastDate=sharedPreferences1.getString("date","0");
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Date currentDate=new Date();
                int hm[]=checkDate(currentDate.getTime(),lastDate);
                boolean isInApp=getActivity().getSharedPreferences("data",MODE_PRIVATE).getBoolean("isInApp",false);

                if(hm[0]>=1 || isInApp ) {
                    Toast.makeText(getActivity(), hm[0] + " " + hm[1], Toast.LENGTH_SHORT).show();
                    mProgressDialog.setMax(Integer.parseInt(likesNumber));
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setMessage("Liking profiles.Please wait...");

                    likedProfile = new ArrayList<>();

                    count = 0;
                    profileMapforAutolike = new LinkedHashMap<>();
                    profileIdsForAutoLike = new ArrayList<>();
                    repeat = true;
                    i = 0;
                    mEditor.putString("date",currentDate.getTime()+"").commit();
                    getRecsforAutoLiker();

                }

                else
                {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage("Please wait for " +(60-hm[1])+" mins" +"\n" +"Unlock unlimited in app likes");
                    alertDialogBuilder.setPositiveButton("Unlock",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

// BUY



                                }
                            });



                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();


                }


            }
        });






        // Detecting the end of the recyclerView
        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                int visibleItemCount = linear.getChildCount();
                int totalItemCount = linear.getItemCount();
                int firstVisibleItemPosition = linear.findFirstVisibleItemPosition();


                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {


                    getRecs2();
                }


            }
        });

        mList.addItemDecoration(new SpacesItemDecoration(20));
        // Loading the first recommendation list
        getRecs();


        mCheckout.start();

        mCheckout.loadInventory(Inventory.Request.create().loadAllPurchases(), new InventoryCallback());

        return v;
    }


    int[] checkDate(long current, String start)
    {
        long end =Long.parseLong(start);
        long secs=(current-end)/1000;
        int hours= (int) (secs/3600);
        secs=secs%3600;
        int mins= (int) (secs/60);
        secs=secs%60;

        return new int[]{hours,mins};
    }


    @Override
   public void onDestroy() {
        mCheckout.stop();
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCheckout.onActivityResult(requestCode, resultCode, data);
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(@Nonnull Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                // billing is not supported, user can't purchase anything. Don't show ads in this
                // case
                return;
            }
            if (product.isPurchased("inAppLikes")) {
                Log.d("purchase","purchased");
                SharedPreferences sharedPreferences=getActivity().getSharedPreferences("data",MODE_PRIVATE);

                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("isInApp",true).commit();
                return;
            }

        }
    }

    class ListHolder extends RecyclerView.ViewHolder
    {
        TextView nameTextView,bio;
        ImageView image,heart,star;
        int pos;
        String id;
        RelativeLayout parent;
        ImageButton info;
        String imageUrl;
        public ListHolder(View itemView) {
            super(itemView);
            bio=itemView.findViewById(R.id.bio);
            image=(ImageView)itemView.findViewById(R.id.image);
            nameTextView=(TextView) itemView.findViewById(R.id.name);
            parent=(RelativeLayout) itemView.findViewById(R.id.parent);
            star=itemView.findViewById(R.id.superliked);
            info=itemView.findViewById(R.id.info);
            heart=itemView.findViewById(R.id.liked);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    like(id);

                    if(liked) {
                        isLiked.put(pos, true);
                        heart.setVisibility(View.VISIBLE);
                    }
                }
            });

            image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    SuperLike(id);

                    if(superLiked) {
                        isSuperLiked.put(pos, true);
                        star.setVisibility(View.VISIBLE);
                    }

                    return true;
                }
            });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder=new AlertDialog.Builder(getActivity());
                Intent i=new Intent(getActivity(),UserActivity.class);
                i.putExtra("id",id);
                i.putExtra("imageUrl",imageUrl);
                View v=getLayoutInflater().inflate(R.layout.activity_user,null);

                CarouselView carouselView = v. findViewById(R.id.carouselView);
                TextView bio =v.findViewById(R.id.bio);


                builder.setView(v);
                getUser(id,carouselView,bio);

                AlertDialog alertDialog=builder.create();
                alertDialog.show();
            }
        });

        }
    }

    class ListAdapter extends RecyclerView.Adapter<ListHolder>
    {

        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater=LayoutInflater.from(getActivity());
            View v=layoutInflater.inflate(R.layout.rec_recyclerview_list,parent,false);
            return new ListHolder(v);
        }

        @Override
        public void onBindViewHolder(ListHolder holder, int position) {
            List<String> info = profileMap.get(profileIds.get(position));
            Picasso.with(getActivity()).load(info.get(1)).into(holder.image);
            holder.pos=position;
            holder.id=profileIds.get(position);
            holder.imageUrl=info.get(1);
            String dobString=info.get(2);
            dob.set(Integer.parseInt(dobString.substring(0,4)),Integer.parseInt(dobString.substring(5,7)),Integer.parseInt(dobString.substring(8,10)));
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                age--;
            }
            holder.nameTextView.setText(info.get(0)+","+age+"");
            if(isLiked.containsKey(position) && liked)
            holder.heart.setVisibility(View.VISIBLE);
            else
                holder.heart.setVisibility(View.INVISIBLE);

            if(isSuperLiked.containsKey(position) && superLiked)
                holder.star.setVisibility(View.VISIBLE);
            else
                holder.star.setVisibility(View.INVISIBLE);


            if(position==2 && showTut.equalsIgnoreCase("no"))
            {
                showTut="yes";

                new MaterialTapTargetPrompt.Builder(getActivity())
                        .setTarget(holder.parent)
                        .setPrimaryText("Tap info icon to see details\n\nTap to like\nHold to super like")
                        .setBackgroundColour(getResources().getColor(R.color.colorPrimary))
                        .setPromptBackground(new RectanglePromptBackground())
                    .setPromptFocal(new RectanglePromptFocal())
                        .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                        {
                            @Override
                            public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                            {
                                if (state == MaterialTapTargetPrompt.STATE_DISMISSING)
                                {

                                    new MaterialTapTargetPrompt.Builder(getActivity())
                                            .setBackgroundColour(getResources().getColor(R.color.colorPrimary))
                                            .setTarget(mFloatingActionButton)
                                            .setPrimaryText("Tap this to start autoliking")
                                            .show();

                                }
                            }
                        })
                        .show();
            }



        }

        @Override
        public int getItemCount() {
            return profileMap.size();
        }
    }

    //liking a user
    void like(final String id) {

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/like/" + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.contains("true"))
                {

                    builder=new AlertDialog.Builder(getActivity(),R.style.Theme_AppCompat_Dialog);

                    View v=getLayoutInflater().inflate(R.layout.activity_user,null);

                    CarouselView carouselView = v. findViewById(R.id.carouselView);
                    TextView matched=v.findViewById(R.id.matched);

                    TextView bio =v.findViewById(R.id.bio);

                    matched.setVisibility(View.VISIBLE);
                    builder.setView(v);
                    getUser(id,carouselView,bio);

                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();

                    Window window = alertDialog.getWindow();
                    //window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);



                }
                try {
                    JSONObject object=new JSONObject(response);
                    object.getString("rate_limited_until");
                    Snackbar.make(mList,"Out of likes bruh",Snackbar.LENGTH_SHORT).show();
                    liked=false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    liked=true;
                    Snackbar.make(mList,response,Snackbar.LENGTH_SHORT).show();

                }
                mListAdapter.notifyDataSetChanged();



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



    void SuperLike(String id) {

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        StringRequest sr2 = new StringRequest(Request.Method.POST, "https://api.gotinder.com/like/" + id+"/super", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Snackbar.make(mList,response,Snackbar.LENGTH_SHORT).show();
               if(response.contains("0"))
               superLiked=false;
               else
                   superLiked=true;
                mListAdapter.notifyDataSetChanged();


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

    // used for subseuent loading of profiles
    void getRecs2() {

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/user/recs", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                Log.d("response123",response);
                int lastSize=profileMap.size();

                mProgressBar.setVisibility(View.GONE);
                try {
                    final JSONObject result = new JSONObject(response);
                    profiles = (JSONArray) result.get("results");


                    for(int i=0;i<profiles.length();++i)
                    {
                        String id = profiles.getJSONObject(i).getString("_id");
                        if (!profileMap.containsKey(id))

                        {
                            profileInfo=new ArrayList<>();
                            // profileList.add(profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url")+","+profiles.getJSONObject(i).getString("_id")+","+profiles.getJSONObject(i).getString("name"));
                            profileInfo.add(profiles.getJSONObject(i).getString("name"));
                            profileInfo.add(profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url"));
                            profileInfo.add(profiles.getJSONObject(i).getString("birth_date"));
                            profileIds.add(id);
                            profileMap.put(id,profileInfo);
                        }
                    }

                    int latestSize=profileMap.size();

                    Log.d("sizee",lastSize-latestSize+"");
                    if(latestSize-lastSize>0) // check whether the total size of list grows or not
                   mListAdapter.notifyItemInserted(profileMap.size()-1);
                } catch (Exception e) {
                    Log.d("getrecs1", e.getMessage());


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

        mProgressBar.setVisibility(View.VISIBLE);
        queue.add(sr2);

    }

    void getRecs() {

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/user/recs", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                mSwipeRefreshLayout.setRefreshing(false);
                //  Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                profileList=new ArrayList<>();
                profileInfo=new ArrayList<>();
                profileMap=new HashMap<>();
                profileIds=new ArrayList<>();

                try {
                    final JSONObject result = new JSONObject(response);
                    profiles = (JSONArray) result.get("results");

                    for(int i=0;i<profiles.length();++i) {
                        String id = profiles.getJSONObject(i).getString("_id");
                        if (!profileMap.containsKey(id))

                        {
                            profileInfo=new ArrayList<>();
                            // profileList.add(profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url")+","+profiles.getJSONObject(i).getString("_id")+","+profiles.getJSONObject(i).getString("name"));
                            profileInfo.add(profiles.getJSONObject(i).getString("name"));
                            profileInfo.add(profiles.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url"));
                            profileInfo.add(profiles.getJSONObject(i).getString("birth_date"));


                            profileIds.add(id);

                            profileMap.put(id,profileInfo);
                        }
                    }
                    mList.setAdapter(mListAdapter);
                    mListAdapter.notifyItemInserted(14);



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


    // used for subseuent loading of profiles
    void getUser(String id, final CarouselView carouselView, final TextView biotv) {

        images=new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/user/"+id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("xxx",response);

                try {
                    JSONObject jsonObject=new JSONObject(response);
                    JSONArray photosJson =jsonObject.getJSONObject("results").getJSONArray("photos");
                    try {
                       String bio = jsonObject.getJSONObject("results").getString("bio");
                        biotv.setText(bio);

                    }
                    catch (Exception e)
                    {

                    }
                    for(int i=0;i<photosJson.length();++i)
                    {
                        JSONObject obj=(JSONObject)photosJson.get(i);
                        obj=(JSONObject) obj.getJSONArray("processedFiles").get(0);
                        String img=obj.getString("url");
                        images.add(img);
                    }



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

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(Context context, int itemOffsetId) {
            this(itemOffsetId);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }
    
    
    //code for autoliking



    private ArrayList<String> profileInfoForAutoLike,profileIdsForAutoLike,likedProfile;
    LinkedHashMap<String,List<String>> profileMapforAutolike,profileMapforAutolike2;
    String id;

    int getRecsforAutoLiker() {
        Log.d("getRecs","getRecs");
        Context context=getContext();
        if(context==null)
            return 0;

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest sr2 = new StringRequest(Request.Method.GET, "https://api.gotinder.com/user/recs", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                int lastSize=profileMapforAutolike.size();

                //  Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();




                    try {
                        final JSONObject result = new JSONObject(response);
                        profilesForAutoLike = (JSONArray) result.get("results");

                        for (int i = 0; i < profilesForAutoLike.length(); ++i) {
                            String id = profilesForAutoLike.getJSONObject(i).getString("_id");
                            if (!profileMapforAutolike.containsKey(id))

                            {
                                profileInfoForAutoLike = new ArrayList<>();
                                profileInfoForAutoLike.add(profilesForAutoLike.getJSONObject(i).getString("name"));
                                profileInfoForAutoLike.add(profilesForAutoLike.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url"));
                                profileInfoForAutoLike.add(profilesForAutoLike.getJSONObject(i).getString("birth_date"));
                                profileIdsForAutoLike.add(id);
                                profileMapforAutolike.put(id, profileInfoForAutoLike);
                            }
                        }

                        int latestSize=profileMapforAutolike.size();



                        if(latestSize-lastSize>0)
                            hasProfiles=true;
                        else
                            hasProfiles=false;


                        mProgressDialog.show();

                        autoLike();

                    } catch (Exception e) {
                        Log.d("getrecs1", e.getMessage());

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


return 1;
    }

    boolean isOutOfLikes;
    void autoLike()
    {

        RequestQueue queue = Volley.newRequestQueue(getActivity());
         id=profileIdsForAutoLike.get(count);
        Log.d("iddd",id);
        count++;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://api.gotinder.com/like/" + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {




                try {

                    if(response.contains("rate_limited_until"))
                    isOutOfLikes=true;

                    if(count==1 && isOutOfLikes)
                    {                    Log.d("out",isOutOfLikes+"");

                        open();
                    }

                    if(count!=0 && isOutOfLikes) {
                        open();
                        Toast.makeText(getActivity(), "Out of likes", Toast.LENGTH_SHORT).show();
                    }


                    if(count==Integer.parseInt(likesNumber) )
                    {
                        open();
                        likedProfile.add(id);


                    }
                    if (count < Integer.parseInt(likesNumber) && !isOutOfLikes) {

                        Log.d("TinderResponse", response + count);
                        mProgressDialog.setProgress(count);
                        likedProfile.add(id);
                        autoLike();
                    }

                } catch (Exception e) {

                   Log.d("autolike",e.getMessage());
                    mProgressDialog.setProgress(count);
                    getRecsforAutoLiker();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                
                getRecsforAutoLiker();
                
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


        queue.add(stringRequest);

     
    }


    public void open(){

        mProgressDialog.hide();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Show Liked Profiles");
                alertDialogBuilder.setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                profileMapforAutolike2 = new LinkedHashMap<>();

                                int i = 1;
                                for (Map.Entry e : profileMapforAutolike.entrySet()) {

                                    profileMapforAutolike2.put((String) e.getKey(), (List<String>) e.getValue());
                                    if (i == Integer.parseInt(likesNumber))
                                        break;

                                    i++;

                                }

                                Intent intent = new Intent(getActivity(), NotificationResultActivity.class);
                                intent.putStringArrayListExtra("profileIds",likedProfile);
                                intent.putExtra("profileMap", profileMapforAutolike2);
                                startActivity(intent);




                            }
                        });



        AlertDialog alertDialog = alertDialogBuilder.create();

        if(likedProfile.size()>0)
        alertDialog.show();

        else
            Toast.makeText(getActivity(), "You are out of likes !", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume()
    {
        super.onResume();
    }
}
