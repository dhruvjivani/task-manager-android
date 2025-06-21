package com.conestoga.taskmanager.activities

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.conestoga.taskmanager.R
import com.conestoga.taskmanager.database.TaskDatabaseHelper
import com.conestoga.taskmanager.databinding.ActivityEditTaskBinding
import com.conestoga.taskmanager.models.Task

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding
    private lateinit var dbHelper: TaskDatabaseHelper
    private var currentTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout via ViewBinding
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database helper
        dbHelper = TaskDatabaseHelper(this)

        // Get Task ID passed from intent extras
        val taskId = intent.getIntExtra("TASK_ID", -1)
        Log.d("EditTaskActivity", "Received TASK_ID: $taskId")
        if (taskId == -1) {
            Toast.makeText(this, getString(R.string.toast_invalid_task_id), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Retrieve task from database
        currentTask = dbHelper.getTaskById(taskId)
        if (currentTask == null) {
            Toast.makeText(this, getString(R.string.toast_task_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Populate UI fields with current task data
        binding.editTextTaskName.setText(currentTask?.name)
        binding.editTextDueDate.setText(currentTask?.dueDate)

        // Setup priority spinner with options
        val priorities = listOf(getString(R.string.priority_low), getString(R.string.priority_medium), getString(R.string.priority_high))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        binding.spinnerPriority.adapter = adapter

        // Set spinner selection to current priority
        val priorityPosition = priorities.indexOf(currentTask?.priority)
        if (priorityPosition >= 0) {
            binding.spinnerPriority.setSelection(priorityPosition)
        }

        // Setup update button click listener
        binding.buttonUpdate.setOnClickListener {
            updateTask()
        }
    }

    /**
     * Validate input and update the task in database.
     */
    private fun updateTask() {
        val name = binding.editTextTaskName.text.toString().trim()
        val dueDate = binding.editTextDueDate.text.toString().trim()
        val priority = binding.spinnerPriority.selectedItem.toString()

        // Check required fields
        if (name.isEmpty() || dueDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        // Create updated task object
        val updatedTask = currentTask?.copy(name = name, dueDate = dueDate, priority = priority)
        if (updatedTask != null) {
            // Update task in DB and notify user
            val success = dbHelper.updateTask(updatedTask)
            if (success) {
                Toast.makeText(this, getString(R.string.toast_task_updated), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
