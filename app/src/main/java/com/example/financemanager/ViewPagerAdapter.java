package com.example.financemanager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ExpenseFragment(); // Return ExpenseFragment for the first tab
            case 1:
                return new IncomeFragment();  // Return IncomeFragment for the second tab
            default:
                return new ExpenseFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Expense and Income
    }
}
