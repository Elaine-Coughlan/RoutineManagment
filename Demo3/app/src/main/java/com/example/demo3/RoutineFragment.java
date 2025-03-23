package com.example.demo3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.demo3.notifications.TaskReminderWorker;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RoutineFragment extends Fragment {

    private ArrayList<String> taskList = new ArrayList<>();
    private TextView taskDisplay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routine, container, false);

        EditText taskInput = view.findViewById(R.id.etTaskInput);

        Button setReminder = view.findViewById(R.id.btnSetReminder);
        Button addTaskButton = view.findViewById(R.id.btnAddTask);

        taskDisplay = view.findViewById(R.id.tvTaskList);


        setReminder.setOnClickListener(v -> {
            scheduleTaskReminder();
        });

        addTaskButton.setOnClickListener(v -> {
            String task = taskInput.getText().toString().trim();
            if (!task.isEmpty()) {
                taskList.add(task);
                updateTaskList();
                taskInput.setText("");
            }
        });

        return view;
    }

    private void scheduleTaskReminder() {
        OneTimeWorkRequest reminderRequest = new OneTimeWorkRequest.Builder(TaskReminderWorker.class)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(requireContext()).enqueue(reminderRequest);


    }

    private void updateTaskList() {
        StringBuilder tasks = new StringBuilder();
        for (int i = 0; i < taskList.size(); i++) {
            tasks.append(i + 1).append(". ").append(taskList.get(i)).append("\n");
        }
        taskDisplay.setText(tasks.toString());
    }
}
