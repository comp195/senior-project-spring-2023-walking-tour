package com.example.universitywalkingtour;

import static com.example.universitywalkingtour.TripActivity.LOCATION_PERMISSION_REQUEST_CODE;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Response; // Import for Volley's Response class
// import com.google.android.gms.common.api.Response; // Remove or comment out this line


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import java.net.HttpURLConnection;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
//import com.google.android.gms.common.api.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.universitywalkingtour.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import androidx.databinding.DataBindingUtil;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng source;
    private LatLng destination;

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private ActivityMapsBinding binding;
    public double dest_latitude;
    public double dest_longitude;
    public double source_latitude;
    public double source_longitude;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private Marker sourceMarker;

    private Handler handler = new Handler();
    private Runnable markerUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateSourceMarker();
            handler.postDelayed(this, 5000); // Update marker every 5 seconds
        }
    };

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


    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        dest_latitude = intent.getDoubleExtra("dest_latitude", 0.0);
        dest_longitude = intent.getDoubleExtra("dest_longitude", 0.0);
        source_latitude = intent.getDoubleExtra("source_latitude", 0.0);
        source_longitude = intent.getDoubleExtra("source_longitude", 0.0);
        source = new LatLng(source_latitude, source_longitude);
        destination = new LatLng(dest_latitude, dest_longitude);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        /*fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                currentLocation = locationResult.getLastLocation();
                updateSourceMarker();
            }*/
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update the position of the source marker
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    sourceMarker.setPosition(latLng);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(destination);
                    builder.include(source);
                    LatLngBounds bounds = builder.build();
                    int padding = 50; // offset from edges of the map in pixels
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }
            }

        };


    };




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        //LatLng destination = new LatLng(dest_latitude, dest_longitude);
        mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));

        // Add a marker for the source
        //LatLng source = new LatLng(source_latitude, source_longitude);
        sourceMarker = mMap.addMarker(new MarkerOptions().position(source).title("Your Location"));

        // Set the camera position to show both markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(destination);
        builder.include(source);
        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        showPolyLine();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }


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
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        handler.postDelayed(markerUpdateRunnable, 0); // Start updating marker immediately

        // mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        /*mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
        });*/


      /*  String url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + source_latitude + "," + source_longitude +
                "&destination=" + dest_latitude + "," + dest_longitude +
                "&key=" + BuildConfig.MAPS_API_KEY;
        URL urlObject = new URL(url);
        try {
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = urlObject.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        String response = stringBuilder.toString();
        JSONObject jsonObject = new JSONObject(response);
        JSONArray routesArray = jsonObject.getJSONArray("routes");
        JSONObject routeObject = routesArray.getJSONObject(0);
        JSONArray legsArray = routeObject.getJSONArray("legs");
        JSONObject legObject = legsArray.getJSONObject(0);
        JSONObject distanceObject = legObject.getJSONObject("distance");
        String distance = distanceObject.getString("text");
        JSONObject durationObject = legObject.getJSONObject("duration");
        String duration = durationObject.getString("text");
        JSONArray stepsArray = legObject.getJSONArray("steps");
        List<LatLng> polylinePoints = new ArrayList<>();
        for (int i = 0; i < stepsArray.length(); i++) {
            JSONObject stepObject = stepsArray.getJSONObject(i);
            JSONObject polylineObject = stepObject.getJSONObject("polyline");
            String encodedPolyline = polylineObject.getString("points");
            List<LatLng> decodedPolyline = PolyUtil.decode(encodedPolyline);
            polylinePoints.addAll(decodedPolyline);
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(10);
        polylineOptions.addAll(polylinePoints);
        mMap.addPolyline(polylineOptions);
*/
    }
    private void showPolyLine() {
        String apiKey = "AIzaSyCJLJ2SKUEYJg3yjLV2JTM5PbDCX89PUbc";
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?";
        String origin = "origin=" + source_latitude + "," + source_longitude;
        String destination = "destination=" + dest_latitude + "," + dest_longitude;
        StringBuilder waypointsParam = new StringBuilder("waypoints=");


        String url = baseUrl + origin + "&" + destination + "&" + waypointsParam + "&key=" + apiKey +"&optimize=true&mode=bicycling";
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

    private void updateSourceMarker() {
        if (currentLocation != null && sourceMarker != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            sourceMarker.setPosition(latLng);
            Log.d("MapsActivity", "Location callback triggered: " + latLng.toString());
        }
    }
}
