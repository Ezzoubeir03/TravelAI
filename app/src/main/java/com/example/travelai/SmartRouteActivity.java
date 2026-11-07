package com.example.travelai;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SmartRouteActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE = "place";
    private static final int TRAVEL_MIN = 20;

    private String place = "Bali";
    private List<RouteStop> stops = new ArrayList<>();
    private int startMinutes = 9 * 60;
    private boolean mapExpanded = true;

    private LinearLayout container;
    private RouteMapView map;
    private TextView tvTitle, tvCount, tvDuration, tvEnd;
    private LinearProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_route);

        container = findViewById(R.id.route_container);
        map = findViewById(R.id.route_map);
        tvTitle = findViewById(R.id.tv_title);
        tvCount = findViewById(R.id.tv_count);
        tvDuration = findViewById(R.id.tv_duration);
        tvEnd = findViewById(R.id.tv_end);
        progressBar = findViewById(R.id.progress);

        String p = getIntent().getStringExtra(EXTRA_PLACE);
        if (p != null) place = p;

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_more).setOnClickListener(this::showPlaceMenu);
        findViewById(R.id.btn_sort).setOnClickListener(this::showSortMenu);
        findViewById(R.id.btn_view).setOnClickListener(v -> toggleMap());
        findViewById(R.id.btn_see_more).setOnClickListener(v -> showMore());

        loadPlace();
    }

    private void loadPlace() {
        stops = DataProvider.getRouteStops(place);
        tvTitle.setText(place);
        render(true);
    }

    private void render(boolean animate) {
        container.removeAllViews();
        LayoutInflater inf = getLayoutInflater();

        // Start card
        View start = inf.inflate(R.layout.item_route_start, container, false);
        String date = new SimpleDateFormat("d MMMM yyyy", Locale.US).format(new Date());
        ((TextView) start.findViewById(R.id.tv_start_time))
                .setText(formatTime(startMinutes) + ", " + date);
        start.findViewById(R.id.btn_edit).setOnClickListener(v -> pickStartTime());
        container.addView(start);

        int t = startMinutes;
        boolean[] done = new boolean[stops.size()];
        int doneCount = 0;

        for (int i = 0; i < stops.size(); i++) {
            RouteStop s = stops.get(i);
            t += TRAVEL_MIN;
            int from = t;
            t += s.durationMin;

            View row = inf.inflate(R.layout.item_route_stop, container, false);
            ((TextView) row.findViewById(R.id.tv_name)).setText(s.name);
            ((TextView) row.findViewById(R.id.tv_time))
                    .setText(formatTime(from) + " - " + formatTime(t));

            ImageView photo = row.findViewById(R.id.iv_photo);
            ImageView icon = row.findViewById(R.id.iv_icon);
            if (s.imageRes != 0) {
                photo.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
                Glide.with(this).load(s.imageRes).centerCrop().into(photo);
            } else {
                photo.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
            }

            ImageView check = row.findViewById(R.id.iv_check);
            styleCheck(check, s.done);
            check.setOnClickListener(v -> {
                s.done = !s.done;
                render(false);
            });

            done[i] = s.done;
            if (s.done) doneCount++;
            container.addView(row);
        }

        int total = t - startMinutes;
        tvCount.setText(String.valueOf(stops.size()));
        tvDuration.setText(formatDuration(total));
        tvEnd.setText(formatTime(t));
        progressBar.setProgressCompat(
                stops.isEmpty() ? 0 : doneCount * 100 / stops.size(), true);

        map.setStops(stops.size(), done);
        if (animate) map.play();
    }

    private void styleCheck(ImageView v, boolean done) {
        v.setBackgroundResource(done ? R.drawable.bg_circle_green : R.drawable.bg_check_todo);
        v.setColorFilter(done ? Color.WHITE : Color.parseColor("#C5CCC6"));
    }

    private String formatTime(int minutes) {
        return String.format(Locale.US, "%02d:%02d", (minutes / 60) % 24, minutes % 60);
    }

    private String formatDuration(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return (h > 0 ? h + " hrs " : "") + m + " mins";
    }

    private void pickStartTime() {
        new TimePickerDialog(this, (view, h, m) -> {
            startMinutes = h * 60 + m;
            render(false);
        }, startMinutes / 60, startMinutes % 60, true).show();
    }

    private void showSortMenu(View anchor) {
        PopupMenu pm = new PopupMenu(this, anchor);
        pm.getMenu().add(0, 0, 0, "Optimized order");
        pm.getMenu().add(0, 1, 1, "Shortest stay first");
        pm.getMenu().add(0, 2, 2, "Longest stay first");
        pm.getMenu().add(0, 3, 3, "Name A-Z");
        pm.setOnMenuItemClickListener(item -> {
            Comparator<RouteStop> c;
            switch (item.getItemId()) {
                case 1:
                    c = (a, b) -> Integer.compare(a.durationMin, b.durationMin);
                    break;
                case 2:
                    c = (a, b) -> Integer.compare(b.durationMin, a.durationMin);
                    break;
                case 3:
                    c = (a, b) -> a.name.compareToIgnoreCase(b.name);
                    break;
                default:
                    c = (a, b) -> Integer.compare(a.order, b.order);
            }
            Collections.sort(stops, c);
            render(true);
            return true;
        });
        pm.show();
    }

    private void showPlaceMenu(View anchor) {
        PopupMenu pm = new PopupMenu(this, anchor);
        List<Destination> list = DataProvider.getDestinations();
        for (int i = 0; i < list.size(); i++) {
            pm.getMenu().add(0, i, i, list.get(i).getName());
        }
        pm.setOnMenuItemClickListener(item -> {
            place = list.get(item.getItemId()).getName();
            loadPlace();
            return true;
        });
        pm.show();
    }

    private void toggleMap() {
        mapExpanded = !mapExpanded;
        View frame = findViewById(R.id.map_frame);
        ViewGroup.LayoutParams lp = frame.getLayoutParams();
        lp.height = dp(mapExpanded ? 300 : 160);
        frame.setLayoutParams(lp);
    }

    private void showMore() {
        int n = stops.size();
        new AlertDialog.Builder(this)
                .setTitle(place + " route details")
                .setMessage("Stops: " + n
                        + "\nDistance: " + (n * 9) + " km"
                        + "\nEstimated cost: $" + (n * 85)
                        + "\nCO2 saved by optimizing: " + (n * 3) + " kg")
                .setPositiveButton("OK", null)
                .show();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}