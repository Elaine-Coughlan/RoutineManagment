package com.elaine.tasksandroutine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elaine.tasksandroutine.data.Routine

class TasksAdapter(private val routine: Routine) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val taskNameTextView: TextView = itemView.findViewById(R.id.taskNameTextView)
        val taskCompletedCheckBox: CheckBox = itemView.findViewById(R.id.taskCompletedCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = routine.tasks[position]
        holder.taskNameTextView.text = task.name
        holder.taskCompletedCheckBox.isChecked = task.completed

        holder.taskCompletedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            task.completed = isChecked
            // You might want to save the updated routine here
        }
    }

    override fun getItemCount(): Int {
        return routine.tasks.size
    }



}