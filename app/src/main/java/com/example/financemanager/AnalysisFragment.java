package com.example.financemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AnalysisFragment extends Fragment {

    TextView popUpText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analysis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Display Finance1Fragment by default
        FragmentTransaction defaultTransaction = getChildFragmentManager().beginTransaction();
        defaultTransaction.replace(R.id.fragment_container, new Finance1Fragment());
        defaultTransaction.commit();

        ImageView dropDown = view.findViewById(R.id.dropDown);
        popUpText=view.findViewById(R.id.popUpText);

        dropDown.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.finance_option) {
                    popUpText.setText("Finance");
                    selectedFragment = new Finance1Fragment();
                } else if (item.getItemId() == R.id.income_option) {
                    popUpText.setText("Income");
                    selectedFragment = new Income1Fragment();
                } else if (item.getItemId() == R.id.expense_option) {
                    popUpText.setText("Expense");
                    selectedFragment = new Expense1Fragment();
                }

                if (selectedFragment != null) {
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, selectedFragment);
                    transaction.commit();
                }

                return true;
            });

            popupMenu.show();
        });


    }
}
