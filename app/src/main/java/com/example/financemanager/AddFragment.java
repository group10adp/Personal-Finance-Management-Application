package com.example.financemanager;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AddFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        getActivity().getWindow().setStatusBarColor(Color.parseColor("#121212"));

//        ImageView backArrow = view.findViewById(R.id.back_arrow);
//        backArrow.setOnClickListener(v -> {
//            setupOnBackPressed();
//        });


        // Initialize TabLayout and ViewPager2
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        // Set up the ViewPager with the TabLayout
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach TabLayout with ViewPager2 and set tab names dynamically
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Expense"); // First tab
                            break;
                        case 1:
                            tab.setText("Income"); // Second tab
                            break;
                        default:
                            tab.setText("Tab " + (position + 1)); // For additional tabs, if needed
                    }
                }
        ).attach();

        return view;
    }

    private void setupOnBackPressed(){
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(isEnabled()){
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }


}
