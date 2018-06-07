package com.faraz.autoliker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


    }

    public void scheduleAlarm() {

//21600L
        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(this);
        Task task = new OneoffTask.Builder()
                .setExecutionWindow(0,5) // occurs at *most* once this many seconds - note that you can't control when
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED) // various connectivity scenarios are available
                .setService(LikerService.class) // the GcmTaskServer you created earlier
                .setTag("LikerService")
                .setPersisted(true) // persists across reboots or not
                .build();
        gcmNetworkManager.schedule(task);
    }

    public void start(View view) {

        scheduleAlarm();

    }
}
