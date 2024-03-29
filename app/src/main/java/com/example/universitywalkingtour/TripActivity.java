package com.example.universitywalkingtour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TripActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MediaPlayer buildingRecordingPlayer;

    //Lists of buildings
    private List<Building> allBuildings;
    private List<Building> wayPoints;
    private List<Building> optimizedRoute;
    private ArrayList<Building> selectedBuildings;
    private ArrayList<Marker> markers;
    //Read File
    private DirectionSearch directionSearch;

    //Map
    private GoogleMap mMap;
    private double curr_latitude;
    private double curr_longitude;
    private static final String TAG = "TripActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    //private Marker userMarker;
    GroundOverlayOptions groundOverlayOptions;
    GroundOverlay groundOverlay;

    //Selection Window
    private String[] buildingTypes = {"Academic", "Landscape", "Utility", "Dorm", "Office"};
    private boolean[] selectedItems = {false, false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        Objects.requireNonNull(getSupportActionBar()).setTitle("UOP Walk");
        selectedBuildings = new ArrayList<>();
        markers = new ArrayList<>();
        optimizedRoute = new ArrayList<>();
        //Read File
        InputStream inputStream = getResources().openRawResource(R.raw.building_coordinates);
        directionSearch = new DirectionSearch(inputStream);
        allBuildings = directionSearch.readCSV();

        //Map init
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //getUserLocation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // 更新 curr_longitude 和 curr_latitude 变量
                    curr_longitude = location.getLongitude();
                    curr_latitude = location.getLatitude();
                    Log.d(TAG, "Latitude: " + curr_latitude + ", Longitude: " + curr_longitude);
                    updateUserMarker();
                    checkIfNearAnyBuilding();
                    if(selectedBuildings.size() != 0){
                        Toast.makeText(TripActivity.this, "Next destination: " + findTheNearestBuildingFromStart().getName(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        // Selection Window
        AlertDialog.Builder selectionWindowBuilder = new AlertDialog.Builder(TripActivity.this);
        selectionWindowBuilder.setCancelable(false);
        selectionWindowBuilder.setTitle("Select Building Type to Visit: ");

        selectionWindowBuilder.setSingleChoiceItems(buildingTypes, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedBuildings.clear();
                for (int j = 0; j < allBuildings.size(); j++) {
                    if (allBuildings.get(j).getType().equals(buildingTypes[i])) {
                        selectedBuildings.add(allBuildings.get(j));
                    }
                }
            }
        });

        selectionWindowBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            // When the user clicked on confirm
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                setBuildingAudioFileResourceIDs();
                dialogInterface.dismiss();
                // showPolyLine
                moveToCurrentLocation();
                showPolyLine();
                addBuildingMarkers();
                // play audio introduction
                buildingRecordingPlayer = MediaPlayer.create(TripActivity.this, R.raw.uopwalk_01);
                buildingRecordingPlayer.start();
            }
        });

        AlertDialog selectionWindow = selectionWindowBuilder.create();
        selectionWindow.setCanceledOnTouchOutside(false);
        selectionWindow.show();
    }

    private void addBuildingMarkers() {
        for(int i = 0; i < selectedBuildings.size(); i++){
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(selectedBuildings.get(i).getLatitude(), selectedBuildings.get(i).getLongitude())).title(selectedBuildings.get(i).getName())));
        }
    }

    private void checkIfNearAnyBuilding() {
        Building buildingToDel = null;
        int buildingToDelIndex = -1;
        for(int i = 0; i < selectedBuildings.size(); i++){
            if(isUserNearCurrentDestination(curr_latitude, curr_longitude, selectedBuildings.get(i).getLatitude(), selectedBuildings.get(i).getLongitude())){
                if(selectedBuildings.get(i).getAudioFileResourceID() != -1 && !buildingRecordingPlayer.isPlaying()){
                    buildingRecordingPlayer = MediaPlayer.create(this, selectedBuildings.get(i).getAudioFileResourceID());
                    buildingRecordingPlayer.start();
                    selectedBuildings.get(i).setIsAudioPlayed(true);
                    buildingToDel = selectedBuildings.get(i);
                }
                for(int j = 0; j < markers.size(); j++){
                    if(markers.get(j).getTitle().equals(selectedBuildings.get(i).getName()) && selectedBuildings.get(i).getIsAudioPlayed()){
                        markers.get(j).remove();
                        buildingToDelIndex = j;
                        break;
                    }
                }
                if(buildingToDel != null){
                    selectedBuildings.remove(buildingToDel);
                }
                if(buildingToDelIndex != -1){
                    markers.remove(markers.get(buildingToDelIndex));
                }
                break;
            }
        }
    }

    private boolean isUserNearCurrentDestination(double curr_latitude, double curr_longitude, double dest_latitude, double dest_longitude) {
        double distance = calculateDistance(curr_latitude, curr_longitude, dest_latitude, dest_longitude);
        return distance <= 20; // 判断用户是否在100米内
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径，单位为公里
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // 距离，单位为公里
        return distance * 1000; // 将距离转换为米
    }

    private List<Building> nearestNeighborAlgorithm() {
        System.out.println("Selected Buildings: ");
        for (int i = 0; i < selectedBuildings.size(); i++) {
            System.out.println(selectedBuildings.get(i).getName());
        }
        List<Building> remainingBuildings = new ArrayList<>(selectedBuildings);
        Building currentBuilding = findTheNearestBuildingFromStart();
        optimizedRoute.add(currentBuilding);
        remainingBuildings.remove(currentBuilding);
        while (!remainingBuildings.isEmpty()) {
            double minDistance = Double.MAX_VALUE;
            Building nearestBuilding = null;
            for (Building building : remainingBuildings) {
                double distance = calculateDistance(currentBuilding.getLatitude(), currentBuilding.getLongitude(),
                        building.getLatitude(), building.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestBuilding = building;
                }
            }
            remainingBuildings.remove(nearestBuilding);
            optimizedRoute.add(nearestBuilding);
            currentBuilding = nearestBuilding;
        }
        System.out.println("optimizedRoute Buildings: ");
        for (int i = 0; i < optimizedRoute.size(); i++) {
            System.out.println(optimizedRoute.get(i).getName());
        }
        return optimizedRoute;
    }

    private Building findTheNearestBuildingFromStart() {
        double minDistance = Double.MAX_VALUE;
        Building nearestBuilding = null;
        for (Building building : selectedBuildings) {
            double distance = calculateDistance(curr_latitude, curr_longitude,
                    building.getLatitude(), building.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearestBuilding = building;
            }
        }
        return nearestBuilding;
    }

    private void showPolyLine() {
        optimizedRoute = nearestNeighborAlgorithm();
        String apiKey = getApiKeyFromConfig();
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?";
        String origin = "origin=" + curr_latitude + "," + curr_longitude;
        wayPoints = optimizedRoute.subList(0, selectedBuildings.size() - 1);
        System.out.println("wayPoints Buildings: ");
        for (int i = 0; i < wayPoints.size(); i++) {
            System.out.println(wayPoints.get(i).getName());
        }
        StringBuilder waypointsParam = new StringBuilder("waypoints=");
        for (int i = 0; i < wayPoints.size(); i++) {
            Building waypoint = wayPoints.get(i);
            waypointsParam.append(waypoint.getLatitude()).append(",").append(waypoint.getLongitude());
            if (i < wayPoints.size() - 1) {
                waypointsParam.append("|");
            }
        }
        String destination = "destination=" + optimizedRoute.get(optimizedRoute.size() - 1).getLatitude() + "," + optimizedRoute.get(optimizedRoute.size() - 1).getLongitude();
        String url = baseUrl + origin + "&" + destination + "&" + waypointsParam + "&key=" + apiKey + "&optimize=true&mode=bicycling";
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
                .width(8) // Polyline宽度
                .color(Color.BLUE) // Polyline颜色
                .geodesic(true); // 使用大地线插值
        System.out.println("Made this far?");
        mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        if (buildingRecordingPlayer != null) {
            buildingRecordingPlayer.stop();
            buildingRecordingPlayer.release();
            buildingRecordingPlayer = null;
        }
    }

    private void moveToCurrentLocation() {
        LatLng currentLatLng = new LatLng(curr_latitude, curr_longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 17); // Change the '15' to your desired zoom level
        mMap.animateCamera(cameraUpdate);
    }

    private void updateUserMarker() {
        LatLng overlayCenter = new LatLng(curr_latitude, curr_longitude);
        float overlayWidth = 24f;
        float overlayHeight = 24f;
        if(groundOverlayOptions == null){
            groundOverlayOptions = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.user_icon))
                    .position(overlayCenter, overlayWidth, overlayHeight);
            groundOverlay = mMap.addGroundOverlay(groundOverlayOptions);
        }
        else{
            groundOverlay.setPosition(overlayCenter);
        }
        //userMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(curr_latitude, curr_longitude)).title("Your Location"));
    }

    @SuppressLint("DiscouragedApi")
    private void setBuildingAudioFileResourceIDs() {
        for(int i = 0; i < selectedBuildings.size(); i++){
            if(!Objects.equals(selectedBuildings.get(i).getAudioFileName(), "null")){
                selectedBuildings.get(i).setAudioFileResourceID(getResources().getIdentifier(selectedBuildings.get(i).getAudioFileName(), "raw", getPackageName()));
            }
            else{
                selectedBuildings.get(i).setIsAudioPlayed(true);
            }
        }
    }

    public String getApiKeyFromConfig() {
        String apiKey = "";
        try {
            InputStream inputStream = getApplicationContext().getAssets().open("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            apiKey = properties.getProperty("API_KEY");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiKey;
    }
}