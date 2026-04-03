package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.R;
import com.eventlottery.databinding.ActivitySearchUsersBinding;

/**
 * Activity hosting the SearchUsersFragment and its search bar.
 * Part of the 'View' in MVC, acts as a container for Fragments.
 */
public class SearchUsersActivity extends AppCompatActivity {

    private ActivitySearchUsersBinding binding;
    private SearchUsersFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (savedInstanceState == null) {
            fragment = new SearchUsersFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else {
            fragment = (SearchUsersFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }

        setupSearch();
    }

    /**
     * Sets up the search bar and its functionality.
     */
    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (fragment != null) {
                    fragment.filterUsers(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
