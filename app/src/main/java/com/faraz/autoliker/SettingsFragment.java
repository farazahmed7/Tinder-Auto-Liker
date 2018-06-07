package com.faraz.autoliker;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.angmarch.views.NiceSpinner;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    EditText mSetSecs;
    ExpandableLayout ep;
    NiceSpinner spinner,bgLikeSpinner,inAppLikeSpinner;
    SwitchCompat mSwitchCompat;
    Button set,logOut;
    Boolean ultimateBgLikes;
    GcmNetworkManager gcmNetworkManager;
    public SettingsFragment() {
        // Required empty public constructor
    }


    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(Purchase purchase) {

            Log.d("purchase",purchase.toString());
            SharedPreferences sharedPreferences=getActivity().getSharedPreferences("data",MODE_PRIVATE);

            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean("isBgLikes",true).commit();

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
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_settings, container, false);
        gcmNetworkManager = GcmNetworkManager.getInstance(getActivity());
        mSwitchCompat=v.findViewById(R.id.onoff);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", MODE_PRIVATE);
        String likesNumber =sharedPreferences.getString("likesNumber","0");
        mSetSecs =v.findViewById(R.id.sec);
        bgLikeSpinner=v.findViewById(R.id.bg_like_spinner);
        set=v.findViewById(R.id.set);
        ep=v.findViewById(R.id.expand);
        spinner=v.findViewById(R.id.nice_spinner);
        inAppLikeSpinner=v.findViewById(R.id.inapp_like_spinner);
        logOut=v.findViewById(R.id.log_out);
        ultimateBgLikes=sharedPreferences.getBoolean("isBgLikes",false);


        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSharedPreferences("data",MODE_PRIVATE).edit().clear().apply();
                getActivity().getSharedPreferences("api_token",MODE_PRIVATE).edit().clear().apply();
                Intent i=new Intent(getActivity(),FBLoginActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });
        final List<String> options=new ArrayList();
       for(int i=12;i>=1;--i)
           options.add(i+"");

       spinner.attachDataSource(options);

       spinner.setSelectedIndex(sharedPreferences.getInt("hours",0));
       spinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               i=12-i;

               if(i>6) {
                   gcmNetworkManager.cancelAllTasks(LikerService.class);
                   gcmNetworkManager.cancelTask("LikerService", LikerService.class);
                   sharedPreferences.edit().putInt("hours", i);
                   scheduleAlarm((i) * 3600);
               }
               else
               {
                   if(ultimateBgLikes)
                   {
                       gcmNetworkManager.cancelAllTasks(LikerService.class);
                       gcmNetworkManager.cancelTask("LikerService", LikerService.class);
                       sharedPreferences.edit().putInt("hours", i);
                       scheduleAlarm((i) * 3600);

                   }
                   else
                   {
                       //buy

                   }
               }
           }
       });

       final List<String> nLikesbg= new ArrayList<>();
       nLikesbg.add("20 Likes"); nLikesbg.add("50 Likes"); nLikesbg.add("100 Likes"); nLikesbg.add("150 Likes");

       bgLikeSpinner.attachDataSource(nLikesbg);

       if (likesNumber.equalsIgnoreCase("20"))
           bgLikeSpinner.setSelectedIndex(0);
       else    if (likesNumber.equalsIgnoreCase("50"))
           bgLikeSpinner.setSelectedIndex(1);
       else    if (likesNumber.equalsIgnoreCase("100"))
           bgLikeSpinner.setSelectedIndex(2);
       else    if (likesNumber.equalsIgnoreCase("150"))
           bgLikeSpinner.setSelectedIndex(3);

        final SharedPreferences.Editor mEditor=sharedPreferences.edit();

        bgLikeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               switch (i)
               {
                   case 0:
                       mEditor.putString("likesNumber","20").commit();
                       break;
                   case 1:
                       mEditor.putString("likesNumber","50").commit();
                       break;
                   case 2:
                       mEditor.putString("likesNumber","100").commit();
                       break;
                   case 3:
                       mEditor.putString("likesNumber","150").commit();
                       break;


               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {

           }
       });

     inAppLikeSpinner.attachDataSource(nLikesbg);

     String inAppLikesNumber=getActivity().getSharedPreferences("data",MODE_PRIVATE).getString("inApplikesNumber","100");

        if (inAppLikesNumber.equalsIgnoreCase("20"))
            inAppLikeSpinner.setSelectedIndex(0);
        else    if (inAppLikesNumber.equalsIgnoreCase("50"))
            inAppLikeSpinner.setSelectedIndex(1);
        else    if (inAppLikesNumber.equalsIgnoreCase("100"))
            inAppLikeSpinner.setSelectedIndex(2);
        else    if (inAppLikesNumber.equalsIgnoreCase("150"))
            inAppLikeSpinner.setSelectedIndex(3);



        inAppLikeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

             switch (i) {
                 case 0:
                     mEditor.putString("inApplikesNumber", "20").commit();
                     break;
                 case 1:
                     mEditor.putString("inApplikesNumber", "50").commit();
                     break;
                 case 2:
                     mEditor.putString("inApplikesNumber", "100").commit();
                     break;
                 case 3:
                     mEditor.putString("inApplikesNumber", "150").commit();
                     break;
             }

             }

         @Override
         public void onNothingSelected(AdapterView<?> adapterView) {

         }
     });







        if(sharedPreferences.getString("DIDSERVICERUN","no").equalsIgnoreCase("no"))
        {
        }





        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               int x= inAppLikeSpinner.getSelectedIndex()+1;
                Toast.makeText(getActivity(), x+"", Toast.LENGTH_SHORT).show();
                gcmNetworkManager.cancelAllTasks(LikerService.class);
                gcmNetworkManager.cancelTask("LikerService",LikerService.class);
                scheduleAlarm(Integer.parseInt(mSetSecs.getText().toString()));

            }
        });

        boolean s=getActivity().getSharedPreferences("data",MODE_PRIVATE).getBoolean("switch",true);

        mSwitchCompat.setChecked(s);

        if(s)
            ep.setExpanded(true);
        else
            ep.setExpanded(false);


        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                mEditor.putBoolean("switch",b).commit();

                if(b)
                    ep.expand();
                else {
                    ep.collapse();
                    GcmNetworkManager.getInstance(getActivity()).cancelAllTasks(LikerService.class);
                }
            }
        });


        mCheckout.start();

        mCheckout.loadInventory(Inventory.Request.create().loadAllPurchases(), new InventoryCallback());

        return v;
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
            if (product.isPurchased("bgLikes")) {
                Log.d("purchase","purchased");
                SharedPreferences sharedPreferences=getActivity().getSharedPreferences("data",MODE_PRIVATE);

                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("isBgLikes",true).commit();
                return;
            }

        }
    }

    // Setup a recurring alarm every half hour
    public void scheduleAlarm(int hour) {

//21600L


        PeriodicTask periodicTask = new PeriodicTask.Builder()

                .setPeriod(hour) // occurs at *most* once this many seconds - note that you can't control when
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                // various connectivity scenarios are available
                .setService(LikerService.class) // the GcmTaskServer you created earlier
                .setTag("LikerService")
                .setPersisted(true).setUpdateCurrent(true) // persists across reboots or not
                .build();
        gcmNetworkManager.schedule(periodicTask);

        Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
    }

}
