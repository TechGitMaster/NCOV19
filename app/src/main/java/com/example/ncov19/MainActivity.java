package com.example.ncov19;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import static android.content.Intent.ACTION_GET_CONTENT;

public class MainActivity extends AppCompatActivity {


    private FragmentManager fragmentManager;
    private LinearLayout layoutNavigation;
    private String conditions;
    private int numberForExit;
    private RequestQueue requestQueue;
    private LinearLayout linearLayout, elevationLoading;
    private TextView textView2;
    private DatabaseReference dataRef;
    private boolean booleanConditionToAll;
    private androidx.constraintlayout.widget.ConstraintLayout linearLayout4;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        dataRef = FirebaseDatabase.getInstance().getReference("announceCovid19");

        progressDialog = new ProgressDialog(this);

        linearLayout = findViewById(R.id.linearLayout);
        linearLayout4 = findViewById(R.id.linearLayout4);
        elevationLoading = findViewById(R.id.elevationLoading);
        textView2 = findViewById(R.id.textView2);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        booleanConditionToAll = false;
        numberForExit = 0;
        conditions = "0";
        layoutNavigation = (LinearLayout)findViewById(R.id.navigation);


        for(int numberCount = 0;numberCount < layoutNavigation.getChildCount();numberCount++){
            ImageView image = (ImageView)layoutNavigation.getChildAt(numberCount);
            image.setContentDescription(String.valueOf(numberCount));

            image.setOnClickListener(listenerClick);
        }

        new gettingDataHeader().execute();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!booleanConditionToAll) {
                    Toast.makeText(getApplicationContext(), "Reload", Toast.LENGTH_LONG).show();

                    new gettingDataHeader().execute();

                    new Handler().postDelayed(this, 10000);
                }
            }
        }, 10000);

    }



    private class gettingDataHeader extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            final String linkDataNCOV19 = "https://2019ncov.asia/api/country_region";

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DEPRECATED_GET_OR_POST, linkDataNCOV19, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            booleanConditionToAll = true;
                            dataRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                    linearLayout.setVisibility(View.VISIBLE);
                                    linearLayout4.setVisibility(View.VISIBLE);
                                    layoutNavigation.setVisibility(View.VISIBLE);

                                    elevationLoading.setVisibility(View.INVISIBLE);

                                    linearLayout.setBackgroundColor(Color.parseColor(dataSnapshot.child("backgroundHex").getValue(String.class)));
                                    textView2.setTextColor(Color.parseColor(dataSnapshot.child("textColorHex").getValue(String.class)));
                                    textView2.setText(dataSnapshot.child("textNCOVHeader").getValue(String.class));

                                    Fragment fragmentHome = new home();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.fragment, fragmentHome);
                                    fragmentTransaction.commit();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Check your connection", Toast.LENGTH_LONG).show();
                    booleanConditionToAll = false;
                }
            });

            requestQueue.add(jsonRequest);
            return null;
        }
    }








    private View.OnClickListener listenerClick = new View.OnClickListener(){
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onClick(View v) {
            if(v.getContentDescription().toString().equals("0")){
                if(!conditions.equals(v.getContentDescription().toString())) {
                    Fragment fragmentHome = new home();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, fragmentHome);
                    fragmentTransaction.commit();
                    conditions = v.getContentDescription().toString();
                    numberForExit = 0;
                }
            }else if(v.getContentDescription().toString().equals("2")){
                if(!conditions.equals(v.getContentDescription().toString())) {
                    Fragment fragmentHome = new symtoms();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, fragmentHome);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.commit();
                    conditions = v.getContentDescription().toString();
                    numberForExit = 1;
                }
            }else if(v.getContentDescription().toString().equals("1")){
                if(!conditions.equals(v.getContentDescription().toString())) {
                    Fragment fragmentHome = new worldStatistics();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, fragmentHome);
                    fragmentTransaction.commit();
                    conditions = v.getContentDescription().toString();
                    numberForExit = 1;
                }
            }else{
                if(!conditions.equals(v.getContentDescription().toString())) {
                    Fragment fragmentHome = new news();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, fragmentHome);
                    fragmentTransaction.commit();
                    conditions = v.getContentDescription().toString();
                    numberForExit = 1;
                }
            }


            for(int numberCount = 0;numberCount < layoutNavigation.getChildCount();numberCount++){
                ImageView img = (ImageView)layoutNavigation.getChildAt(numberCount);
                if(String.valueOf(numberCount).equals(v.getContentDescription().toString())){
                    img.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                }else{
                    img.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkWhite), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        }
    };


    @Override
    public void onBackPressed() {
        if(numberForExit > 0) {
            for (int numberCount = 0; numberCount < layoutNavigation.getChildCount(); numberCount++) {
                ImageView img = (ImageView) layoutNavigation.getChildAt(numberCount);
                if (img.getContentDescription().toString().equals("0")) {
                    img.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    img.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkWhite), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }


            Fragment home = new home();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, home);
            fragmentTransaction.commit();

            conditions = "0";
            numberForExit--;
        }else {
            finish();
        }
    }

}
