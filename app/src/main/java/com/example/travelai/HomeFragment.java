package com.example.travelai;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Destinations carousel
        List<Destination> list = new ArrayList<>();
        list.add(new Destination("Bali", "Indonesia", R.drawable.bali));
        list.add(new Destination("Kyoto", "Japan", R.drawable.kyoto));
        list.add(new Destination("Swiss Alps", "Switzerland", R.drawable.swiss));

        RecyclerView rv = view.findViewById(R.id.rv_destinations);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(new DestinationAdapter(list));

        // Quick actions (temporary messages, we'll connect real screens later)
        setToast(view, R.id.action_ai, "AI Itinerary");
        setToast(view, R.id.action_route, "Smart Route");
        setToast(view, R.id.action_budget, "Budget Plan");
        setToast(view, R.id.action_guide, "Travel Guide");
        setToast(view, R.id.banner_ai, "AI trip planner coming soon");
        setToast(view, R.id.btn_mic, "Voice search coming soon");
        setToast(view, R.id.tv_see_all, "All destinations");
    }

    private void setToast(View root, int id, String message) {
        root.findViewById(id).setOnClickListener(v ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }
}