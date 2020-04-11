package com.example.ncov19;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.BundleCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class home extends Fragment {


    private View viewAll;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    private String[][] arrCountries;
    private androidx.constraintlayout.widget.ConstraintLayout layoutHome;
    private int[] countAllAffected, countCountriesPH;
    private TextView totalCases, DeathsTextView, RecoveredTextView, textViewConfirmsAlljar, textViewDeathsAlljar, textViewRecoveredAlljar,
            textViewAffectedAlljar, textView5, textViewRateMorality, textViewRateRecovery,
            textViewConfirmPercent, textViewDeathsPercents, textViewRecoveredPercent;
    private String[] month;
    private DatabaseReference dataRef;
    private List<String> confirmedCases, deathCases, recoveryCases;

    private boolean booleanConditionToAll;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewAll = inflater.inflate(R.layout.fragment_home, container, false);

        dataRef = FirebaseDatabase.getInstance().getReference("NCOV19_Cases_PHILIPPINES");


        textView5 = viewAll.findViewById(R.id.textView5);

        totalCases = viewAll.findViewById(R.id.totalCases);
        DeathsTextView = viewAll.findViewById(R.id.DeathsTextView);
        RecoveredTextView = viewAll.findViewById(R.id.RecoveredTextView);
        textViewRateMorality = viewAll.findViewById(R.id.textViewRateMorality);
        textViewRateRecovery = viewAll.findViewById(R.id.textViewRateRecovery);

        textViewConfirmPercent = viewAll.findViewById(R.id.textViewConfirmPercent);
        textViewDeathsPercents = viewAll.findViewById(R.id.textViewDeathsPercents);
        textViewRecoveredPercent = viewAll.findViewById(R.id.textViewRecoveredPercent);


        textViewConfirmsAlljar = viewAll.findViewById(R.id.textViewConfirmsAlljar);
        textViewDeathsAlljar = viewAll.findViewById(R.id.textViewDeathsAlljar);
        textViewRecoveredAlljar = viewAll.findViewById(R.id.textViewRecoveredAlljar);
        textViewAffectedAlljar = viewAll.findViewById(R.id.textViewAffectedAlljar);


        booleanConditionToAll = false;

        layoutHome = viewAll.findViewById(R.id.layoutHome);
        layoutHome.setVisibility(View.INVISIBLE);
        progressDialog = new ProgressDialog(getContext());
        requestQueue = Volley.newRequestQueue(getActivity());
        home.this.handlingCountries();
        countAllAffected = new int[] {0, 0, 0};
        countCountriesPH = new int[] {0, 0, 0};
        confirmedCases = new ArrayList<>();
        deathCases = new ArrayList<>();
        recoveryCases = new ArrayList<>();

        getDataNCOV19 covidDataGet = new getDataNCOV19();
        covidDataGet.execute();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!booleanConditionToAll) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Reload", Toast.LENGTH_LONG).show();


                    getDataNCOV19 covidDataGet = new getDataNCOV19();
                    covidDataGet.execute();

                    new Handler().postDelayed(this, 10000);
                }
            }
        }, 10000);

        return viewAll;
    }


    private class getDataNCOV19 extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.show();
            progressDialog.setContentView(R.layout.loadingstatement);
            Window window = progressDialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final String linkDataNCOV19 = "https://2019ncov.asia/api/country_region";

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DEPRECATED_GET_OR_POST, linkDataNCOV19, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            countAllAffected = new int[] {0, 0, 0};
                            countCountriesPH = new int[] {0, 0, 0};
                            confirmedCases = new ArrayList<>();
                            deathCases = new ArrayList<>();
                            recoveryCases = new ArrayList<>();

                            try{
                                  JSONArray jsonArray = response.getJSONArray("results");
                                  for(int numberCount2 = 0;numberCount2 < jsonArray.length();numberCount2++){
                                      JSONObject dataNCOV19 = jsonArray.getJSONObject(numberCount2);


                                      countAllAffected[0] += Integer.parseInt(dataNCOV19.getString("confirmed"));
                                      countAllAffected[1] += Integer.parseInt(dataNCOV19.getString("deaths"));
                                      countAllAffected[2] += Integer.parseInt(dataNCOV19.getString("recovered"));

                                      if(dataNCOV19.getString("country_region").equals("Philippines")) {
                                          countCountriesPH[0] = Integer.parseInt(dataNCOV19.getString("confirmed"));
                                          countCountriesPH[1] = Integer.parseInt(dataNCOV19.getString("deaths"));
                                          countCountriesPH[2] = Integer.parseInt(dataNCOV19.getString("recovered"));
                                      }

                                      if(numberCount2+2 > jsonArray.length()){
                                          textViewAffectedAlljar.setText(String.valueOf(jsonArray.length()));

                                          //Get the updated date of philippines data________________________________________
                                          String getDatePhilipinesUpdate = "https://pomber.github.io/covid19/timeseries.json";

                                          JsonObjectRequest jsonRequests = new JsonObjectRequest(Request.Method.DEPRECATED_GET_OR_POST, getDatePhilipinesUpdate, null,
                                                  new Response.Listener<JSONObject>() {
                                                      @Override
                                                      public void onResponse(JSONObject response) {
                                                          try {
                                                              JSONArray jsonArray = response.getJSONArray("Philippines");
                                                              JSONObject dataNCOV19 = jsonArray.getJSONObject(jsonArray.length()-1);

                                                              String[] handleDate = new String[]{"", "", ""};
                                                              int numberCountForeDateHandle = 0;
                                                              for(int numberDate = 0;numberDate < dataNCOV19.getString("date").toCharArray().length;numberDate++){
                                                                  if(!String.valueOf(dataNCOV19.getString("date").toCharArray()[numberDate]).equals("-")){
                                                                      handleDate[numberCountForeDateHandle] += dataNCOV19.getString("date").toCharArray()[numberDate];
                                                                  }else{
                                                                      numberCountForeDateHandle += 1;
                                                                  }

                                                                  if(numberDate+1 >= dataNCOV19.getString("date").length()){
                                                                      String handleMonth = "";
                                                                      for(int arrDateCount = 0;arrDateCount < month.length;arrDateCount++){
                                                                          if(handleDate[1].equals(String.valueOf(arrDateCount+1))){
                                                                              handleMonth = month[arrDateCount];
                                                                          }

                                                                          if(arrDateCount+2 > month.length){
                                                                              textView5.setText("Last update: "+handleMonth+" "+
                                                                                      (Integer.parseInt(handleDate[handleDate.length-1]) > 9 ? handleDate[handleDate.length-1]:
                                                                                              "0"+handleDate[handleDate.length-1])+", "+handleDate[0]);

                                                                              //ConfirmedCases______________________________________________________________
                                                                              dataRef.child("confirmedCases").addValueEventListener(new ValueEventListener() {
                                                                                  @Override
                                                                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                                      for(DataSnapshot dataSnap: dataSnapshot.getChildren()){
                                                                                          confirmedCases.add(dataSnap.getValue().toString());
                                                                                      }

                                                                                      //DeathCases________________________________________________________
                                                                                      dataRef.child("deathCases").addValueEventListener(new ValueEventListener() {
                                                                                          @Override
                                                                                          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                                              for(DataSnapshot dataSnap: dataSnapshot.getChildren()){
                                                                                                  deathCases.add(dataSnap.getValue().toString());
                                                                                              }

                                                                                              //RecoveryCases__________________________________________________________
                                                                                              dataRef.child("recoveredCases").addValueEventListener(new ValueEventListener() {
                                                                                                  @Override
                                                                                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                      for(DataSnapshot dataSnap: dataSnapshot.getChildren()){
                                                                                                          recoveryCases.add(dataSnap.getValue().toString());
                                                                                                      }


                                                                                                      //ConfirnmCases_____________________________________________________________________________________
                                                                                                      if(!confirmedCases.get(confirmedCases.size()-1).equals(String.valueOf(countCountriesPH[0]))){

                                                                                                          dataRef.child("confirmedCases").child(dataRef.push().getKey()).setValue(countCountriesPH[0]);

                                                                                                          for(int numberCountss = 0; numberCountss < 3;numberCountss++) {
                                                                                                              double doubles = Double.parseDouble("" + (countCountriesPH[0] - Integer.parseInt(confirmedCases.get(confirmedCases.size() - 1))));
                                                                                                              textViewConfirmPercent.setText("+" + (countCountriesPH[0] - Integer.parseInt(confirmedCases.get(confirmedCases.size() - 1))) +
                                                                                                                      "(+" + returnFinalnumber(String.valueOf((doubles * (100.0 / Double.parseDouble("" + countCountriesPH[0]))))) + "%)");
                                                                                                          }


                                                                                                      }else{
                                                                                                          for(int numberCountss = 0; numberCountss < 3;numberCountss++) {
                                                                                                              double doubles = Double.parseDouble("" + (countCountriesPH[0] - Integer.parseInt(confirmedCases.get(confirmedCases.size() - 2))));
                                                                                                              textViewConfirmPercent.setText("+" + (countCountriesPH[0] - Integer.parseInt(confirmedCases.get(confirmedCases.size() - 2))) +
                                                                                                                      "(+" + returnFinalnumber(String.valueOf((doubles * (100.0 / Double.parseDouble("" + countCountriesPH[0]))))) + "%)");
                                                                                                          }
                                                                                                      }



                                                                                                      //Death Cases________________________________________________________________________________
                                                                                                      if(!deathCases.get(deathCases.size()-1).equals(String.valueOf(countCountriesPH[1]))){

                                                                                                          dataRef.child("deathCases").child(dataRef.push().getKey()).setValue(countCountriesPH[1]);

                                                                                                          for(int numberCountss = 0; numberCountss < 3;numberCountss++) {
                                                                                                              double doubless = Double.parseDouble("" + (countCountriesPH[1] - Integer.parseInt(deathCases.get(deathCases.size() - 1))));
                                                                                                              textViewDeathsPercents.setText("+" + (countCountriesPH[1] - Integer.parseInt(deathCases.get(deathCases.size() - 1))) +
                                                                                                                      "(+" + returnFinalnumber(String.valueOf((doubless * (100.0 / Double.parseDouble("" + countCountriesPH[0]))))) + "%)");
                                                                                                          }


                                                                                                      }else{
                                                                                                          for(int numberCountss = 0; numberCountss < 3;numberCountss++) {
                                                                                                              double doubless = Double.parseDouble("" + (countCountriesPH[1] - Integer.parseInt(deathCases.get(deathCases.size() - 2))));
                                                                                                              textViewDeathsPercents.setText("+" + (countCountriesPH[1] - Integer.parseInt(deathCases.get(deathCases.size() - 2))) +
                                                                                                                      "(+" + returnFinalnumber(String.valueOf((doubless * (100.0 / Double.parseDouble("" + countCountriesPH[0]))))) + "%)");
                                                                                                          }

                                                                                                      }


                                                                                                      //Recovered Cases______________________________________________________________________
                                                                                                      if(!recoveryCases.get(recoveryCases.size()-1).equals(String.valueOf(countCountriesPH[2]))){

                                                                                                          dataRef.child("recoveredCases").child(dataRef.push().getKey()).setValue(countCountriesPH[2]);

                                                                                                          for(int numberCountss = 0; numberCountss < 3;numberCountss++) {
                                                                                                              double doubless = Double.parseDouble("" + (countCountriesPH[2] - Integer.parseInt(recoveryCases.get(recoveryCases.size() - 1))));
                                                                                                              textViewRecoveredPercent.setText("+" + (countCountriesPH[2] - Integer.parseInt(recoveryCases.get(recoveryCases.size() - 1))) +
                                                                                                                      "(+" + returnFinalnumber(String.valueOf((doubless * (100.0 / Double.parseDouble("" + countCountriesPH[0]))))) + "%)");
                                                                                                          }


                                                                                                      }else{
                                                                                                          for(int numberCountss = 0; numberCountss < 3;numberCountss++) {
                                                                                                              double doubless = Double.parseDouble("" + (countCountriesPH[2] - Integer.parseInt(recoveryCases.get(recoveryCases.size() - 2))));
                                                                                                              textViewRecoveredPercent.setText("+" + (countCountriesPH[2] - Integer.parseInt(recoveryCases.get(recoveryCases.size() - 2))) +
                                                                                                                      "(+" + returnFinalnumber(String.valueOf((doubless * (100.0 / Double.parseDouble("" + countCountriesPH[0]))))) + "%)");
                                                                                                          }

                                                                                                      }
                                                                                                      booleanConditionToAll = true;
                                                                                                      home.this.showTheDataFinal();
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                                      Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                                                                                                      booleanConditionToAll = false;
                                                                                                  }
                                                                                              });



                                                                                          }

                                                                                          @Override
                                                                                          public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                              Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                                                                                              booleanConditionToAll = false;
                                                                                          }
                                                                                      });




                                                                                  }

                                                                                  @Override
                                                                                  public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                      Toast.makeText(getContext(), "Check your connection", Toast.LENGTH_LONG).show();
                                                                                      booleanConditionToAll = false;
                                                                                  }
                                                                              });


                                                                          }
                                                                      }
                                                                  }
                                                              }


                                                          }catch (Exception e){
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
                                          requestQueue.add(jsonRequests);
                                      }
                                  }
                            }catch(Exception e){
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
                        }
                    );

            requestQueue.add(jsonRequest);
            return null;
        }
    }

    private void showTheDataFinal(){
        progressDialog.dismiss();
        layoutHome.setVisibility(View.VISIBLE);

        for(int numberCount1 = 0;numberCount1 < 3;numberCount1++) {
            textViewRateMorality.setText("Morality Rate: " +
                    returnFinalnumber(String.valueOf((Double.parseDouble("" + countCountriesPH[1]) * 100.0) /
                            Double.parseDouble("" + countCountriesPH[0]))) + "%");
        }

        for(int numberCount2 = 0;numberCount2 < 3;numberCount2++) {
            textViewRateRecovery.setText("Recovery Rate: " +
                    returnFinalnumber(String.valueOf((Double.parseDouble("" + countCountriesPH[2]) * 100.0) /
                            Double.parseDouble("" + countCountriesPH[0]))) + "%");
        }

        for(int numberCouns = 0;numberCouns < 3;numberCouns++) {
            totalCases.setText(numberSeperated(String.valueOf(countCountriesPH[0])));
            DeathsTextView.setText(numberSeperated(String.valueOf(countCountriesPH[1])));
            RecoveredTextView.setText(numberSeperated(String.valueOf(countCountriesPH[2])));

            textViewConfirmsAlljar.setText(numberSeperated(String.valueOf(countAllAffected[0])));
            textViewDeathsAlljar.setText(numberSeperated(String.valueOf(countAllAffected[1])));
            textViewRecoveredAlljar.setText(numberSeperated(String.valueOf(countAllAffected[2])));
        }

    }


    private String returnFinalnumber(String handleNumber){
        String handleFinal = "";
        int count = 0;
        boolean condition = false;
        if(handleNumber.length() > 3){
            for(int numberCount = 0;numberCount < handleNumber.length();numberCount++){
                if(!String.valueOf(handleNumber.toCharArray()[numberCount]).equals(".")){
                    if(!condition) {
                        handleFinal += String.valueOf(handleNumber.toCharArray()[numberCount]);
                    }else{
                        if(count < 2){
                            handleFinal += String.valueOf(handleNumber.toCharArray()[numberCount]);
                            count++;
                        }
                    }
                }else{
                    handleFinal += String.valueOf(handleNumber.toCharArray()[numberCount]);
                    condition = true;
                }
            }
        }else{
            return handleNumber;
        }

        return handleFinal;
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

        month = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        arrCountries = new String[][]{
                {"US", "USA"},
                {"ES", "Spain"},
                {"IT", "Italy"},
                {"DE", "Germany"},
                {"FR", "France"},
                {"IR", "Iran"},
                {"GB", "UK"},
                {"TR", "Turkey"},
                {"CH", "Switzerland"},
                {"BE", "Belgium"},
                {"AN", "Netherlands"},
                {"CA", "Canada"},
                {"AT", "Austria"},
                {"PT", "Portugal"},
                {"BR", "Brazil"},
                {"KR", "S. Korea"},
                {"IL", "Israel"},
                {"SE", "Sweden"},
                {"AU", "Australia"},
                {"NO", "Norway"},
                {"RU", "Russia"},
                {"IE", "Ireland"},
                {"CZ", "Czechia"},
                {"DK", "Denmark"},
                {"CL", "Chile"},
                {"RO", "Romania"},
                {"PL", "Poland"},
                {"MY", "Malaysia"},
                {"IN", "India"},
                {"EC", "Ecuador"},
                {"PH", "Philippines"},
                {"JP", "Japan"},
                {"PK", "Pakistan"},
                {"LU", "Luxembourg"},
                {"SA", "Saudi Arabia"},
                {"ID", "Indonesia"},
                {"TH", "Thailand"},
                {"FI","Finland"},
                {"MX", "Mexico"},
                {"PA", "Panama"},
                {"PE", "Peru"},
                {"GR", "Greece"},
                {"RS", "Serbia"},
                {"ZA", "South Africa"},
                {"DO", "Dominican Republic"},
                {"AE", "UAE"},
                {"AR", "Argentina"},
                {"IS", "Iceland"},
                {"CO", "Colombia"},
                {"QA", "Qatar"},
                {"SG", "Singapore"},
                {"DZ", "Algeria"},
                {"UA", "Ukraine"},
                {"HR", "Croatia"},
                {"EE", "Estonia"},
                {"EG", "Egypt"},
                {"NZ", "New Zealand"},
                {"SI", "Slovenia"},
                {"MA", "Morocco"},
                {"HK", "Hong Kong"},
                {"IQ", "Iraq"},
                {"AM", "Armenia"},
                {"LT", "Lithuania"},
                {"MD", "Moldova, Republic of"},
                {"HU", "Hungary"},
                {"", "Diamond Princess", ""},
                {"BH", "Bahrain"},
                {"BA", "Bosnia"},
                {"AZ", "Azerbaijan"},
                {"KZ", "Kazakhstan"},
                {"BY", "Belarus"},
                {"KW", "Kuwait"},
                {"MK", "Macedonia"},
                {"CM", "Cameroon"},
                {"TN", "Tunisia"},
                {"LV", "Latvia"},
                {"LB", "Lebanon"},
                {"BG", "Bulgaria"},
                {"AD", "Andorra"},
                {"SK", "Slovakia"},
                {"CR", "Costa Rica"},
                {"CY", "Cyprus"},
                {"UY", "Uruguay"},
                {"TW", "Taiwan"},
                {"AL", "Albania"},
                {"AF", "Afghanistan"},
                {"RE", "Réunion"},
                {"JO", "Jordan"},
                {"BF", "Burkina Faso"},
                {"OM", "Oman"},
                {"UZ", "Uzbekistan"},
                {"CU", "Cuba"},
                {"HN", "Honduras"},
                {"SM", "San Marino"},
                {"JE", "Channel Islands"},
                {"", "Côte d'Ivoire", ""},
                {"VN", "Vietnam"},
                {"MU", "Mauritius"},
                {"MT", "Malta"},
                {"PS", "Palestinian Territory, Occupied"},
                {"NG", "Nigeria"},
                {"SN", "Senegal"},
                {"GH", "Ghana"},
                {"ME", "Montenegro"},
                {"FO", "Faroe Islands"},
                {"LK", "Sri Lanka"},
                {"GE", "Georgia"},
                {"BO", "Bolivia"},
                {"VE", "Venezuela"},
                {"CD", "DRC"},
                {"KG", "Kyrgyzstan"},
                {"MQ", "Martinique"},
                {"NE", "Niger"},
                {"BN", "Brunei"},
                {"GP", "Guadeloupe"},
                {"YT", "Mayotte"},
                {"IM", "Isle of Man"},
                {"KE", "Kenya"},
                {"KH", "Cambodia"},
                {"GN", "Guinea"},
                {"PY", "Paraguay"},
                {"TT", "Trinidad and Tobago"},
                {"RW", "Rwanda"},
                {"GI", "Gibraltar"},
                {"BD", "Bangladesh"},
                {"LI", "Liechtenstein"},
                {"MG", "Madagascar"},
                {"MC", "Monaco"},
                {"AW", "Aruba"},
                {"SV", "El Salvador"},
                {"GT", "Guatemala"},
                {"GF", "French Guiana"},
                {"JM", "Jamaica"},
                {"BB", "Barbados"},
                {"DJ", "Djibouti"},
                {"UG", "Uganda"},
                {"CG", "Congo"},
                {"MO", "Macao"},
                {"ET", "Ethiopia"},
                {"ML", "Mali"},
                {"TG", "Togo"},
                {"PF", "French Polynesia"},
                {"ZM", "Zambia"},
                {"BM", "Bermuda"},
                {"KY", "Cayman Islands"},
                {"MF", "Saint Martin"},
                {"ER", "Eritrea"},
                {"BS", "Bahamas"},
                {"SX", "Sint Maarten"},
                {"GY", "Guyana"},
                {"MM", "Myanmar"},
                {"HT", "Haiti"},
                {"TZ", "Tanzania, United Republic of"},
                {"SY", "Syrian Arab Republic"},
                {"MV", "Maldives"},
                {"LY", "Libyan Arab Jamahiriya"},
                {"GW", "Guinea-Bissau"},
                {"NC", "New Caledonia"},
                {"BJ", "Benin"},
                {"GQ", "Equatorial Guinea"},
                {"NA", "Namibia"},
                {"AG", "Antigua and Barbuda"},
                {"DM", "Dominica"},
                {"MN", "Mongolia"},
                {"LC", "Saint Lucia"},
                {"LR", "Liberia"},
                {"FJ", "Fiji"},
                {"GD", "Grenada"},
                {"CW", "Curaçao"},
                {"GL", "Greenland"},
                {"LA", "Lao People's Democratic Republic"},
                {"AO", "Angola"},
                {"SD", "Sudan"},
                {"SR", "Suriname"},
                {"MZ", "Mozambique"},
                {"SC", "Seychelles"},
                {"", "MS Zaandam", ""},
                {"ZW", "Zimbabwe"},
                {"NP", "Nepal"},
                {"TD", "Chad"},
                {"KN", "Saint Kitts and Nevis"},
                {"SZ", "Swaziland"},
                {"CF", "Central African Republic"},
                {"CV", "Cabo Verde"},
                {"VA", "Holy See (Vatican City State)"},
                {"SO", "Somalia"},
                {"MR", "Mauritania"},
                {"MS", "Montserrat"},
                {"BL", "St. Barth"},
                {"SL", "Sierra Leone"},
                {"GA", "Gabon"},
                {"NI", "Nicaragua"},
                {"VC", "Saint Vincent and the Grenadines"},
                {"BT", "Bhutan"},
                {"TC", "Turks and Caicos Islands"},
                {"BW", "Botswana"},
                {"GM", "Gambia"},
                {"BZ", "Belize"},
                {"MW", "Malawi"},
                {"EH", "Western Sahara"},
                {"AI", "Anguilla"},
                {"VG", "British Virgin Islands"},
                {"BI", "Burundi"},
                {"BQ", "Caribbean Netherlands"},
                {"FK", "Falkland Islands (Malvinas)"},
                {"PG", "Papua New Guinea"},
                {"PM", "Saint Pierre Miquelon"},
                {"SS", "South Sudan"},
                {"TL", "Timor-Leste"},
                {"CN", "China"}
        };
    }
}














