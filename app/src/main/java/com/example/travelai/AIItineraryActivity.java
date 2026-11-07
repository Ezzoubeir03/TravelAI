package com.example.travelai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AIItineraryActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE = "place";

    private int days = 5;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_itinerary);

        EditText etPlace = findViewById(R.id.et_place);
        TextView tvDays = findViewById(R.id.tv_days);
        TextView tvResult = findViewById(R.id.tv_result);
        TextView btnGenerate = findViewById(R.id.btn_generate);
        View cardResult = findViewById(R.id.card_result);

        String place = getIntent().getStringExtra(EXTRA_PLACE);
        if (place != null) etPlace.setText(place);
        tvDays.setText(days + " days");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_minus).setOnClickListener(v -> {
            if (days > 1) days--;
            tvDays.setText(days + (days == 1 ? " day" : " days"));
        });
        findViewById(R.id.btn_plus).setOnClickListener(v -> {
            if (days < 14) days++;
            tvDays.setText(days + " days");
        });

        btnGenerate.setOnClickListener(v -> {
            String p = etPlace.getText().toString().trim();
            if (p.isEmpty()) {
                etPlace.setError("Enter a destination");
                return;
            }
            cardResult.setVisibility(View.VISIBLE);
            tvResult.setText("✨ AI is crafting your trip…");
            btnGenerate.setEnabled(false);
            handler.postDelayed(() -> {
                tvResult.setText(DataProvider.generatePlan(p, days));
                btnGenerate.setEnabled(true);
            }, 1500);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}