package com.faraz.autoliker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;


import java.sql.Timestamp;

import jonathanfinerty.once.Once;

public class MainFragmentActivity extends AppCompatActivity {

    TabLayout mTabLayout;

    private static final String DIDSERVICERUN ="YES" ;
    ViewPager mPager;
    private Tracker mTracker;
    AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recs);
        SharedPreferences sharedPreferences = getSharedPreferences("api_token", MODE_PRIVATE);
        SharedPreferences.Editor mEditor=sharedPreferences.edit();
        Toolbar toolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        App application=(App)getApplication();
        mTracker = application.getDefaultTracker();

        mTabLayout=findViewById(R.id.tab);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long x=1519917291232L;
        Log.d("time",new Timestamp(timestamp.getTime()-x)+"");

        String isServiceRunning=sharedPreferences.getString("DIDSERVICERUN","no");

        Once.initialise(this);
        String dob=getIntent().getStringExtra("dob");
        Bundle bundle=new Bundle();
        bundle.putString("dob",dob);
        bundle.putString("showTut",isServiceRunning);
        final FragmentManager manager=getSupportFragmentManager();
        final RecsFragment recsFragment=new RecsFragment();
        recsFragment.setArguments(bundle);

        mPager=(ViewPager) findViewById(R.id.viewpager);
        final Fragment[] fragments={recsFragment,new SettingsFragment(),new MatchFragment()};

        mTabLayout.setupWithViewPager(mPager);
        mPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public CharSequence getPageTitle(int position) {
                String[] x= new String[]{"Feed","Settings","Matches"};
                return x[position];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }
        });

        mPager.setOffscreenPageLimit(2);

        if(isServiceRunning.equalsIgnoreCase("no"))
        {
            Log.d("mkc","mlc");

            scheduleAlarm();
            mEditor.putString("DIDSERVICERUN",DIDSERVICERUN);
            mEditor.commit();

        }





        // ads

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);




    }

    @Override
    public void onResume()
    {
        super.onResume();
        mTracker.setScreenName("Main Screen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: {
                startActivity(new Intent(this,Preference.class));
                break;
            }
            // case blocks for other MenuItems (if any)
        }

        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_navigation_main, menu);
        return true;
    }
    // Setup a recurring alarm every half hour
    public void scheduleAlarm() {

//21600L

        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(this);

        PeriodicTask periodicTask = new PeriodicTask.Builder()

                .setPeriod(43200L) // occurs at *most* once this many seconds - note that you can't control when
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED) // various connectivity scenarios are available
                .setService(LikerService.class) // the GcmTaskServer you created earlier
                .setTag("LikerService").setUpdateCurrent(true)
                .setPersisted(true) // persists across reboots or not
                .build();
        gcmNetworkManager.schedule(periodicTask);
    }


}
