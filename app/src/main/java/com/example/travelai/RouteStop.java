package com.example.travelai;

public class RouteStop {
    public final String name;
    public final int durationMin;
    public final int imageRes;   // 0 = no photo, an icon is shown instead
    public final int order;
    public boolean done = false;

    public RouteStop(String name, int durationMin, int imageRes, int order) {
        this.name = name;
        this.durationMin = durationMin;
        this.imageRes = imageRes;
        this.order = order;
    }
}