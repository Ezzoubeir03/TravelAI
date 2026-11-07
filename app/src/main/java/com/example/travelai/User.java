package com.example.travelai;

public class User {
    public final long id;
    public String name;
    public final String email;
    public String photoPath;
    public final long created;

    public User(long id, String name, String email, String photoPath, long created) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photoPath = photoPath;
        this.created = created;
    }
}