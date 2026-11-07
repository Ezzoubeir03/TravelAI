package com.example.travelai;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private User user;

    private ImageView ivPhoto;
    private TextView tvInitial, tvName, tvEmail, tvSince;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) savePhoto(uri);
            });

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());
        user = db.getUser(session.getUserId());
        if (user == null) {
            forceLogout();
            return;
        }

        ivPhoto = view.findViewById(R.id.iv_photo);
        tvInitial = view.findViewById(R.id.tv_initial);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvSince = view.findViewById(R.id.tv_since);

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> imagePicker.launch("image/*"));
        ivPhoto.setOnClickListener(v -> imagePicker.launch("image/*"));
        tvInitial.setOnClickListener(v -> imagePicker.launch("image/*"));
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> confirmLogout());

        LinearLayout rows = view.findViewById(R.id.profile_rows);
        addRow(rows, R.drawable.ic_edit, "Edit name", this::showEditName, true);
        addRow(rows, R.drawable.ic_image, "Change profile photo",
                () -> imagePicker.launch("image/*"), true);
        addRow(rows, R.drawable.ic_delete, "Remove profile photo", this::removePhoto, true);
        addRow(rows, R.drawable.ic_info, "About TravelAI", this::showAbout, false);

        bind();
    }

    private void bind() {
        tvName.setText(user.name);
        tvEmail.setText(user.email);
        tvInitial.setText(user.name.isEmpty() ? "?" : user.name.substring(0, 1).toUpperCase());
        tvSince.setText("Member since "
                + new SimpleDateFormat("MMMM yyyy", Locale.US).format(new Date(user.created)));

        boolean hasPhoto = user.photoPath != null && new File(user.photoPath).exists();
        if (hasPhoto) {
            ivPhoto.setVisibility(View.VISIBLE);
            tvInitial.setVisibility(View.INVISIBLE);
            Glide.with(this).load(new File(user.photoPath)).centerCrop().into(ivPhoto);
        } else {
            ivPhoto.setVisibility(View.GONE);
            tvInitial.setVisibility(View.VISIBLE);
        }
    }

    private void savePhoto(Uri uri) {
        final Context app = requireContext().getApplicationContext();
        final long userId = user.id;
        final String oldPath = user.photoPath;

        new Thread(() -> {
            File out = new File(app.getFilesDir(),
                    "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg");
            boolean success = false;
            try {
                Bitmap bmp = Glide.with(app).asBitmap().load(uri)
                        .override(600, 600).centerCrop().submit().get();
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 88, fos);
                }
                success = true;
            } catch (Exception ignored) {
            }

            final boolean ok = success;
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                if (ok) {
                    db.updatePhoto(userId, out.getAbsolutePath());
                    user.photoPath = out.getAbsolutePath();
                    if (oldPath != null) new File(oldPath).delete();
                    bind();
                    Toast.makeText(requireContext(), "Profile photo updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Could not load that image", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void removePhoto() {
        if (user.photoPath == null) {
            Toast.makeText(requireContext(), "You have no profile photo", Toast.LENGTH_SHORT).show();
            return;
        }
        new File(user.photoPath).delete();
        db.updatePhoto(user.id, null);
        user.photoPath = null;
        bind();
        Toast.makeText(requireContext(), "Photo removed", Toast.LENGTH_SHORT).show();
    }

    private void showEditName() {
        EditText input = new EditText(requireContext());
        input.setText(user.name);
        input.setSingleLine();
        input.setSelection(input.getText().length());

        FrameLayout box = new FrameLayout(requireContext());
        int pad = dp(24);
        box.setPadding(pad, dp(8), pad, 0);
        box.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit name")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    String n = input.getText().toString().trim();
                    if (n.length() < 2) {
                        Toast.makeText(requireContext(), "Name is too short", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.updateName(user.id, n);
                    user.name = n;
                    bind();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAbout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("TravelAI")
                .setMessage("AI-powered travel planning made effortless.\nVersion 1.0")
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", (d, w) -> forceLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void forceLogout() {
        session.logout();
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void addRow(LinearLayout parent, int icon, String title,
                        Runnable action, boolean divider) {
        View row = getLayoutInflater().inflate(R.layout.item_profile_row, parent, false);
        ((ImageView) row.findViewById(R.id.iv_row_icon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.tv_row_title)).setText(title);
        row.setOnClickListener(v -> action.run());
        parent.addView(row);

        if (divider) {
            View line = new View(requireContext());
            line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider));
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
            lp.setMarginStart(dp(18));
            lp.setMarginEnd(dp(18));
            parent.addView(line, lp);
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}