package com.example.demo3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo3.model.Badge;

import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);

        GridView gridView = view.findViewById(R.id.gridViewAchievements);

        // Create sample badge data
        List<Badge> badges = new ArrayList<>();
        badges.add(new Badge(R.drawable.ic_badge_week, "1 Week Streak", "Awarded for completing routines for 1 week straight."));
        badges.add(new Badge(R.drawable.ic_badge_tasks, "5 Tasks Completed", "Awarded for completing 5 tasks."));
        badges.add(new Badge(R.drawable.ic_badge_first, "First Routine", "Awarded for creating your first routine."));
        badges.add(new Badge(R.drawable.ic_badge_milestone, "10 Tasks Completed", "Awarded for completing 10 tasks."));

        // Set adapter
        AchievementAdapter adapter = new AchievementAdapter(requireContext(), badges);
        gridView.setAdapter(adapter);

        return view;
    }
}

