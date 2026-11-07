package com.example.travelai;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF = "travelai_session";
    private static final String KEY_USER = "user_id";
    private final SharedPreferences sp;

    public SessionManager(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void login(long userId) {
        sp.edit().putLong(KEY_USER, userId).apply();
    }

    public void logout() {
        sp.edit().remove(KEY_USER).apply();
    }

    public long getUserId() {
        return sp.getLong(KEY_USER, -1);
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }
}