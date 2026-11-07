package com.example.travelai;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;

public class HomeFragment extends Fragment {

    private EditText etSearch;

    private final ActivityResultLauncher<Intent> voiceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> r = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (r != null && !r.isEmpty() && etSearch != null) {
                        etSearch.setText(r.get(0));
                        handleSearch(r.get(0));
                    }
                }
            });

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Destinations carousel
        RecyclerView rv = view.findViewById(R.id.rv_destinations);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(new DestinationAdapter(DataProvider.getDestinations()));

        // Search
        etSearch = view.findViewById(R.id.et_search);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handleSearch(v.getText().toString());
                return true;
            }
            return false;
        });

        // Voice
        view.findViewById(R.id.btn_mic).setOnClickListener(v -> startVoice());

        // See all
        view.findViewById(R.id.tv_see_all).setOnClickListener(v -> showAllDialog());

        // Quick actions
        view.findViewById(R.id.action_ai).setOnClickListener(v -> openAI(null));
        view.findViewById(R.id.banner_ai).setOnClickListener(v -> openAI(null));
        view.findViewById(R.id.action_route).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SmartRouteActivity.class)));
        view.findViewById(R.id.action_budget).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BudgetPlanActivity.class)));
        view.findViewById(R.id.action_guide).setOnClickListener(v -> showGuide());

        // Top bar
        view.findViewById(R.id.btn_menu).setOnClickListener(this::showMenu);
        view.findViewById(R.id.btn_profile).setOnClickListener(v ->
                ((MainActivity) requireActivity()).selectTab(R.id.nav_profile));

        // Top bar
        view.findViewById(R.id.btn_menu).setOnClickListener(this::showMenu);
        view.findViewById(R.id.btn_profile).setOnClickListener(v ->
                ((MainActivity) requireActivity()).selectTab(R.id.nav_profile));

        // Show the profile photo in the top-right avatar
        ImageView avatar = view.findViewById(R.id.btn_profile);
        User u = new DatabaseHelper(requireContext())
                .getUser(new SessionManager(requireContext()).getUserId());
        if (u != null && u.photoPath != null && new File(u.photoPath).exists()) {
            avatar.setImageTintList(null);
            avatar.setPadding(0, 0, 0, 0);
            avatar.setClipToOutline(true);
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(new File(u.photoPath)).centerCrop().into(avatar);
        }
    }   // <-- this is the closing brace of onViewCreated
    }

    private void handleSearch(String text) {
        String q = text.trim();
        if (q.isEmpty()) {
            Toast.makeText(requireContext(), "Type a destination first", Toast.LENGTH_SHORT).show();
            return;
        }
        Destination d = DataProvider.findByName(q);
        if (d != null) {
            Intent i = new Intent(requireContext(), DestinationDetailActivity.class);
            i.putExtra(DestinationDetailActivity.EXTRA_NAME, d.getName());
            startActivity(i);
        } else {
            openAI(q);
        }
    }

    private void openAI(@Nullable String place) {
        Intent i = new Intent(requireContext(), AIItineraryActivity.class);
        if (place != null) i.putExtra(AIItineraryActivity.EXTRA_PLACE, place);
        startActivity(i);
    }

    private void startVoice() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Where do you want to go?");
        try {
            voiceLauncher.launch(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(),
                    "Voice search is not available on this device", Toast.LENGTH_LONG).show();
        }
    }

    private void showAllDialog() {
        List<Destination> list = DataProvider.getDestinations();
        String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            names[i] = list.get(i).getName() + ", " + list.get(i).getCountry();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("All destinations")
                .setItems(names, (dialog, which) -> {
                    Intent i = new Intent(requireContext(), DestinationDetailActivity.class);
                    i.putExtra(DestinationDetailActivity.EXTRA_NAME, list.get(which).getName());
                    startActivity(i);
                })
                .show();
    }

    private void showGuide() {
        String[] titles = {"Best time to visit", "Packing tips", "Local etiquette", "Staying safe"};
        String[] texts = {
                "Check the dry season of your destination. Book flights and hotels 2 to 3 months ahead for better prices.",
                "Pack light layers, a power adapter, a reusable water bottle, copies of your documents and a small first-aid kit.",
                "Learn a few local greetings, dress respectfully at temples and religious sites, and ask before taking photos of people.",
                "Keep digital copies of your passport, buy travel insurance and save local emergency numbers before you arrive."
        };
        new AlertDialog.Builder(requireContext())
                .setTitle("Travel Guide")
                .setItems(titles, (dialog, which) ->
                        new AlertDialog.Builder(requireContext())
                                .setTitle(titles[which])
                                .setMessage(texts[which])
                                .setPositiveButton("OK", null)
                                .show())
                .show();
    }

    private void showMenu(View anchor) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenu().add(0, 1, 0, "My Itinerary");
        pm.getMenu().add(0, 2, 1, "Bookings");
        pm.getMenu().add(0, 3, 2, "Profile");
        pm.getMenu().add(0, 4, 3, "About TravelAI");
        pm.setOnMenuItemClickListener(item -> {
            MainActivity main = (MainActivity) requireActivity();
            switch (item.getItemId()) {
                case 1: main.selectTab(R.id.nav_itinerary); break;
                case 2: main.selectTab(R.id.nav_bookings); break;
                case 3: main.selectTab(R.id.nav_profile); break;
                case 4:
                    new AlertDialog.Builder(requireContext())
                            .setTitle("TravelAI")
                            .setMessage("AI-powered travel planning made effortless.\nVersion 1.0")
                            .setPositiveButton("OK", null)
                            .show();
                    break;
            }
            return true;
        });
        pm.show();
    }
}