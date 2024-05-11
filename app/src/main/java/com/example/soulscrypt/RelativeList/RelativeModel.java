
package com.example.soulscrypt.RelativeList;
public class RelativeModel {

    private String relative_name;
    private String relative_death_date;
    private String relative_section;
    private double latitude;
    private double longitude;

    private int record_id;

    public RelativeModel(String relative_name, String relative_death_date, String relative_section, double latitude, double longitude, int record_id) {
        this.relative_name = relative_name;
        this.relative_death_date = relative_death_date;
        this.relative_section = relative_section;
        this.latitude = latitude;
        this.longitude = longitude;
        this.record_id = record_id;

    }

    public String getRelative_name() {
        return relative_name;
    }

    public void setRelative_name(String relative_name) {
        this.relative_name = relative_name;
    }

    public String getRelative_death_date() {
        return relative_death_date;
    }

    public void setRelative_death_date(String relative_death_date) {
        this.relative_death_date = relative_death_date;
    }

    public String getRelative_section() {
        return relative_section;
    }

    public void setRelative_section(String relative_section) {
        this.relative_section = relative_section;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRecord_id() {
        return record_id;
    }

    public void setRecord_id(int record_id) {
        this.record_id = record_id;
    }
}
