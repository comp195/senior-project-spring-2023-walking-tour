package com.example.universitywalkingtour;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.View;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class start_page extends AppCompatActivity {
    SearchView searchUOPP;
    ListView listUOPP;
    ArrayList <String> arrayBuildings;
    ArrayAdapter <String> adapterBuildings;
    InputStream inputStream = getResources().openRawResource(R.raw.buildings);
    CSVFile csvFile = new CSVFile(inputStream);
    List buildings = csvFile.read();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        searchUOPP = findViewById(R.id.searchUOP);
        listUOPP = findViewById(R.id.listUOP);
        listUOPP.setVisibility(View.GONE);
        arrayBuildings = new ArrayList <>();
        arrayBuildings.add("Baun Hall");

        adapterBuildings = new ArrayAdapter <>(this,android.R.layout.simple_list_item_1, buildings);
        listUOPP.setAdapter(adapterBuildings);
        searchUOPP.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                listUOPP.setVisibility(View.VISIBLE);
                adapterBuildings.getFilter().filter(s);

                return false;

            }
        });

    }
}