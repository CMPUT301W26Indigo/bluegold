package com.eventlottery.services;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Class for selecting an image from the device's gallery.
 * Coded by Google Gemini, Prompt: "How would i separate off the image picker
 * into it own class"
 */
public class ImagePicker {

    /**
     * Interface for handling the selected image.
     */
    public interface ImagePickerListener {
        void imagePicked(Uri uri);
    }

    private final ActivityResultLauncher<String> launcher;

    /**
     * Constructor for ImagePicker to start up.
     * @param activity The activity to register the launcher with.
     * @param listener The listener to handle the selected image.
     */
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

    /**
     * Launches the image picker to select an image.
     */
    public void pickImage() {
        launcher.launch("image/*");
    }
}
