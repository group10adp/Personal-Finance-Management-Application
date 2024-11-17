package com.example.financemanager;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InvestmentsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_investments, container, false);

        // Find the FloatingActionButton and set a click listener
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the new AddInvestment fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                // Optional: Set animations
                transaction.setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                );

                transaction.replace(R.id.fragment_container, new AddInvestment());
                transaction.addToBackStack(null); // Adds the transaction to the back stack
                transaction.commit();
            }
        });

        return view;
    }
}
