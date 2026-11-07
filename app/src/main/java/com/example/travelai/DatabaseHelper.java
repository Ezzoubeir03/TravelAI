package com.example.travelai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final long ERR_EMAIL_EXISTS = -2;
    private static final String DB_NAME = "travelai.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "email TEXT NOT NULL UNIQUE, "
                + "salt TEXT NOT NULL, "
                + "hash TEXT NOT NULL, "
                + "photo TEXT, "
                + "created INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    /** Returns the new user id, ERR_EMAIL_EXISTS, or -1 on other failure. */
    public long register(String name, String email, String password) {
        String mail = email.trim().toLowerCase(Locale.ROOT);
        if (emailExists(mail)) return ERR_EMAIL_EXISTS;

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        ContentValues v = new ContentValues();
        v.put("name", name.trim());
        v.put("email", mail);
        v.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP));
        v.put("hash", hash(password, salt));
        v.put("created", System.currentTimeMillis());
        return getWritableDatabase().insert("users", null, v);
    }

    /** Returns the user if email and password match, otherwise null. */
    public User login(String email, String password) {
        String mail = email.trim().toLowerCase(Locale.ROOT);
        try (Cursor c = getReadableDatabase().query("users", null,
                "email=?", new String[]{mail}, null, null, null)) {
            if (!c.moveToFirst()) return null;
            byte[] salt = Base64.decode(
                    c.getString(c.getColumnIndexOrThrow("salt")), Base64.NO_WRAP);
            String stored = c.getString(c.getColumnIndexOrThrow("hash"));
            boolean ok = MessageDigest.isEqual(
                    hash(password, salt).getBytes(), stored.getBytes());
            return ok ? readUser(c) : null;
        }
    }

    public User getUser(long id) {
        try (Cursor c = getReadableDatabase().query("users", null,
                "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readUser(c) : null;
        }
    }

    public void updateName(long id, String name) {
        ContentValues v = new ContentValues();
        v.put("name", name.trim());
        getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void updatePhoto(long id, String path) {
        ContentValues v = new ContentValues();
        v.put("photo", path);
        getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)});
    }

    private boolean emailExists(String email) {
        try (Cursor c = getReadableDatabase().query("users", new String[]{"id"},
                "email=?", new String[]{email}, null, null, null)) {
            return c.moveToFirst();
        }
    }

    private User readUser(Cursor c) {
        return new User(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("email")),
                c.getString(c.getColumnIndexOrThrow("photo")),
                c.getLong(c.getColumnIndexOrThrow("created")));
    }

    private static String hash(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 256);
            byte[] h = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(spec).getEncoded();
            return Base64.encodeToString(h, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}