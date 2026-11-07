package com.example.travelai;

public class Destination {
    private final String name;
    private final String country;
    private final int imageRes;
    private final String description;
    private final int cost;
    private final int days;
    private final float rating;

    public Destination(String name, String country, int imageRes,
                       String description, int cost, int days, float rating) {
        this.name = name;
        this.country = country;
        this.imageRes = imageRes;
        this.description = description;
        this.cost = cost;
        this.days = days;
        this.rating = rating;
    }

    public String getName() { return name; }
    public String getCountry() { return country; }
    public int getImageRes() { return imageRes; }
    public String getDescription() { return description; }
    public int getCost() { return cost; }
    public int getDays() { return days; }
    public float getRating() { return rating; }
}