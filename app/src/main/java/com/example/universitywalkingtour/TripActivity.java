package com.example.universitywalkingtour;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
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
    //Lists of buildings
    List<Building> allBuildings;
    ArrayList<Building> selectedBuildings;

    //Read File
    DirectionSearch directionSearch;

    //Map
    private GoogleMap mMap;

    //Selection Window
    String buildingTypes[] = {"Academic", "Landscape", "Utility", "Dorm", "Office"};
    boolean[] selectedItems = {false, false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        getSupportActionBar().setTitle("UOP Walk");
        ArrayList<Building> selectedBuildings = new ArrayList<>();
        //Read File
        InputStream inputStream = getResources().openRawResource(R.raw.building_coordinates);
        directionSearch = new DirectionSearch(inputStream);
        allBuildings = directionSearch.readCSV();

        //Map init
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Selection Window
        AlertDialog.Builder selectionWindowBuilder = new AlertDialog.Builder(TripActivity.this);
        selectionWindowBuilder.setCancelable(false);
        selectionWindowBuilder.setTitle("Select Building Types to Visit: ");
        selectionWindowBuilder.setMultiChoiceItems(buildingTypes, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                 selectedItems[i] = isChecked;
            }
        });
        selectionWindowBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                for(int i = 0; i < allBuildings.size(); i++){
                    for(int j = 0; j < selectedItems.length; j++){
                        if(selectedItems[j] == true && allBuildings.get(i).getType().equals(buildingTypes[j])){
                            selectedBuildings.add(allBuildings.get(i));
                        }
                    }
                }
                dialogInterface.dismiss();
            }
        });
        AlertDialog selectionWindow = selectionWindowBuilder.create();
        selectionWindow.setCanceledOnTouchOutside(false);
        selectionWindow.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}