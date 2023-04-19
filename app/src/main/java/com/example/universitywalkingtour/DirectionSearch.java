package com.example.universitywalkingtour;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionSearch {
    InputStream inputStream2;
    private Map<String, Coordinates> locationData;
    List<Building> buildings = new ArrayList<>();

    public DirectionSearch(InputStream inputStream2) {
        locationData = new HashMap<>();
        this.inputStream2 = inputStream2;
       //readCSV();
    }

    public List<Building> readCSV() {

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream2));
        try {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",(?![^(]*\\))");
                if (parts.length == 2) {
                    String name = parts[0].trim().replaceAll("^\"|\"$", "");
                    String coordinates = parts[1].trim().replaceAll("[\\(\\)\"]", "");
                    //String[] coordinates = parts[1].trim().replaceAll("^\\(|\\)$", "").split(",\\s+");
                    String[] latLng = coordinates.split(",");
                    if (latLng.length == 2) {
                        double latitude = Double.parseDouble(latLng[0].trim());
                        double longitude = Double.parseDouble(latLng[1].trim());
                        Building building = new Building(name, latitude, longitude);
                        buildings.add(building);


                        Coordinates buildingCoordinates = new Coordinates(latitude, longitude);
                        locationData.put(name, buildingCoordinates);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV input stream: " + e.getMessage());
        }
        return buildings;
    }

    public double[] getBuildingCoordinates(String name) {
        for (Building building : buildings) {
            if (building.getName().equals(name)) {
                return new double[] {building.getLatitude(), building.getLongitude()};
            }
        }
        return null;
    }

    public Coordinates getCoordinates(String location) {
        Coordinates coordinates = locationData.get(location);
        if (coordinates == null) {
            return null;
        } else {
            return coordinates;
        }
    }

    public Coordinates searchByLocation(String location) {
        return getCoordinates(location);
    }

    public static class Coordinates {
        private double latitude;
        private double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
