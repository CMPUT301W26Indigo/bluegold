package com.eventlottery.ui.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eventlottery.R;
import com.eventlottery.model.Image;
import com.eventlottery.services.Base64EncodeDecode;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a grid of images for admin review.
 * Handles both standard URLs and Base64 encoded image strings.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private static final String TAG = "ImageAdapter";
    private List<Image> images;
    private final OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(Image image);
    }

    public ImageAdapter(OnImageClickListener listener) {
        this.images = new ArrayList<>();
        this.listener = listener;
    }

    public void setImages(List<Image> newImages) {
        this.images = new ArrayList<>(newImages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_card, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Image image = images.get(position);
        holder.bind(image, listener);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivReviewImage);
        }

        public void bind(final Image image, final OnImageClickListener listener) {
            String urlOrBase64 = image.getUrl();
            
            if (urlOrBase64 == null || urlOrBase64.isEmpty()) {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            } else if (urlOrBase64.startsWith("http") || urlOrBase64.startsWith("content://")) {
                // Normal URL or Uri
                Glide.with(itemView.getContext())
                        .load(urlOrBase64)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(imageView);
            } else {
                // Assume Base64 string
                try {
                    Bitmap bitmap = Base64EncodeDecode.decodeBase64(urlOrBase64);
                    if (bitmap != null) {
                        Glide.with(itemView.getContext())
                                .load(bitmap)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .centerCrop()
                                .into(imageView);
                    } else {
                        Log.w(TAG, "Failed to decode Base64 image for event: " + image.getEventId());
                        imageView.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading Base64 image", e);
                    imageView.setImageResource(R.drawable.ic_launcher_foreground);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(image);
                }
            });
        }
    }
}
