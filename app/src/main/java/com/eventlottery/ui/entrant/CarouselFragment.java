package com.eventlottery.ui.entrant;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

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

import java.util.ArrayList;
import java.util.Comparator;
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
        setupSpinner();
    }

    private void setupRecyclerView() {
        adapter = new CarouselAdapter(events, listener);
        binding.carouselRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.carouselRecyclerView.setAdapter(adapter);

        // Add snapping behavior for carousel effect
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.carouselRecyclerView);

        scrollToMiddle();
    }

    /**
     * Scrolls the carousel to a middle position to enable infinite swiping in both directions.
     */
    private void scrollToMiddle() {
        if (adapter != null && adapter.getActualItemCount() > 0) {
            int middle = Integer.MAX_VALUE / 2;
            int offset = middle % adapter.getActualItemCount();
            binding.carouselRecyclerView.scrollToPosition(middle - offset);
        }
    }

    /**
     * Sets up the spinner for sorting events.
     */
    private void setupSpinner() {
        String[] options = {"Newest", "Popular", "Closing Soon", "Within 3KM", "Hidden Gems", "Less than $10"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortDropdown.setAdapter(spinnerAdapter);

        binding.sortDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applySort(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Applies the selected sorting option.
     * @param position
     */
    private void applySort(int position) {
        switch (position) {
            case 0: getNewest(); break;
            case 1: getMostFull(); break;
            case 2: getClosestDeadline(); break;
            case 3: getNearest(); break;
            case 4: getMostEmpty(); break;
            case 5: getCheap(); break;
            default: defaultSort(); break;
        }
        scrollToMiddle();
    }

    /**
     * Sets the list of events to display.
     * @param newEvents
     */
    public void setEvents(List<Event> newEvents) {
        this.events = new ArrayList<>(newEvents);
        if (binding != null) {
            applySort(binding.sortDropdown.getSelectedItemPosition());
            scrollToMiddle();
        } else if (adapter != null) {
            adapter.updateEvents(this.events);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Adapter for the carousel.
     */
    private static class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {
        private List<Event> eventList = new ArrayList<>();
        private final OnEventClickListener listener;

        CarouselAdapter(List<Event> eventList, OnEventClickListener listener) {
            updateEvents(eventList);
            this.listener = listener;
        }

        /**
         * Updates the events displayed in the carousel.
         * @param newEvents
         */
        void updateEvents(List<Event> newEvents) {
            if (newEvents == null || newEvents.isEmpty()) {
                this.eventList = new ArrayList<>();
            } else {
                // Ensure we only use the top 5 events
                this.eventList = new ArrayList<>(newEvents.subList(0, Math.min(newEvents.size(), 5)));
            }
            notifyDataSetChanged();
        }

        /**
         * Gets the actual number of items in the carousel.
         * @return
         */
        public int getActualItemCount() {
            return eventList.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CarouselCardBinding binding = CarouselCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (eventList.isEmpty()) return;
            // Use modulo to cycle through events infinitely
            Event event = eventList.get(position % eventList.size());
            holder.bind(event, listener);
        }

        /**
         * Gets the number of items in the carousel.
         * @return int
         */
        @Override
        public int getItemCount() {
            // Return a very large number for infinite swiping
            return eventList.isEmpty() ? 0 : Integer.MAX_VALUE;
        }

        /**
         * ViewHolder Class for the carousel items
         */
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
                return switch (status) {
                    case "open" -> "Open";
                    case "closed" -> "Closed";
                    case "lottery_drawn" -> "Lottery Drawn";
                    case "completed" -> "Completed";
                    default -> status;
                };
            }

            private int getStatusColor(String status) {
                if (status == null) return R.color.status_closed_gray;
                return switch (status) {
                    case "open" -> R.color.status_open_green;
                    case "closed" -> R.color.status_closed_gray;
                    case "lottery_drawn" -> R.color.status_waiting_yellow;
                    case "completed" -> R.color.status_closed_gray;
                    default -> R.color.status_closed_gray;
                };
            }
        }
    }

    /**
     * Default sort for the carousel
     */
    public void defaultSort() {
        getNewest();
    }

    /**
     * Gets five newest events
     */
    public void getNewest() {
        events.sort((e1, e2) -> Long.compare(e2.getCreatedAt(), e1.getCreatedAt()));
        if (adapter != null) adapter.updateEvents(events);
    }

    /**
     * Gets events that close within 3 days
     */
    public void getClosestDeadline() {
        List<Event> closestDeadlineEvents = new ArrayList<>();
        long threeDaysInMs = 3L * 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        for (Event event : events) {
            long deadline = event.getRegistrationCloses();
            if (deadline > now && deadline <= now + threeDaysInMs) {
                closestDeadlineEvents.add(event);
            }
        }
        if (adapter != null) adapter.updateEvents(closestDeadlineEvents);
    }

    /**
     * Gets events within 3KM
     */
    public void getNearest() {
        List<Event> nearestEvents = new ArrayList<>();
        for (Event event : events) {
            Integer radius = event.getGeolocationRadius();
            if (radius != null && radius <= 3) {
                nearestEvents.add(event);
            }
        }
        if (adapter != null) adapter.updateEvents(nearestEvents);
    }

    /**
     * Gets events with less than 5 people signed up
     */
    public void getMostEmpty() {
        List<Event> emptiestEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getWaitlistCount() <= 5) {
                emptiestEvents.add(event);
            }
        }
        if (adapter != null) adapter.updateEvents(emptiestEvents);
    }

    /**
     * Gets events with less than 5 spots available
     */
    public void getMostFull() {
        List<Event> fullestEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getCapacity() - event.getWaitlistCount() <= 5) {
                fullestEvents.add(event);
            }
        }
        if (adapter != null) adapter.updateEvents(fullestEvents);
    }

    /**
     * Gets events {@code <} $10
     */
    public void getCheap() {
        List<Event> cheapEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getPrice() < 10.0) {
                cheapEvents.add(event);
            }
        }
        if (adapter != null) adapter.updateEvents(cheapEvents);
    }
}
