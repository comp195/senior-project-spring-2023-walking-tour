package com.example.universitywalkingtour;

public class Building {
    private int id;
    private String name;
    private String type;
    private double latitude;
    private double longitude;
    private String audioFileName;
    private int audioFileResourceID;
    private boolean isAudioPlayed;

    public Building(int id, String name, String type, double latitude, double longitude, String audioFileName) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.audioFileName = audioFileName;
        this.audioFileResourceID = -1;
        this.isAudioPlayed = false;
    }
    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileResourceID(int audioFileResourceID){
        this.audioFileResourceID = audioFileResourceID;
    }

    public int getAudioFileResourceID() {
        return audioFileResourceID;
    }

    public void setIsAudioPlayed(boolean isAudioPlayed){
        this.isAudioPlayed = isAudioPlayed;
    }

    public boolean getIsAudioPlayed() {
        return this.isAudioPlayed;
    }
}
