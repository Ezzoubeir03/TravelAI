package com.example.travelai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class DestinationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_detail);

        String name = getIntent().getStringExtra(EXTRA_NAME);
        Destination d = DataProvider.findByName(name);
        if (d == null) {
            finish();
            return;
        }

        Glide.with(this).load(d.getImageRes()).centerCrop()
                .into((ImageView) findViewById(R.id.iv_hero));
        ((TextView) findViewById(R.id.tv_name)).setText(d.getName());
        ((TextView) findViewById(R.id.tv_country)).setText(d.getCountry());
        ((TextView) findViewById(R.id.tv_description)).setText(d.getDescription());
        ((TextView) findViewById(R.id.tv_cost)).setText(
                String.format(Locale.US, "From $%,d", d.getCost()));
        ((TextView) findViewById(R.id.tv_days)).setText(d.getDays() + " days");
        ((TextView) findViewById(R.id.tv_rating)).setText("★ " + d.getRating());

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_plan).setOnClickListener(v -> {
            Intent i = new Intent(this, AIItineraryActivity.class);
            i.putExtra(AIItineraryActivity.EXTRA_PLACE, d.getName());
            startActivity(i);
        });
    }
}