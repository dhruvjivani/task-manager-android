package com.conestoga.taskmanager.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.conestoga.taskmanager.R
import com.conestoga.taskmanager.database.TaskDatabaseHelper
import com.conestoga.taskmanager.databinding.ActivityAddTaskBinding
import com.conestoga.taskmanager.models.Task
import java.util.Calendar

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var dbHelper: TaskDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database helper instance
        dbHelper = TaskDatabaseHelper(this)

        // Setup priority spinner with predefined priority options
        val priorities = listOf("Low", "Medium", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        binding.spinnerPriority.adapter = adapter

        // Set click listener on due date input to show date picker dialog
        binding.editTextDueDate.setOnClickListener {
            showDatePicker()
        }

        // Set click listener on save button to save the new task
        binding.buttonSave.setOnClickListener {
            saveTask()
        }
    }

    /**
     * Shows a DatePickerDialog and updates the due date EditText with the selected date.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            // Format the date as day/month/year and set it to EditText
            binding.editTextDueDate.setText("$d/${m + 1}/$y")
        }, year, month, day)

        datePicker.show()
    }

    /**
     * Validate inputs and add the new task to the database.
     * Shows Toast messages for success or validation errors.
     */
    private fun saveTask() {
        val name = binding.editTextTaskName.text.toString().trim()
        val dueDate = binding.editTextDueDate.text.toString().trim()
        val priority = binding.spinnerPriority.selectedItem?.toString() ?: ""

        // Validate all fields are filled
        if (name.isEmpty() || dueDate.isEmpty() || priority.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_all_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        // Create Task object and add it to DB
        val task = Task(name = name, dueDate = dueDate, priority = priority)
        dbHelper.addTask(task)

        // Show success message and close activity
        Toast.makeText(this, getString(R.string.task_added_success), Toast.LENGTH_SHORT).show()
        finish()
    }
}
