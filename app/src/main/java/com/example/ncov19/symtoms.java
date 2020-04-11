package com.example.ncov19;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class symtoms extends Fragment {

    View viewAll;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewAll = inflater.inflate(R.layout.fragment_symtoms, container, false);

        TextView txtSeeMore = (TextView)viewAll.findViewById(R.id.textView25);

        txtSeeMore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.who.int/news-room/q-a-detail/q-a-coronaviruses#:~:text=symptoms")));
            }
        });

        return viewAll;
    }
}
