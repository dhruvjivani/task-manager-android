package com.conestoga.taskmanager.models

data class Task(
    val id: Int = 0,
    val name: String,
    val dueDate: String,
    val priority: String
)
