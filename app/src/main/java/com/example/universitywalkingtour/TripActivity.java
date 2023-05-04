package com.example.universitywalkingtour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

//push test
public class TripActivity extends AppCompatActivity implements OnMapReadyCallback {
    //Lists of buildings
    List<Building> allBuildings;
    ArrayList<Building> selectedBuildings;

    //Read File
    DirectionSearch directionSearch;

    //Map
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private double curr_latitude;
    private double curr_longitude;

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
            //When the user clicked on confirm
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //Add corresponding buildings to the list
                for(int i = 0; i < allBuildings.size(); i++){
                    for(int j = 0; j < selectedItems.length; j++){
                        if(selectedItems[j] == true && allBuildings.get(i).getType().equals(buildingTypes[j])){
                            selectedBuildings.add(allBuildings.get(i));
                        }
                    }
                }
                setBuildingAudioFileResourceIDs();

                //Ask location permission
                getCurrentLocation();
                moveToCurrentLocation();
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

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(TripActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(TripActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(TripActivity.this)
                                            .removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size() >0){
                                        int index = locationResult.getLocations().size() - 1;
                                        curr_latitude = locationResult.getLocations().get(index).getLatitude();
                                        curr_longitude = locationResult.getLocations().get(index).getLongitude();
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(TripActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(TripActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    private void moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15); // Change the '15' to your desired zoom level
                    mMap.animateCamera(cameraUpdate);
                }
            }
        });
    }

    private void setBuildingAudioFileResourceIDs() {
        for(int i = 0; i < selectedBuildings.size(); i++){
            if(selectedBuildings.get(i).getAudioFileName() != "null"){
                selectedBuildings.get(i).setAudioFileResourceID(getResources().getIdentifier(selectedBuildings.get(i).getAudioFileName(), "raw", getPackageName()));
                System.out.println(selectedBuildings.get(i).getAudioFileResourceID());
            }
        }
    }
}