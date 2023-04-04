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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
public class TripActivity extends AppCompatActivity implements OnMapReadyCallback {
    SearchView searchUOPP;
    ListView listUOPP;
    ArrayList <String> arrayBuildings;
    ArrayAdapter <String> adapterBuildings;
    //InputStream inputStream = getResources().openRawResource(R.raw.buildings);
    //CSVFile csvFile = new CSVFile(inputStream);
    //List buildings = csvFile.read();

    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        searchUOPP = findViewById(R.id.searchUOP);
        listUOPP = findViewById(R.id.listUOP);
        listUOPP.setVisibility(View.GONE);
        arrayBuildings = new ArrayList <>();
        arrayBuildings.add("Baun Hall");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //adapterBuildings = new ArrayAdapter <>(this,android.R.layout.simple_list_item_1, buildings);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}