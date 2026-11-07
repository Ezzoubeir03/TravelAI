package com.example.travelai;

import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    public static List<Destination> getDestinations() {
        List<Destination> list = new ArrayList<>();
        list.add(new Destination("Bali", "Indonesia", R.drawable.bali,
                "The Island of the Gods is known for green rice terraces, ancient sea temples, surf beaches and a warm, welcoming culture. Perfect for a mix of relaxation and adventure.",
                1240, 7, 4.8f));
        list.add(new Destination("Kyoto", "Japan", R.drawable.kyoto,
                "Japan's old capital is full of historic shrines, bamboo forests, traditional tea houses and beautiful gardens, especially during cherry blossom and autumn seasons.",
                1980, 6, 4.9f));
        list.add(new Destination("Swiss Alps", "Switzerland", R.drawable.swiss,
                "Snow-capped peaks, crystal lakes and scenic train rides. A paradise for hikers, skiers and anyone who loves mountain views.",
                2650, 5, 4.7f));
        return list;
    }

    public static Destination findByName(String query) {
        if (query == null) return null;
        String q = query.trim().toLowerCase();
        if (q.isEmpty()) return null;
        for (Destination d : getDestinations()) {
            String n = d.getName().toLowerCase();
            if (n.equals(q) || n.contains(q) || q.contains(n)) return d;
        }
        return null;
    }

    public static String generatePlan(String place, int days) {
        String[] ideas;
        String p = place.toLowerCase();
        if (p.contains("bali")) {
            ideas = new String[]{"Arrive, hotel check-in and Uluwatu Temple sunset",
                    "Ubud rice terraces and Sacred Monkey Forest",
                    "Nusa Penida island day trip",
                    "Beach day and seafood dinner in Jimbaran",
                    "Tanah Lot temple and local market",
                    "Mount Batur sunrise hike",
                    "Spa morning and free time"};
        } else if (p.contains("kyoto")) {
            ideas = new String[]{"Fushimi Inari shrine and its thousand gates",
                    "Arashiyama bamboo grove and monkey park",
                    "Kinkaku-ji Golden Pavilion and Gion district",
                    "Day trip to Nara and its friendly deer",
                    "Tea ceremony and Nishiki Market food tour",
                    "Kiyomizu-dera temple and old streets"};
        } else if (p.contains("swiss") || p.contains("alps")) {
            ideas = new String[]{"Arrive in Interlaken and lakeside walk",
                    "Scenic train to Jungfraujoch",
                    "Lake Brienz boat ride",
                    "Grindelwald mountain hike",
                    "Lucerne old town and Chapel Bridge",
                    "Zermatt and Matterhorn views"};
        } else {
            ideas = new String[]{"Arrive and explore the city center",
                    "Visit the top landmarks",
                    "Local food tour",
                    "Nature or beach day",
                    "Museums and culture",
                    "Free day and shopping"};
        }

        StringBuilder sb = new StringBuilder();
        sb.append("✨ Your ").append(days).append("-day trip to ").append(place).append("\n\n");
        for (int i = 0; i < days; i++) {
            sb.append("Day ").append(i + 1).append(": ")
                    .append(ideas[i % ideas.length]).append("\n\n");
        }
        sb.append("Route optimized: less travel time, scenic and budget friendly.");
        return sb.toString();
    }
    public static java.util.List<RouteStop> getRouteStops(String place) {
        java.util.List<RouteStop> l = new ArrayList<>();
        String p = place == null ? "" : place.toLowerCase();
        if (p.contains("kyoto")) {
            l.add(new RouteStop("Fushimi Inari Shrine", 75, R.drawable.kyoto, 0));
            l.add(new RouteStop("Arashiyama Bamboo Grove", 60, 0, 1));
            l.add(new RouteStop("Kinkaku-ji Golden Pavilion", 45, 0, 2));
            l.add(new RouteStop("Nishiki Market", 60, 0, 3));
            l.add(new RouteStop("Gion District Walk", 90, 0, 4));
        } else if (p.contains("swiss") || p.contains("alps")) {
            l.add(new RouteStop("Interlaken Lakeside Walk", 60, R.drawable.swiss, 0));
            l.add(new RouteStop("Jungfraujoch Train", 120, 0, 1));
            l.add(new RouteStop("Lake Brienz Cruise", 60, 0, 2));
            l.add(new RouteStop("Grindelwald Hike", 120, 0, 3));
        } else {
            l.add(new RouteStop("Uluwatu Temple", 60, R.drawable.bali, 0));
            l.add(new RouteStop("Tegallalang Rice Terrace", 75, 0, 1));
            l.add(new RouteStop("Sacred Monkey Forest", 60, 0, 2));
            l.add(new RouteStop("Ubud Market", 45, 0, 3));
            l.add(new RouteStop("Tanah Lot Sunset", 90, 0, 4));
        }
        return l;
    }
}