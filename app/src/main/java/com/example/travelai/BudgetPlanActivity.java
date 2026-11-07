package com.example.travelai;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class BudgetPlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_plan);

        EditText etDays = findViewById(R.id.et_days);
        EditText etTravelers = findViewById(R.id.et_travelers);
        EditText etFlight = findViewById(R.id.et_flight);
        EditText etHotel = findViewById(R.id.et_hotel);
        EditText etFood = findViewById(R.id.et_food);
        TextView tvResult = findViewById(R.id.tv_result);
        View cardResult = findViewById(R.id.card_result);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_calculate).setOnClickListener(v -> {
            double days = Math.max(num(etDays), 1);
            double travelers = Math.max(num(etTravelers), 1);
            double flights = num(etFlight) * travelers;
            double hotel = num(etHotel) * days;
            double food = num(etFood) * days * travelers;
            double total = flights + hotel + food;

            tvResult.setText(String.format(Locale.US,
                    "Flights: $%,.0f\nHotel: $%,.0f\nFood & activities: $%,.0f\n\nTotal: $%,.0f\nPer person: $%,.0f",
                    flights, hotel, food, total, total / travelers));
            cardResult.setVisibility(View.VISIBLE);
        });
    }

    private double num(EditText e) {
        try {
            return Double.parseDouble(e.getText().toString().trim());
        } catch (Exception ex) {
            return 0;
        }
    }
}