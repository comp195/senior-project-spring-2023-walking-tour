package com.example.universitywalkingtour;

import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.View;
import android.widget.AdapterView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;


// ...
// ...
// ...
// ...
public class DirectionSearchActivity extends AppCompatActivity {
    SearchView searchDirection;
    ListView listDirection;
    ArrayList<String> arrayBuildings;
    ArrayAdapter<String> adapterBuildings;

    DirectionSearch directionSearch;
    List<Building> buildings;
    List<String> choice;

    double source_latitude;
    double source_longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        source_latitude = intent.getDoubleExtra("source_latitude", 0.0);
        source_longitude = intent.getDoubleExtra("source_longitude", 0.0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction_search);
        InputStream inputStream = getResources().openRawResource(R.raw.building_coordinates);
        directionSearch = new DirectionSearch(inputStream);
        buildings = directionSearch.readCSV();
        searchDirection = findViewById(R.id.searchDirection);
        listDirection = findViewById(R.id.listDirection);
        listDirection.setVisibility(View.GONE);
        arrayBuildings = new ArrayList<>();
        for (Building building : buildings) {
            String name = building.getName();
            if (name != null && !name.isEmpty()) {
                arrayBuildings.add(name);
            }
        }
     /*   if (arrayBuildings != null && !arrayBuildings.isEmpty()) {
            adapterBuildings = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayBuildings);
            listDirection.setAdapter(adapterBuildings);
        }*/
        adapterBuildings = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayBuildings);
        listDirection.setAdapter(adapterBuildings);

        //searchDirection.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        //searchDirection.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchDirection.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (adapterBuildings != null) { // Check if adapterBuildings is not null
                    listDirection.setVisibility(View.VISIBLE);
                    adapterBuildings.getFilter().filter(s);
                }
                return false;
            }
        });
        // Listen for clicks on the list items
        listDirection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the name of the selected building
                String name = adapterBuildings.getItem(position);

                // Look up the coordinates of the building
                double[] coordinates = directionSearch.getBuildingCoordinates(name);

                // Start the MapActivity and pass the coordinates as extras
                Intent intent = new Intent(DirectionSearchActivity.this, MapsActivity.class);
                intent.putExtra("dest_latitude", coordinates[0]);
                intent.putExtra("dest_longitude", coordinates[1]);
                intent.putExtra("source_latitude", source_latitude);
                intent.putExtra("source_longitude", source_longitude);
                startActivity(intent);

                startActivity(intent);
            }
        });




        // Make sure listDirection is visible and enabled
        listDirection.setVisibility(View.VISIBLE);
        listDirection.setEnabled(true);
        listDirection.setFocusable(true);
    }
}
// ...
