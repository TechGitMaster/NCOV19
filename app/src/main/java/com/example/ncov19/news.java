package com.example.ncov19;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class news extends Fragment {

    private View viewAll;
    private LinearLayout layout;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    private LinearLayout newsLayout, linearLayoutParent;
    private TextView dateLatest;
    private List<List<String>> stringListNews;
    private boolean booleanConditionToAll;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewAll = inflater.inflate(R.layout.fragment_news, container, false);


        layout = viewAll.findViewById(R.id.newsLayout);
        newsLayout = (LinearLayout)viewAll.findViewById(R.id.newsLayout);
        dateLatest = viewAll.findViewById(R.id.dateLatest);
        linearLayoutParent = viewAll.findViewById(R.id.linearLayoutParent);

        booleanConditionToAll = false;
        stringListNews = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());
        requestQueue = Volley.newRequestQueue(getActivity());

        new getDataClass().execute();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!booleanConditionToAll) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Reload", Toast.LENGTH_LONG).show();

                    new getDataClass().execute();
                    new Handler().postDelayed(this, 10000);
                }
            }
        }, 10000);

        return viewAll;
    }

    private class getDataClass extends AsyncTask<Void, Void, Void>{
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
            booleanConditionToAll = true;
            String newsData = "http://newsapi.org/v2/top-headlines?country=ph&apiKey=59ca12886bbb42e2ac3ccd332896c39d";

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DEPRECATED_GET_OR_POST, newsData, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            stringListNews = new ArrayList<>();
                            try{
                                JSONArray jsonArray = response.getJSONArray("articles");

                                for(int numberCountArr = 0;numberCountArr < jsonArray.length();numberCountArr++){
                                    JSONObject dataNewsNCOV19 = jsonArray.getJSONObject(numberCountArr);
                                    String tittle = dataNewsNCOV19.getString("title");
                                    String description = dataNewsNCOV19.getString("description");


                                    String stHandle = "";
                                    boolean bools = false;
                                    for(int numberTitleCount = 0;numberTitleCount < tittle.length();numberTitleCount++){
                                        if(!bools){
                                            if(!String.valueOf(tittle.toCharArray()[numberTitleCount]).equals(" ")){
                                                stHandle += String.valueOf(tittle.toCharArray()[numberTitleCount]);
                                            }else{
                                                if(stHandle.toLowerCase().equals("covid-19") || stHandle.toLowerCase().equals("duterte")){
                                                    List<String> stringLists = new ArrayList<>();
                                                    bools = true;

                                                    stringLists.add(dataNewsNCOV19.getString("title"));
                                                    stringLists.add(dataNewsNCOV19.getString("url"));
                                                    stringLists.add(dataNewsNCOV19.getString("urlToImage"));
                                                    stringLists.add(dataNewsNCOV19.getString("publishedAt"));
                                                    stringListNews.add(stringLists);
                                                }else{
                                                    stHandle = "";
                                                }
                                            }
                                        }

                                        if(numberTitleCount+1 >= tittle.length()){
                                            if(!bools){
                                                stHandle = "";
                                                bools = false;
                                                for(int numberCountDescrip = 0;numberCountDescrip < description.length();numberCountDescrip++){
                                                    if(!String.valueOf(description.toCharArray()[numberCountDescrip]).equals(" ")){
                                                        stHandle += String.valueOf(description.toCharArray()[numberCountDescrip]);
                                                    }else{
                                                        if(stHandle.toLowerCase().equals("covid-19") || stHandle.toLowerCase().equals("duterte")){
                                                            List<String> stringLists = new ArrayList<>();
                                                            bools = true;

                                                            stringLists.add(dataNewsNCOV19.getString("title"));
                                                            stringLists.add(dataNewsNCOV19.getString("url"));
                                                            stringLists.add(dataNewsNCOV19.getString("urlToImage"));
                                                            stringLists.add(dataNewsNCOV19.getString("publishedAt"));
                                                            stringListNews.add(stringLists);
                                                        }else{
                                                            stHandle = "";
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if(numberCountArr+1 >= jsonArray.length()){
                                        dateLatest.setText("Date Update "+stringListNews.get(0).get(3));

                                        news.this.loadNews();
                                    }
                                }

                            }catch(Exception e){
                                Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                }
            });

            requestQueue.add(jsonRequest);
            return null;
        }
    }


    private void loadNews(){

        newsLayout.removeAllViews();

        if(stringListNews.size() > 0) {
            for (int numberCount = 0; numberCount < stringListNews.toArray().length; numberCount++) {

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 350);
                CardView cardView = new CardView(getActivity());
                layoutParams.setMargins(5, 5, 5, 10);
                cardView.setPadding(5, 5, 5, 5);
                cardView.setContentDescription(String.valueOf(numberCount));
                cardView.setLayoutParams(layoutParams);

                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int numberNewsRow = Integer.parseInt(v.getContentDescription().toString());
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(stringListNews.get(numberNewsRow).get(1))));
                    }
                });

                LinearLayout layout2 = new LinearLayout(getContext());
                layout2.setGravity(Gravity.LEFT);
                layout2.setPadding(10, 10, 10, 10);
                layout2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


                LinearLayout layout = new LinearLayout(getContext());
                layout.setGravity(Gravity.CENTER);
                layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                ImageView img = new ImageView(getContext());
                img.setLayoutParams(new LinearLayout.LayoutParams(350, 350));
                layout.addView(img);


                layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                TextView txtV = new TextView(getContext());
                txtV.setText(stringListNews.get(numberCount).get(0) + "\n\n-Click the article for more info-");
                layoutParams.setMargins(10, 5, 5, 5);
                txtV.setPadding(5, 5, 5, 5);
                txtV.setLayoutParams(layoutParams);


                layout.addView(txtV);
                layout2.addView(layout);
                cardView.addView(layout2);
                newsLayout.addView(cardView);
                Picasso.with(getContext()).load(stringListNews.get(numberCount).get(2)).error(R.drawable.noimageavailable).resize(350, 350).into(img);

                if(numberCount+2 > stringListNews.toArray().length){
                    progressDialog.dismiss();
                    linearLayoutParent.setVisibility(View.VISIBLE);
                }
            }
        }else{
            TextView txt = new TextView(getContext());
            txt.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            txt.setGravity(Gravity.CENTER);
            txt.setText("No news NCOV-19 for now");
            newsLayout.addView(txt);

            progressDialog.dismiss();
            linearLayoutParent.setVisibility(View.VISIBLE);
        }
    }


















}
