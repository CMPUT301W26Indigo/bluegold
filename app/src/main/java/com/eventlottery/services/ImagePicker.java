package com.eventlottery.services;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ImagePicker {
    public interface ImagePickerListener {
        void imagePicked(Uri uri);
    }

    private final ActivityResultLauncher<String> launcher;

    public ImagePicker(AppCompatActivity activity, ImagePickerListener listener) {
        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        listener.imagePicked(uri);
                    }
                }
        );
    }
    public void pickImage() {
        launcher.launch("image/*");
    }
}
