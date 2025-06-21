package com.conestoga.taskmanager.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.conestoga.taskmanager.R
import com.conestoga.taskmanager.database.TaskDatabaseHelper
import com.conestoga.taskmanager.databinding.ActivityTaskDetailBinding
import com.conestoga.taskmanager.models.Task

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var dbHelper: TaskDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout using ViewBinding
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = TaskDatabaseHelper(this)

        // Get task ID passed via intent
        val taskId = intent.getIntExtra("TASK_ID", -1)
        if (taskId == -1) {
            Toast.makeText(this, getString(R.string.toast_invalid_task), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Retrieve the task from database
        val task: Task? = dbHelper.getTaskById(taskId)
        if (task != null) {
            // Populate UI with task details
            binding.textViewTaskName.text = task.name
            binding.textViewDueDate.text = getString(R.string.label_due_date_colon) + " ${task.dueDate}"
            binding.textViewPriority.text = getString(R.string.label_priority_colon) + " ${task.priority}"

            // Delete button listener - deletes task and closes activity
            binding.buttonDelete.setOnClickListener {
                dbHelper.deleteTask(task.id)
                Toast.makeText(this, getString(R.string.toast_task_deleted), Toast.LENGTH_SHORT).show()
                finish() // return to previous screen (TaskList)
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_task_not_found), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
