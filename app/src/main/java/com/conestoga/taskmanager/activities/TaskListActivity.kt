package com.conestoga.taskmanager.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.conestoga.taskmanager.adapters.TaskAdapter
import com.conestoga.taskmanager.database.TaskDatabaseHelper
import com.conestoga.taskmanager.databinding.ActivityTaskListBinding
import com.conestoga.taskmanager.models.Task
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.conestoga.taskmanager.R
import org.json.JSONArray

class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var adapter: TaskAdapter
    private var taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate view using ViewBinding
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database helper
        dbHelper = TaskDatabaseHelper(this)

        // Initialize adapter with callbacks for Edit and Delete actions
        adapter = TaskAdapter(taskList,
            onEditClick = { task ->
                // Open EditTaskActivity with task ID passed
                val intent = Intent(this, EditTaskActivity::class.java)
                intent.putExtra("TASK_ID", task.id)
                startActivity(intent)
            },
            onDeleteClick = { task ->
                // Delete task from DB and reload list
                dbHelper.deleteTask(task.id)
                loadTasks()
                Toast.makeText(this, getString(R.string.toast_task_deleted), Toast.LENGTH_SHORT).show()
            }
        )

        // Setup RecyclerView with LinearLayoutManager and the adapter
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = adapter

        // Floating Action Button to add new task
        binding.fabAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        // Enable swipe to delete on RecyclerView items
        setupSwipeToDelete()

        // Fetch a motivational quote from API and show it
        fetchMotivationalQuote()
    }

    override fun onResume() {
        super.onResume()
        // Reload tasks every time activity resumes
        loadTasks()
    }

    /**
     * Loads all tasks from the database, sorts them according to preference,
     * and updates the adapter.
     */
    private fun loadTasks() {
        taskList.clear()
        val all = dbHelper.getAllTasks()
        val sorted = when (getSortPreference()) {
            "priority" -> all.sortedBy { it.priority }
            else -> all.sortedBy { it.dueDate }
        }
        taskList.addAll(sorted)
        adapter.notifyDataSetChanged()
    }

    /**
     * Configures swipe gestures on RecyclerView items to delete tasks.
     */
    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val task = taskList[pos]
                dbHelper.deleteTask(task.id)
                taskList.removeAt(pos)
                adapter.notifyItemRemoved(pos)
                Toast.makeText(this@TaskListActivity, getString(R.string.toast_task_deleted), Toast.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerViewTasks)
    }

    /**
     * Retrieves the user's preferred sort option from SharedPreferences.
     */
    private fun getSortPreference(): String {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        return prefs.getString("sort_by", "date") ?: "date"
    }

    /**
     * Saves the user's preferred sort option to SharedPreferences.
     */
    private fun saveSortPreference(value: String) {
        getSharedPreferences("settings", MODE_PRIVATE).edit().putString("sort_by", value).apply()
    }

    // Inflate the menu options (sort and theme toggle)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.task_list_menu, menu)
        return true
    }

    // Handle clicks on menu options
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_date -> {
                saveSortPreference("date")
                loadTasks()
                true
            }
            R.id.action_sort_priority -> {
                saveSortPreference("priority")
                loadTasks()
                true
            }
            R.id.action_toggle_theme -> {
                // Toggle dark/light mode
                val mode = AppCompatDelegate.getDefaultNightMode()
                AppCompatDelegate.setDefaultNightMode(
                    if (mode == AppCompatDelegate.MODE_NIGHT_YES)
                        AppCompatDelegate.MODE_NIGHT_NO
                    else AppCompatDelegate.MODE_NIGHT_YES
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Fetches a motivational quote from an external API asynchronously
     * and displays it as a Toast.
     */
    private fun fetchMotivationalQuote() {
        Thread {
            try {
                val conn = URL("https://zenquotes.io/api/random").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val json = conn.inputStream.bufferedReader().readText()
                // The response is a JSON array
                val jsonArray = JSONArray(json)
                val quoteObj = jsonArray.getJSONObject(0)
                val quote = quoteObj.getString("q")
                val author = quoteObj.getString("a")

                runOnUiThread {
                    binding.textViewQuote.text = "\"$quote\" — $author"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.textViewQuote.text = "Failed to load quote"
                }
            }
        }.start()
    }
}
