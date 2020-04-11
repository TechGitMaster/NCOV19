package com.example.ncov19;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class worldStatistics extends Fragment {

    View viewAll;
    private String[][] arrCountries;
    private List<List<String>> handleDataNCOV19;
    private List<List<String>> handlePicture;
    private List<String> GetHave;
    private int[] countAllAffected;
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;
    private TextView textViewAllConfirmed, textViewAllRecovered, textViewAllDeaths, textView6;
    private androidx.constraintlayout.widget.ConstraintLayout linearLayout2StatisticsWorld;
    private CardView cardViewViewed;
    private LinearLayout layoutCountriesAll;
    private DatabaseReference dataRef;
    private String noImageCountriesForNow;
    private EditText searchingText;
    private boolean firstSearch, booleanConditionToAll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewAll = inflater.inflate(R.layout.fragment_world_statistics, container, false);

        textViewAllConfirmed = viewAll.findViewById(R.id.textViewAllConfirmed);
        textViewAllRecovered = viewAll.findViewById(R.id.textViewAllRecovered);
        textViewAllDeaths = viewAll.findViewById(R.id.textViewAllDeaths);
        textView6 = viewAll.findViewById(R.id.textView6);
        linearLayout2StatisticsWorld = viewAll.findViewById(R.id.linearLayout2StatisticsWorld);
        linearLayout2StatisticsWorld.setVisibility(View.INVISIBLE);
        layoutCountriesAll = viewAll.findViewById(R.id.layoutCountriesAll);
        searchingText = viewAll.findViewById(R.id.searchingText);
        cardViewViewed = viewAll.findViewById(R.id.cardViewViewed);


        firstSearch = false;
        booleanConditionToAll = false;

        handleDataNCOV19 = new ArrayList<>();
        handlePicture = new ArrayList<>();
        GetHave = new ArrayList<>();
        countAllAffected = new int[] {0, 0, 0};

        dataRef = FirebaseDatabase.getInstance().getReference();
        noImageCountriesForNow = "https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586346711817_IMG?alt=media&token=ee8b6eb0-d247-4486-b543-6ba40a33078b";

        requestQueue = Volley.newRequestQueue(getActivity());
        this.handlingCountries();

        progressDialog = new ProgressDialog(getContext());

        getDataNCOV19 classes = new worldStatistics.getDataNCOV19();
        classes.execute();

        this.loadNavigationViewedCountries();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!booleanConditionToAll) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Reload", Toast.LENGTH_LONG).show();


                    getDataNCOV19 classes = new worldStatistics.getDataNCOV19();
                    classes.execute();

                    new Handler().postDelayed(this, 10000);
                }
            }
        }, 10000);


        return viewAll;
    }



    private class getDataNCOV19 extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.show();
            progressDialog.setContentView(R.layout.loadingstatement);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String linkDataNCOV19 = "https://2019ncov.asia/api/country_region";
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DEPRECATED_GET_OR_POST, linkDataNCOV19, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            booleanConditionToAll = true;

                            handleDataNCOV19 = new ArrayList<>();
                            handlePicture = new ArrayList<>();
                            GetHave = new ArrayList<>();
                            countAllAffected = new int[] {0, 0, 0};

                            dataRef.child("imageCountries").
                                    addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot datas : dataSnapshot.getChildren()) {
                                                List<String> st = new ArrayList<>();

                                                st.add(datas.getKey());
                                                st.add(datas.getValue(String.class));

                                                handlePicture.add(st);
                                            }

                                            requestQueue = Volley.newRequestQueue(getActivity());
                                            String linkDataNCOV19 = "https://2019ncov.asia/api/country_region";
                                            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DEPRECATED_GET_OR_POST, linkDataNCOV19, null,
                                                    new Response.Listener<JSONObject>() {
                                                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                            try {
                                                                JSONArray jsonArray = response.getJSONArray("results");


                                                                for (int numberCountData = 0; numberCountData < jsonArray.length(); numberCountData++) {
                                                                    JSONObject dataNCOV19 = jsonArray.getJSONObject(numberCountData);

                                                                    countAllAffected[0] += Integer.parseInt(dataNCOV19.getString("confirmed"));
                                                                    countAllAffected[1] += Integer.parseInt(dataNCOV19.getString("deaths"));
                                                                    countAllAffected[2] += Integer.parseInt(dataNCOV19.getString("recovered"));

                                                                    List<String> strings = new ArrayList<>();
                                                                    strings.add(dataNCOV19.getString("country_region"));
                                                                    strings.add(dataNCOV19.getString("confirmed"));
                                                                    strings.add(dataNCOV19.getString("deaths"));
                                                                    strings.add(dataNCOV19.getString("recovered"));
                                                                    handleDataNCOV19.add(strings);

                                                                    int asd = 0;
                                                                    for (int numberCountForScanning1 = 0; numberCountForScanning1 < arrCountries.length; numberCountForScanning1++) {
                                                                        if (arrCountries[numberCountForScanning1][1].equals(dataNCOV19.getString("country_region"))) {
                                                                            asd = 1;
                                                                        }

                                                                        if (numberCountForScanning1 + 1 >= arrCountries.length) {
                                                                            if (asd == 0) {
                                                                                asd = 0;
                                                                                for (int numberCountForScanning2 = 0; numberCountForScanning2 < handlePicture.toArray().length;
                                                                                     numberCountForScanning2++) {

                                                                                    if (handlePicture.get(numberCountForScanning2).get(0).equals(dataNCOV19.getString("country_region"))) {
                                                                                        asd = 1;
                                                                                    }

                                                                                    if (numberCountForScanning2 + 1 >= handlePicture.toArray().length) {
                                                                                        if (asd == 0) {
                                                                                            GetHave.add(strings.get(0));
                                                                                            List<String> stringss = new ArrayList<>();
                                                                                            stringss.add(dataNCOV19.getString("country_region"));
                                                                                            stringss.add("Nm");
                                                                                            handlePicture.add(stringss);
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }


                                                                    if (numberCountData + 1 >= jsonArray.length()) {
                                                                        //Toast.makeText(getContext(), ""+GetHave.toArray().length, Toast.LENGTH_LONG).show();

                                                                        if (GetHave.toArray().length == 0) {
                                                                            textViewAllConfirmed.setText(numberSeperated(String.valueOf(countAllAffected[0])));
                                                                            textViewAllRecovered.setText(numberSeperated(String.valueOf(countAllAffected[2])));
                                                                            textViewAllDeaths.setText(numberSeperated(String.valueOf(countAllAffected[1])));


                                                                            showingCountriesAffected(handleDataNCOV19, true);
                                                                        } else {
                                                                            for (int countNew = 0; countNew < GetHave.size(); countNew++) {
                                                                                dataRef.child("imageCountries").child(GetHave.get(countNew))
                                                                                        .setValue("Nm")
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    for (int numberCouns = 0; numberCouns < 3; numberCouns++) {
                                                                                                        textViewAllConfirmed.setText(numberSeperated(String.valueOf(countAllAffected[0])));
                                                                                                        textViewAllRecovered.setText(numberSeperated(String.valueOf(countAllAffected[2])));
                                                                                                        textViewAllDeaths.setText(numberSeperated(String.valueOf(countAllAffected[1])));
                                                                                                    }

                                                                                                    showingCountriesAffected(handleDataNCOV19, true);
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }


                                                                    }
                                                                }

                                                            } catch (Exception e) {
                                                                Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                                                                booleanConditionToAll = false;
                                                            }
                                                        }
                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                                                    booleanConditionToAll = false;
                                                }
                                            });

                                            requestQueue.add(jsonRequest);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                                            booleanConditionToAll = false;
                                        }
                                    });
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                    booleanConditionToAll = false;
                }
            });

            requestQueue.add(jsonRequest);
            return null;
        }
    }




    private void loadNavigationViewedCountries(){
        LinearLayout layoutFirst = new LinearLayout(getContext());
        layoutFirst.setPadding(5, 5, 5, 5);
        layoutFirst.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layoutFirst.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(500, 100,1);
        CardView cardCountries = new CardView(getContext());
        params.setMargins(3, 3, 3, 3);
        cardCountries.setLayoutParams(params);
        cardCountries.setPadding(3, 3,3 ,3);

        TextView txtViewCountries = new TextView(getContext());
        txtViewCountries.setText("Countries and Ships");
        txtViewCountries.setTextSize(15);
        txtViewCountries.setTextColor(getResources().getColor(R.color.black));
        txtViewCountries.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100, 1));
        txtViewCountries.setGravity(Gravity.CENTER);

        cardCountries.addView(txtViewCountries);
        layoutFirst.addView(cardCountries);


        params = new LinearLayout.LayoutParams(230, 100,1);
        CardView cardConfirmed = new CardView(getContext());
        params.setMargins(3, 3, 3, 3);
        cardConfirmed.setLayoutParams(params);
        cardConfirmed.setBackgroundResource(R.color.confirmed);
        cardConfirmed.setPadding(3, 3,3 ,3);

        TextView txtViewName = new TextView(getContext());
        txtViewName.setText("Confirmed");
        txtViewName.setTextSize(15);
        txtViewName.setTextColor(getResources().getColor(R.color.black));
        txtViewName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100, 1));
        txtViewName.setGravity(Gravity.CENTER);

        cardConfirmed.addView(txtViewName);
        layoutFirst.addView(cardConfirmed);


        CardView cardRecovered = new CardView(getContext());
        params.setMargins(3, 3, 3, 3);
        cardRecovered.setLayoutParams(params);
        cardRecovered.setBackgroundResource(R.color.recovered);
        cardRecovered.setPadding(3, 3,3 ,3);

        TextView txtViewRecovered = new TextView(getContext());
        txtViewRecovered.setText("Recovered");
        txtViewRecovered.setTextSize(15);
        txtViewRecovered.setTextColor(getResources().getColor(R.color.black));
        txtViewRecovered.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100, 1));
        txtViewRecovered.setGravity(Gravity.CENTER);

        cardRecovered.addView(txtViewRecovered);
        layoutFirst.addView(cardRecovered);



        CardView cardDeath = new CardView(getContext());
        params.setMargins(3, 3, 3, 3);
        cardDeath.setLayoutParams(params);
        cardDeath.setBackgroundResource(R.color.death);
        cardDeath.setPadding(3, 3,3 ,3);

        TextView txtViewDeaths = new TextView(getContext());
        txtViewDeaths.setText("Deaths people");
        txtViewDeaths.setTextSize(15);
        txtViewDeaths.setTextColor(getResources().getColor(R.color.black));
        txtViewDeaths.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100, 1));
        txtViewDeaths.setGravity(Gravity.CENTER);

        cardDeath.addView(txtViewDeaths);
        layoutFirst.addView(cardDeath);


        cardViewViewed.addView(layoutFirst);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void showingCountriesAffected(List<List<String>> CasesListNCOVTracker, boolean conditionss){
        layoutCountriesAll.removeAllViews();

        if (conditionss) {
            for (int numberCountForData = 0; numberCountForData < CasesListNCOVTracker.size(); numberCountForData++) {

                LinearLayout layout = new LinearLayout(getContext());
                layout.setPadding(5, 5, 5, 5);
                layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                layout.setGravity(Gravity.CENTER);

                CardView cardViews = new CardView(getActivity());
                cardViews.setLayoutParams(new LinearLayout.LayoutParams(500, 400, 1));

                TextView txtCountries = new TextView(getContext());
                txtCountries.setText(CasesListNCOVTracker.get(numberCountForData).get(0));
                txtCountries.setTextSize(16);
                txtCountries.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                txtCountries.setGravity(Gravity.CENTER);

                ImageView images = new ImageView(getContext());
                images.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

                cardViews.addView(txtCountries);
                cardViews.addView(images);
                layout.addView(cardViews);


                TextView txtViewNames = new TextView(getContext());
                txtViewNames.setText(numberSeperated(CasesListNCOVTracker.get(numberCountForData).get(1)));
                txtViewNames.setTextSize(15);
                txtViewNames.setGravity(Gravity.CENTER);
                txtViewNames.setBackgroundResource(R.color.confirmed);
                txtViewNames.setTextColor(getResources().getColor(R.color.black));
                txtViewNames.setLayoutParams(new LinearLayout.LayoutParams(230, 400, 1));
                layout.addView(txtViewNames);


                TextView txtViewConfirmed = new TextView(getContext());
                txtViewConfirmed.setText(numberSeperated(CasesListNCOVTracker.get(numberCountForData).get(3)));
                txtViewConfirmed.setTextSize(15);
                txtViewConfirmed.setBackgroundResource(R.color.recovered);
                txtViewConfirmed.setGravity(Gravity.CENTER);
                txtViewConfirmed.setTextColor(getResources().getColor(R.color.black));
                txtViewConfirmed.setLayoutParams(new LinearLayout.LayoutParams(230, 400, 1));
                layout.addView(txtViewConfirmed);

                TextView txtViewDead = new TextView(getContext());
                txtViewDead.setText(numberSeperated(CasesListNCOVTracker.get(numberCountForData).get(2)));
                txtViewDead.setTextSize(15);
                txtViewDead.setBackgroundResource(R.color.death);
                txtViewDead.setGravity(Gravity.CENTER);
                txtViewDead.setTextColor(getResources().getColor(R.color.black));
                txtViewDead.setLayoutParams(new LinearLayout.LayoutParams(230, 400, 1));
                layout.addView(txtViewDead);

                layoutCountriesAll.addView(layout);


                boolean conditionBool = false;
                for (int numberCountFlag = arrCountries.length - 1; numberCountFlag >= 0; numberCountFlag--) {
                    if (CasesListNCOVTracker.get(numberCountForData).get(0).equals(arrCountries[numberCountFlag][1])) {
                        conditionBool = true;
                        Picasso.with(getContext()).load(arrCountries[numberCountFlag][0]).resize(420, 420).into(images);
                    }

                    if (numberCountFlag <= 0) {
                        if (!conditionBool) {
                            for (int numberCountForSecond = 0; handlePicture.toArray().length > numberCountForSecond; numberCountForSecond++) {
                                if (handlePicture.get(numberCountForSecond).get(0).equals(CasesListNCOVTracker.get(numberCountForData).get(0))) {
                                    if (handlePicture.get(numberCountForSecond).get(1).length() > 2) {
                                        Picasso.with(getContext()).load(handlePicture.get(numberCountForSecond).get(1)).resize(420, 420).into(images);
                                    } else {
                                    //    Picasso.with(getContext()).load(noImageCountriesForNow).resize(420, 420).into(images);
                                    }
                                }
                            }
                        }
                    }
                }


                if (numberCountForData + 2 > CasesListNCOVTracker.size()) {
                    if (!firstSearch) {
                        firstSearch = true;
                        searchingText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                char[] charArrText = searchingText.getText().toString().toCharArray();
                                List<List<String>> stringSearch = new ArrayList<>();

                                if (searchingText.getText().length() != 0) {
                                    for (int numberArr = 0; handleDataNCOV19.size() > numberArr; numberArr++) {
                                        List<String> stString = handleDataNCOV19.get(numberArr);
                                        if (charArrText.length <= stString.get(0).length()) {
                                            boolean conditionDont = true;
                                            for (int numberText = 0; charArrText.length > numberText; numberText++) {
                                                if (!String.valueOf(stString.get(0).toCharArray()[numberText]).toLowerCase().equals(String.valueOf(charArrText[numberText])
                                                        .toLowerCase())) {
                                                    conditionDont = false;
                                                }

                                                if (numberText + 1 >= charArrText.length) {
                                                    if (conditionDont) {
                                                        stringSearch.add(stString);
                                                    }
                                                }
                                            }
                                        }

                                        if (numberArr + 1 >= handleDataNCOV19.size()) {
                                            if (stringSearch.toArray().length > 0) {
                                                showingCountriesAffected(stringSearch, true);
                                            } else {
                                                showingCountriesAffected(stringSearch, false);
                                            }
                                        }
                                    }
                                } else {
                                    showingCountriesAffected(handleDataNCOV19, true);
                                }

                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }

                    progressDialog.dismiss();
                    linearLayout2StatisticsWorld.setVisibility(View.VISIBLE);
                }
            }
        }else{
            LinearLayout layoutss = new LinearLayout(getContext());
            layoutss.setPadding(5, 5, 5, 5);
            layoutss.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layoutss.setGravity(Gravity.CENTER);

            TextView txtViewss = new TextView(getContext());
            txtViewss.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            txtViewss.setText("Can't find your query.");
            txtViewss.setTextSize(16);
            txtViewss.setTextColor(getResources().getColor(R.color.black));

            layoutss.addView(txtViewss);

            layoutCountriesAll.addView(layoutss);
        }
    }



    private String numberSeperated(String number){
        int[][] arrNumber = new int[][]{ {4, 1}, {5, 2}, {6, 3}, {7, 1}, {8, 2}, {9,3}, {10, 1}};
        String handleFinal = "", pendingSt = "";
        int handleNums = 0;
        if(Integer.parseInt(number) >= 1000) {

            for(int arrCounts = 0;arrCounts < arrNumber.length;arrCounts++){
                if(number.length() == arrNumber[arrCounts][0]){
                    for(int numberFors = 0;numberFors < arrNumber[arrCounts][1];numberFors++){
                        handleFinal += ""+number.toCharArray()[numberFors];
                    }

                    handleNums = arrNumber[arrCounts][1];
                }

                if(arrCounts+1 >= arrNumber.length){
                    handleFinal += ",";

                    int numberFor = 0;
                    for (int numberCount = handleNums; numberCount < number.toCharArray().length; numberCount++) {
                        if (numberFor != 2) {
                            pendingSt += String.valueOf(number.toCharArray()[numberCount]);
                            numberFor++;
                        } else {
                            pendingSt += number.toCharArray()[numberCount]+",";
                            handleFinal += pendingSt;
                            numberFor = 0;
                            pendingSt = "";
                        }
                    }

                    String handleFinals = "";
                    for(int count = 0;handleFinal.length() > count;count++){
                        if(count != handleFinal.length()-1){
                            handleFinals += handleFinal.toCharArray()[count];
                        }
                    }
                    return handleFinals;
                }
            }



        }
        return number;
    }

    private void handlingCountries() {

        arrCountries = new String[][]{
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586247577545_IMG?alt=media&token=a0843578-99f9-44e6-9066-b6648d6e88cf", "USA"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586247642482_IMG?alt=media&token=a621320b-af30-4d9c-92f9-da4c06a19e99", "Spain"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586247811360_IMG?alt=media&token=cbc7e22d-9a62-4324-b237-9859793d41b1", "Italy"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586248177668_IMG?alt=media&token=fcd55312-c0e7-4dd4-9312-878dead3bec1", "Germany"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586248300267_IMG?alt=media&token=06a4c20c-4781-47a1-8ae7-bc3e8f3d7808", "France"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586248480284_IMG?alt=media&token=8ce98c8d-7a42-4086-96eb-40ebb330cfc8", "Iran"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586248588577_IMG?alt=media&token=7344a58f-4208-4d63-b56b-2486260681bb", "UK"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586248815018_IMG?alt=media&token=9b9db47b-1067-41b9-830c-10748c17580f", "Turkey"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586248955245_IMG?alt=media&token=b61a9fc7-a7f3-4dce-8046-855c97c5ed92", "Switzerland"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249010437_IMG?alt=media&token=da22b6e7-ed09-4e0b-af5e-64567861561d", "Belgium"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249068550_IMG?alt=media&token=0ad2fac4-7622-4978-8c70-671d872ee0fc", "Netherlands"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249125569_IMG?alt=media&token=3a0c7401-4f56-40b6-a489-da88e022a41d", "Canada"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249243994_IMG?alt=media&token=fed191a6-b8ac-4459-8516-cdc71c6bb20f", "Austria"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249413904_IMG?alt=media&token=b5313613-c5fb-4c32-a2a9-2166e0ab37fd", "Portugal"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249310731_IMG?alt=media&token=8b523dde-c243-4b74-b1d5-22c2491a7038", "Brazil"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249524997_IMG?alt=media&token=f15d79c2-b91a-4218-9f3d-50eef086d9bb", "S. Korea"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249626506_IMG?alt=media&token=5869eebc-a21f-4a95-8ea3-4bcbf89141f9`", "Israel"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249809451_IMG?alt=media&token=c60cf2fc-4dff-494b-8642-2735e2774cb0", "Sweden"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586259693548_IMG?alt=media&token=65b38896-3e01-463c-bbe2-0d7f67e20ac8", "Australia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586259757837_IMG?alt=media&token=8231e3b9-0e3d-41a8-9551-baf8c1121294", "Norway"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586249758595_IMG?alt=media&token=c247f725-28b5-4d27-a182-2620a1f7cb50", "Russia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586259823752_IMG?alt=media&token=ccac8d11-b5c6-4c6f-9551-6214f6d722e7", "Ireland"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260114096_IMG?alt=media&token=371332ab-9d3c-4d18-95f2-ee172be2795a", "Czechia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586259880823_IMG?alt=media&token=c37ef9a8-37cb-441b-8446-4ed180444760", "Denmark"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260166809_IMG?alt=media&token=29812e33-2019-41fb-8d2e-2024422d0ae3", "Chile"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260255846_IMG?alt=media&token=a6d304d3-1b4f-4f4d-a79d-be553c79de9e", "Romania"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260216445_IMG?alt=media&token=d4766b8f-42fc-4df8-b93c-aa9ba6b0ad19", "Poland"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260347272_IMG?alt=media&token=dc5337fc-72f7-4e5a-855e-0680889ff111", "Malaysia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586259969229_IMG?alt=media&token=0fcd54f0-50a0-44e8-956e-67f9d3172e16", "India"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260462763_IMG?alt=media&token=1a374b4e-e4b9-402e-8f39-ea9ae069d611", "Ecuador"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260424615_IMG?alt=media&token=fa607662-3b00-4add-9ee5-3f599188538b", "Philippines"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260384367_IMG?alt=media&token=e7d3965b-204d-46a4-9e17-3670381c2004", "Japan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260295541_IMG?alt=media&token=97534f75-f5e0-4372-a72a-971be3bc1a47", "Pakistan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260502532_IMG?alt=media&token=a923053f-5101-414a-8ef1-625dd515e8ea", "Luxembourg"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260565838_IMG?alt=media&token=6025e7d7-6c76-4da2-8590-e86393d3442f", "Saudi Arabia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260613930_IMG?alt=media&token=202ebebc-de47-4833-b21b-0e528d157fad", "Indonesia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260954585_IMG?alt=media&token=2368d7a3-4b3d-48f3-920c-aeed40ae7190", "Thailand"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260773378_IMG?alt=media&token=cd7b7913-26d9-4a22-9a93-d5d31328ca20","Finland"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260728583_IMG?alt=media&token=f24cc655-4035-41a2-888b-c9b9e415879c", "Mexico"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260991827_IMG?alt=media&token=7d1bb57f-88c6-4b83-8380-1bdff67ed411", "Panama"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260652560_IMG?alt=media&token=d92dfb69-be40-4de9-be36-9e7216e4348c", "Peru"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261209747_IMG?alt=media&token=f492e3a9-54a6-4eae-9233-daf91da35c2b", "Greece"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586260870252_IMG?alt=media&token=a267e7c1-6f9e-4f86-a5e8-c6d24e706749", "Serbia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261251185_IMG?alt=media&token=b614e0f3-c213-4c4f-8cbd-e85367053bc8", "South Africa"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261171110_IMG?alt=media&token=5502842f-9122-4dc3-8cdd-764e84135fed", "Dominican Republic"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261060369_IMG?alt=media&token=3b56fe98-baa3-4e6d-9954-fccfc4d400eb", "UAE"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261287296_IMG?alt=media&token=1d8bf323-218b-4f21-857d-e46af2ebb02a", "Argentina"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261375475_IMG?alt=media&token=28cb7701-f26b-4c69-aa0f-747ed5fa690f", "Iceland"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261336689_IMG?alt=media&token=533047fb-68f6-47f9-bcd3-6ab230a55a97", "Colombia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261099181_IMG?alt=media&token=81bbb872-5e73-4881-8831-9c471af516ec", "Qatar"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261489152_IMG?alt=media&token=f8abb941-f7f7-42f1-a0a9-620938a12f43", "Singapore"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261453942_IMG?alt=media&token=3fe3ff96-9b5e-4ece-a4ca-7ec1be5781c6", "Algeria"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261420679_IMG?alt=media&token=477d61d1-0bc7-412a-87d1-6c8d12f79589", "Ukraine"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261668491_IMG?alt=media&token=ba7355ef-4594-4c54-b1e5-fb32030b2938", "Croatia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261772575_IMG?alt=media&token=251a60a2-a518-4baf-aa04-77ccd963fc86", "Estonia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261529289_IMG?alt=media&token=82f758e0-e2d5-406f-a225-52c3fbb36713", "Egypt"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261723047_IMG?alt=media&token=0364315d-cac5-4466-a37a-871d76080610", "New Zealand"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261903331_IMG?alt=media&token=b411b18d-662f-4411-a6d3-9223e65a20a0", "Slovenia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261805611_IMG?alt=media&token=de2c9b97-f558-4e7f-924c-b6a931249545", "Morocco"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262059585_IMG?alt=media&token=30f58b02-85ab-4253-85fe-6ae6ada980d0", "Hong Kong"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261861311_IMG?alt=media&token=a948a769-5d01-477f-a85b-dd1cf081621a", "Iraq"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262181057_IMG?alt=media&token=e6f82fe1-97eb-450f-8b9c-f030b5b5da33", "Armenia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262143922_IMG?alt=media&token=e9b1f371-cee5-4847-b3b9-8b5cf4583392", "Lithuania"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586261992114_IMG?alt=media&token=007329d1-0a6d-4526-a049-1d98accdcc3b", "Moldova, Republic of"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262218421_IMG?alt=media&token=dd05c5a7-b4ae-433b-9adb-4c7a2624775a", "Hungary"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262451951_IMG?alt=media&token=bd1e9a40-f801-47d3-90ef-78cbdab43ba4", "Diamond Princess"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262258624_IMG?alt=media&token=f107bd18-cd6e-4b86-9f3d-3ecc0fef1763", "Bahrain"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262320995_IMG?alt=media&token=63c10a4b-6b4f-48d4-b98b-950f208dcb2a", "Bosnia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265142291_IMG?alt=media&token=40e8ff41-7749-48a2-bd7f-75cd72112b33", "Azerbaijan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262532830_IMG?alt=media&token=c2ecf63f-8f9f-4f17-b332-4b7b578a023d", "Kazakhstan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262497459_IMG?alt=media&token=95c48cd1-3aa3-43b8-94d8-61870a0f5098", "Belarus"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262357209_IMG?alt=media&token=93df18a6-41fc-4969-8c06-c61732db123b", "Kuwait"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265270846_IMG?alt=media&token=9e133b6a-afee-4bc2-8b5b-697340693f74", "Macedonia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586262579079_IMG?alt=media&token=64015089-558e-4af3-8e2b-0b223e25eb10", "Cameroon"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265182627_IMG?alt=media&token=16a8fc55-f105-494b-844c-0b22e5d4e9d6", "Tunisia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265423564_IMG?alt=media&token=082846cf-6f99-4a84-a478-fb6e2a4cf636", "Latvia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265366180_IMG?alt=media&token=8008be05-66ca-4af4-9a12-764f412281d6", "Lebanon"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265317897_IMG?alt=media&token=2ac70077-0223-4ff4-8d65-f001ef18e675", "Bulgaria"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265473945_IMG?alt=media&token=054aafd8-8ff3-44b6-95b9-a61e7757c3e0", "Andorra"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265230052_IMG?alt=media&token=7b17935f-39b8-4f4a-8b56-66c19c1a3f83", "Slovakia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265571765_IMG?alt=media&token=0559ba7a-8082-471d-b8ff-8af6f94c0910", "Costa Rica"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265649449_IMG?alt=media&token=afc06e02-16d9-4958-b77f-63b2219f82af", "Cyprus"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265715822_IMG?alt=media&token=2739907c-376f-4109-9e6a-ecd01156a295", "Uruguay"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265866402_IMG?alt=media&token=66831761-5163-4667-8ae3-30b0560a131a", "Taiwan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265766971_IMG?alt=media&token=3f477f87-01b6-471c-838a-8621250e57a5", "Albania"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265683410_IMG?alt=media&token=d1a9227c-7df8-48c7-9fd1-07053c81a781", "Afghanistan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266203482_IMG?alt=media&token=7afd9508-6f93-4353-a19d-1861c439dcb3", "Réunion"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266039620_IMG?alt=media&token=3c92a388-7fa2-4258-8c24-0b9b56db2791", "Jordan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265955635_IMG?alt=media&token=91e3a2a2-4ce8-45a2-bd94-3d6e145df677", "Burkina Faso"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265906095_IMG?alt=media&token=7c8a825e-ed3b-4000-82c3-93a9ba5c663a", "Oman"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586265526121_IMG?alt=media&token=98d95131-2801-4f20-b84b-5b525cf2375d", "Uzbekistan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266005185_IMG?alt=media&token=15fd5c29-d846-4f97-b617-4cdcb02b6eb0", "Cuba"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266531627_IMG?alt=media&token=ff277ac5-60dc-4515-8dcd-bcf111b3d50e", "Honduras"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266690690_IMG?alt=media&token=3b93650f-771b-4247-96d6-5df25e9cc1c2", "San Marino"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266690690_IMG?alt=media&token=3b93650f-771b-4247-96d6-5df25e9cc1c2", "Channel Islands"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266496257_IMG?alt=media&token=34b08709-c4ff-4780-a9f7-ce449f18b076", "Côte d'Ivoire"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266927237_IMG?alt=media&token=a09b609b-dd0f-423c-9f15-0517eb2ae757", "Vietnam"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266971167_IMG?alt=media&token=f862bd60-e792-4133-bd68-1770e7446f41", "Mauritius"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266581585_IMG?alt=media&token=90b0cbd2-e3bf-4dcd-8fa9-830542bcf16d", "Malta"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266812206_IMG?alt=media&token=4957a765-40d6-4820-8ee9-840488fe9665", "Palestinian Territory, Occupied"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267201610_IMG?alt=media&token=b5aad0b0-47d1-43ad-8c03-c9e6f3e5a35d", "Nigeria"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267328902_IMG?alt=media&token=8a602f56-2ef0-405d-a4ae-976bf24a3d57", "Senegal"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266638945_IMG?alt=media&token=868696ab-e32e-415f-8a76-93dfd64d585f", "Ghana"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267024041_IMG?alt=media&token=7ef3356e-86b1-470f-9ae9-87f2a2e45fba", "Montenegro"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267468486_IMG?alt=media&token=2abcc2b0-394d-4400-abc2-4153c378d65e", "Faroe Islands"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267530973_IMG?alt=media&token=52227eea-1e34-4ef1-a529-2e27d9a1d9c0", "Sri Lanka"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267396416_IMG?alt=media&token=07803ce4-0fdf-4018-96d6-479c3ef788f6", "Georgia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267423843_IMG?alt=media&token=ed558c3f-62eb-4b3d-b0f2-7ec01fa0711d", "Bolivia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267572146_IMG?alt=media&token=a4bb8e0e-ae21-400e-b15c-30d2ed1733ed", "Venezuela"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267945236_IMG?alt=media&token=13112822-52a3-4b6f-91f9-a332b7966317", "DRC"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267266564_IMG?alt=media&token=a11008eb-e01a-4431-b827-4cd8d075fd13", "Kyrgyzstan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268117643_IMG?alt=media&token=8f3d2254-2482-4363-91e7-36617eed1888", "Martinique"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586266860496_IMG?alt=media&token=f34e0dbe-3c2a-449c-9e3a-b045edde6543", "Niger"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268296066_IMG?alt=media&token=bcd303df-6146-446c-85d6-be5251f6c3e5", "Brunei"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268254204_IMG?alt=media&token=c362f230-4570-4c25-9e93-760d47b4d75a", "Guadeloupe"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267734817_IMG?alt=media&token=212759fa-c109-4cb2-b87c-6d6b25ea10cf", "Mayotte"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268174463_IMG?alt=media&token=92a4419a-d460-4ca9-8a9a-e1ba376bb623", "Isle of Man"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267986969_IMG?alt=media&token=42481215-8733-42dc-a0d4-24ba8e45b191", "Kenya"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268453995_IMG?alt=media&token=a5acea93-fe50-43ad-a43b-56eaf0a101ce", "Cambodia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268382942_IMG?alt=media&token=6d1f6e90-f764-4ef2-bef3-f22cd6ee6f79", "Guinea"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268419782_IMG?alt=media&token=1caced04-f57c-495f-816a-e05a17a14801", "Paraguay"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268588559_IMG?alt=media&token=e81a1e87-ef5b-4abc-84fd-b63016975d40", "Trinidad and Tobago"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268662924_IMG?alt=media&token=d6788b13-326e-424a-9f1f-95d3da3d0184", "Rwanda"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268541706_IMG?alt=media&token=f88066e9-d86c-47ef-a9f1-e5817bfceaa2", "Gibraltar"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586267605890_IMG?alt=media&token=921b4aa3-2e96-4821-95a9-43a7d09cd3e1", "Bangladesh"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586269054337_IMG?alt=media&token=8e0e9fa9-5de7-4130-ad30-c558cf14fabf", "Liechtenstein"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268914454_IMG?alt=media&token=9ccb93d4-2ca3-41bf-8df8-cb6e641b5f35", "Madagascar"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586269162002_IMG?alt=media&token=83af3671-b8a0-4fd0-b9e3-1ad0b4707558", "Monaco"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270011208_IMG?alt=media&token=1e65b628-e70e-456c-bb76-bf09669326e5", "Aruba"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268976754_IMG?alt=media&token=8ea23c5a-a7d2-4d25-80a2-fb3c47f85c53", "El Salvador"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586269416430_IMG?alt=media&token=574932ea-08aa-4e37-97a9-c8734191eff2", "Guatemala"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586269953356_IMG?alt=media&token=8edfe3b6-a6cb-4c62-a0d7-a5a6b2335938", "French Guiana"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270137703_IMG?alt=media&token=94c3d4d2-527f-4c21-b338-c4b164a16172", "Jamaica"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270060760_IMG?alt=media&token=cccfaff9-4e07-4afd-8cff-7df6c7bd8902", "Barbados"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586268868643_IMG?alt=media&token=61d33a47-f61b-48b8-a6fd-3979a72c7573", "Djibouti"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270295608_IMG?alt=media&token=d8771eff-13a0-465f-8b5f-aaf0be49086a", "Uganda"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270364400_IMG?alt=media&token=379dc7b7-dd56-4cd2-9561-583ca9919bfa", "Congo"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270566127_IMG?alt=media&token=89893188-d214-4d46-a276-39c9a39cd0cb", "Macao"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270252700_IMG?alt=media&token=7d579455-a792-4d29-9668-4b93107c9e6b", "Ethiopia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270217402_IMG?alt=media&token=6f4825de-74c7-4c1b-8651-8670deb51a3d", "Mali"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270179675_IMG?alt=media&token=73fb7a55-7170-4c20-9fee-9da76ae5a701", "Togo"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270635362_IMG?alt=media&token=bee00507-89a2-4e7c-b434-abb3c737a1c8", "French Polynesia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270824916_IMG?alt=media&token=4bba9e29-fb04-4365-849c-c7187dc5557d", "Zambia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270674276_IMG?alt=media&token=a62f2231-7548-4f1a-a8f8-2e107096b9bb", "Bermuda"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270786692_IMG?alt=media&token=f264af4c-c99a-4ffe-8116-0e46c1cc5159", "Cayman Islands"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271136509_IMG?alt=media&token=faad62d8-ceb0-453b-8683-4fa7b2e4fa81", "Saint Martin"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271301483_IMG?alt=media&token=fbad2685-2171-408a-a5ae-9c808dff7014", "Eritrea"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270943008_IMG?alt=media&token=48c6d3c4-b7a9-4ffc-860e-03b7e4c8d305", "Bahamas"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586270881681_IMG?alt=media&token=4845b976-5b46-4e6d-b109-1780f64c97d0", "Sint Maarten"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271246979_IMG?alt=media&token=6794bbbc-7692-4fc1-b3e3-40ddb65dffd0", "Guyana"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271529784_IMG?alt=media&token=5632d356-46c3-46d2-8677-eb42e2946d13", "Myanmar"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271433809_IMG?alt=media&token=805f2f64-1bee-4256-b375-43465bae1a22", "Haiti"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271479091_IMG?alt=media&token=44108445-18df-4a11-8801-7e05b33528df", "Tanzania, United Republic of"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271639263_IMG?alt=media&token=e14c008e-eeb9-424f-bdf7-39b3612c1449", "Syrian Arab Republic"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271891488_IMG?alt=media&token=a3d3d8ce-02de-4f55-84c2-2e75906752dc", "Maldives"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271843861_IMG?alt=media&token=737ab043-ad58-4de5-9c38-ea27b86dae47", "Libyan Arab Jamahiriya"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271002062_IMG?alt=media&token=82350d14-6080-4d26-9af9-095e2e78b130", "Guinea-Bissau"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271978651_IMG?alt=media&token=27bbcafc-bb6a-49ef-8df1-a779bf22d18e", "New Caledonia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271387805_IMG?alt=media&token=bfaa1aa8-7227-40c8-b6a1-8405daeddd8c", "Benin"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272072688_IMG?alt=media&token=087cb95c-845e-4566-a0ff-8acce7f9be50", "Equatorial Guinea"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272156169_IMG?alt=media&token=a140b692-b8b3-47ef-82c8-79a0ffe5db08", "Namibia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272283009_IMG?alt=media&token=caa22647-fe0e-41a1-8f83-ba78da598da7", "Antigua and Barbuda"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272325959_IMG?alt=media&token=25feac2f-f6c5-4734-8ebc-155073c030ca", "Dominica"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272393267_IMG?alt=media&token=82340899-b870-4e24-8666-d18ccfcb2bae", "Mongolia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272740957_IMG?alt=media&token=acf0e89e-2ab4-47b8-a664-d0f54c788306", "Saint Lucia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272433214_IMG?alt=media&token=16921e11-78af-426c-bb85-7ccc65309989", "Liberia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272357012_IMG?alt=media&token=fc34af0d-64a5-451e-aeba-beb9881b7f64", "Fiji"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272829965_IMG?alt=media&token=cc311310-1b36-4d00-89e5-4c058ba14a41", "Grenada"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272793131_IMG?alt=media&token=b355053c-9be0-4a87-acee-5f7f7aadd4b6", "Curaçao"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272874840_IMG?alt=media&token=ed2d655b-50ba-427f-ba41-0ed26351d749", "Greenland"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272664854_IMG?alt=media&token=aa6cac66-017f-4eef-9142-1f57e8955069", "Lao People's Democratic Republic"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272018786_IMG?alt=media&token=1bc012f6-c4d8-44de-9838-52ab11511ee9", "Angola"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272498545_IMG?alt=media&token=8cb2f3fe-d1c7-4ae8-8584-56b9619e57b6", "Sudan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586346402163_IMG?alt=media&token=55e52edc-6198-4ea1-ab27-7a79e31f95be", "Suriname"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273036450_IMG?alt=media&token=8cc9614b-a251-41c1-a43a-988c640b4c26", "Mozambique"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272966751_IMG?alt=media&token=56d6ec22-6b6b-43e7-90bd-9e0c579a7498", "Seychelles"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273185650_IMG?alt=media&token=a97d7b17-6a0a-40dd-b0f3-1bb2e8a0e4b3", "MS Zaandam"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272997696_IMG?alt=media&token=adfb8f11-083b-4b4e-bfe3-4e93656cedbd", "Zimbabwe"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273241358_IMG?alt=media&token=b71d48b6-47bb-4340-9814-7b59b403d542", "Nepal"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273278767_IMG?alt=media&token=9804229e-013a-4b85-aab4-977e7a63f292", "Chad"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586272925438_IMG?alt=media&token=70d45504-36e9-455b-aa15-bcc8161637e8", "Saint Kitts and Nevis"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273086504_IMG?alt=media&token=42e8f9d6-997f-4924-948a-97fc11230245", "Swaziland"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273478692_IMG?alt=media&token=cc65406b-fd53-4d9a-9577-bdf4c9ca1344", "Central African Republic"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273582959_IMG?alt=media&token=1833d9b0-9ab9-4f96-bb21-65873392ba04", "Cabo Verde"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273689618_IMG?alt=media&token=1ce740ce-c8fb-45fb-a93b-12895ba70f81", "Holy See (Vatican City State)"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273783935_IMG?alt=media&token=6c81e656-c185-446e-8962-4237c01dba53", "Somalia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273877765_IMG?alt=media&token=b3d2eaa8-7f7e-4e28-a974-59660042f42e", "Mauritania"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274024847_IMG?alt=media&token=9d24e4ea-39f2-4919-bb2a-bdd7f811615a", "Montserrat"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274168838_IMG?alt=media&token=5239cc96-8bad-485e-8500-7ff32aee91fb", "St. Barth"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274245225_IMG?alt=media&token=2743607e-9d83-472f-a328-d8b4601fa81e", "Sierra Leone"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586271352785_IMG?alt=media&token=fd36a2f0-76a4-465c-8cbe-bbf8f5556e62", "Gabon"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273929170_IMG?alt=media&token=2944b1d1-0d9f-4066-a1d0-626b62fd0047", "Nicaragua"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586346564326_IMG?alt=media&token=2725f0ed-a87f-4c57-8b78-7e7af330e0d9", "Saint Vincent and the Grenadines"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274276465_IMG?alt=media&token=b1127131-cf1f-4e1e-8573-c54293401b41", "Bhutan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273415392_IMG?alt=media&token=44017174-2e11-484d-947b-035482a15faa", "Turks and Caicos Islands"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273826442_IMG?alt=media&token=7824c36a-b911-42cb-bb0b-69955f281e5e", "Botswana"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274326443_IMG?alt=media&token=5d2e2182-f881-42d0-aae0-0c0b904d6076", "Gambia"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273525441_IMG?alt=media&token=1e9e6657-b920-409d-8c81-d1106260bce9", "Belize"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586273323853_IMG?alt=media&token=384c6aee-fb08-4663-8d58-21356b495448", "Malawi"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274502760_IMG?alt=media&token=27b1039e-702e-45df-a608-2b7a4b39e6c7", "Western Sahara"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274552950_IMG?alt=media&token=2a47bd24-f5bd-4a8a-8254-b425eac2a631", "Anguilla"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274628906_IMG?alt=media&token=5e4c489f-0a93-4ccd-97f3-64f35e3677a8", "British Virgin Islands"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274679283_IMG?alt=media&token=4e6fee13-7d35-41ec-8bc3-3000a452354a", "Burundi"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586274858367_IMG?alt=media&token=9fadb0d1-f9a5-4fa8-9d12-54b80f9044b6", "Caribbean Netherlands"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586275045132_IMG?alt=media&token=e5f4f2a8-7317-43df-8adf-6352e51843f9", "Falkland Islands (Malvinas)"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586275136876_IMG?alt=media&token=5f70b5b3-32d2-4f5f-a1e6-d94d7c9e318d", "Papua New Guinea"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586275260713_IMG?alt=media&token=b1d50711-13d2-4576-9971-485c517cc7b9", "Saint Pierre Miquelon"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586275341555_IMG?alt=media&token=166f5a40-561a-4732-85da-dd18f84ffacc", "South Sudan"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586275458723_IMG?alt=media&token=b2bae93d-cc1a-4a1a-845f-e9ca9bd8cc5d", "Timor-Leste"},
                {"https://firebasestorage.googleapis.com/v0/b/ncov19-331bb.appspot.com/o/imageCountriess%2F1586275521155_IMG?alt=media&token=54b60402-415e-45e5-b4e9-026dc949da43", "China"}
        };
    }
}
