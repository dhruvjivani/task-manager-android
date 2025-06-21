package com.conestoga.taskmanager.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.conestoga.taskmanager.models.Task

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DUE_DATE = "due_date"
        const val COLUMN_PRIORITY = "priority"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DUE_DATE TEXT NOT NULL,
                $COLUMN_PRIORITY TEXT NOT NULL
            );
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addTask(task: Task): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, task.name)
            put(COLUMN_DUE_DATE, task.dueDate)    // use COLUMN_DUE_DATE = "due_date"
            put(COLUMN_PRIORITY, task.priority)
        }
        val id = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return id
    }

    fun getAllTasks(): List<Task> {
        val taskList = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val task = Task(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)),  // <-- here
                    priority = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY))
                )
                taskList.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return taskList
    }

    fun getTaskById(id: Int): Task? {
        Log.d("TaskDatabaseHelper", "getTaskById called with id: $id")
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID=?", arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            val task = Task(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)),
                priority = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY))
            )
            cursor.close()
            Log.d("TaskDatabaseHelper", "Task found: $task")
            return task
        }
        cursor.close()
        Log.d("TaskDatabaseHelper", "No task found with id: $id")
        return null
    }

    fun updateTask(task: Task): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, task.name)
            put(COLUMN_DUE_DATE, task.dueDate)  // <-- here
            put(COLUMN_PRIORITY, task.priority)
        }
        val rowsUpdated = db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(task.id.toString()))
        return rowsUpdated > 0
    }


    // Delete a task by ID
    fun deleteTask(id: Int): Boolean {
        val db = writableDatabase
        val affectedRows = db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
        return affectedRows > 0
    }
}
