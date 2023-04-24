package com.example.universitywalkingtour;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.universitywalkingtour.databinding.ActivityMapsBinding;
import android.content.Intent;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    public double dest_latitude;
    public double dest_longitude;
    public double source_latitude;
    public double source_longitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
         dest_latitude = intent.getDoubleExtra("dest_latitude", 0.0);
         dest_longitude = intent.getDoubleExtra("dest_longitude", 0.0);
         source_latitude = intent.getDoubleExtra("source_latitude", 0.0);
         source_longitude = intent.getDoubleExtra("source_longitude", 0.0);


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

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

        LatLng destination = new LatLng(dest_latitude, dest_longitude);
        mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));

        // Add a marker for the source and move the camera
        LatLng source = new LatLng(source_latitude, source_longitude);
        mMap.addMarker(new MarkerOptions().position(source).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(source));
    }
}