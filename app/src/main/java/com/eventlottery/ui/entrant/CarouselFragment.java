package com.eventlottery.ui.entrant;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eventlottery.R;
import com.eventlottery.databinding.CarouselCardBinding;
import com.eventlottery.databinding.FragmentCarouselBinding;
import com.eventlottery.model.Event;
import com.eventlottery.services.Base64EncodeDecode;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * CarouselFragment - Displays a horizontal carousel of events.
 */
public class CarouselFragment extends Fragment {

    private FragmentCarouselBinding binding;
    private CarouselAdapter adapter;
    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public CarouselFragment() {
        // Required empty public constructor
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCarouselBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        adapter = new CarouselAdapter(events, listener);
        binding.carouselRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.carouselRecyclerView.setAdapter(adapter);

        // Add snapping behavior for carousel effect
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.carouselRecyclerView);
    }

    public void setEvents(List<Event> newEvents) {
        this.events = newEvents;
        if (adapter != null) {
            adapter.updateEvents(newEvents);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Inner Adapter Class
    private static class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {
        private List<Event> eventList;
        private final OnEventClickListener listener;

        CarouselAdapter(List<Event> eventList, OnEventClickListener listener) {
            this.eventList = eventList;
            this.listener = listener;
        }

        void updateEvents(List<Event> newEvents) {
            this.eventList = newEvents;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CarouselCardBinding binding = CarouselCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Event event = eventList.get(position);
            holder.bind(event, listener);
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final CarouselCardBinding binding;

            ViewHolder(CarouselCardBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bind(Event event, OnEventClickListener listener) {
                binding.eventNameText.setText(event.getName());
                
                // Set status badge
                binding.statusBadge.setText(getStatusText(event.getStatus()));
                binding.statusBadge.setChipBackgroundColorResource(getStatusColor(event.getStatus()));

                // Set image
                if (event.getPosterImageUrl() != null) {
                    Bitmap bitmap = Base64EncodeDecode.decodeBase64(event.getPosterImageUrl());
                    Glide.with(binding.getRoot().getContext())
                            .load(bitmap)
                            .into(binding.eventPosterImage);
                } else {
                    binding.eventPosterImage.setImageResource(R.drawable.ic_launcher_foreground);
                }

                binding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEventClick(event);
                    }
                });
            }

            private String getStatusText(String status) {
                if (status == null) return "Unknown";
                switch (status) {
                    case "open": return "Open";
                    case "closed": return "Closed";
                    case "lottery_drawn": return "Lottery Drawn";
                    case "completed": return "Completed";
                    default: return status;
                }
            }

            private int getStatusColor(String status) {
                if (status == null) return R.color.status_closed_gray;
                switch (status) {
                    case "open": return R.color.status_open_green;
                    case "closed": return R.color.status_closed_gray;
                    case "lottery_drawn": return R.color.status_waiting_yellow;
                    case "completed": return R.color.status_closed_gray;
                    default: return R.color.status_closed_gray;
                }
            }
        }
    }
}
