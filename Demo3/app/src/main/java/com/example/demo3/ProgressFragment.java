package com.example.demo3;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProgressFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView progressPercentage;
    private int progress = 0;
    private final Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        progressBar = view.findViewById(R.id.circularProgressBar);
        progressPercentage = view.findViewById(R.id.tvProgressPercentage);

        simulateProgress();

        return view;
    }

    private void simulateProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progress < 100) {
                    progress += 10; // Increment progress
                    progressBar.setProgress(progress);
                    progressPercentage.setText(progress + "%");
                    handler.postDelayed(this, 500); // Repeat every 500ms
                }
            }
        }, 500);
    }
}

