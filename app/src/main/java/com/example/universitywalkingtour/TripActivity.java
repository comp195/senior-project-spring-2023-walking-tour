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
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TripActivity extends AppCompatActivity implements OnMapReadyCallback {
    //Lists of buildings
    List<Building> allBuildings;
    ArrayList<Building> selectedBuildings;
    ArrayList<Building> wayPoints;
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
        selectedBuildings = new ArrayList<>();

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

                dialogInterface.dismiss();
                showPolyLine();
            }
        });
        AlertDialog selectionWindow = selectionWindowBuilder.create();
        selectionWindow.setCanceledOnTouchOutside(false);
        selectionWindow.show();
    }

    private void showPolyLine() {
        Building end = selectedBuildings.get(selectedBuildings.size() - 1);
        List<Building> waypoints = selectedBuildings.subList(0, selectedBuildings.size() - 1);
        String apiKey = "AIzaSyCJLJ2SKUEYJg3yjLV2JTM5PbDCX89PUbc";
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?";
        String origin = "origin=" + curr_latitude + "," + curr_longitude;
        String destination = "destination=" + end.getLatitude() + "," + end.getLongitude();
        StringBuilder waypointsParam = new StringBuilder("waypoints=");
        for (int i = 0; i < waypoints.size(); i++) {
            Building waypoint = waypoints.get(i);
            waypointsParam.append(waypoint.getLatitude()).append(",").append(waypoint.getLongitude());
            if (i < waypoints.size() - 1) {
                waypointsParam.append("|");
            }
        }
        String url = baseUrl + origin + "&" + destination + "&" + waypointsParam + "&key=" + apiKey;
        System.out.println(url);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray routes = response.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONArray legs = route.getJSONArray("legs");
                        ArrayList<LatLng> polylinePoints = new ArrayList<>();
                        for (int i = 0; i < legs.length(); i++) {
                            JSONObject leg = legs.getJSONObject(i);
                            JSONArray steps = leg.getJSONArray("steps");
                            for (int j = 0; j < steps.length(); j++) {
                                JSONObject step = steps.getJSONObject(j);
                                JSONObject polyline = step.getJSONObject("polyline");
                                String encodedPoints = polyline.getString("points");
                                List<LatLng> decodedPoints = PolyUtil.decode(encodedPoints);
                                polylinePoints.addAll(decodedPoints);
                            }
                        }
                        drawPolyline(polylinePoints);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error sending polly request.");
            }
        });
        requestQueue.add(jsonObjectRequest);

    }

    private void drawPolyline(List<LatLng> points) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .width(10) // Polyline宽度
                .color(Color.BLUE) // Polyline颜色
                .geodesic(true); // 使用大地线插值
        mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        moveToCurrentLocation();
        //getCurrentLocation();
        System.out.println("Current latitude: " + curr_latitude);
        System.out.println("Current longitude: " + curr_longitude);
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
                                    if (locationResult != null && locationResult.getLocations().size() > 0){
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
                    curr_latitude = location.getLatitude();
                    curr_longitude = location.getLongitude();
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