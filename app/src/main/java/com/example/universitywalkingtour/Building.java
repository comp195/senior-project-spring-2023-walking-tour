package com.example.universitywalkingtour;

public class Building {
    private String name;
    private String type;
    private double latitude;
    private double longitude;

    public Building(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
