package com.conestoga.taskmanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.conestoga.taskmanager.databinding.ItemTaskBinding
import com.conestoga.taskmanager.models.Task

/**
 * RecyclerView Adapter to display a list of tasks.
 * Provides callbacks for Edit and Delete buttons in each item.
 */
class TaskAdapter(
    private val tasks: List<Task>,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    /**
     * ViewHolder class that holds references to item views.
     */
    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind data from a Task object to the item views,
         * and setup button click listeners.
         */
        fun bind(task: Task) {
            binding.textViewTaskName.text = task.name
            binding.textViewDueDate.text = task.dueDate
            binding.textViewPriority.text = task.priority

            // Edit button click triggers onEditClick callback with current task
            binding.buttonEdit.setOnClickListener {
                onEditClick(task)
            }

            // Delete button click triggers onDeleteClick callback with current task
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(task)
            }
        }
    }

    /**
     * Inflates the item layout and creates ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    /**
     * Binds the data to the ViewHolder at the given position.
     */
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    /**
     * Returns the total number of items.
     */
    override fun getItemCount(): Int = tasks.size
}
