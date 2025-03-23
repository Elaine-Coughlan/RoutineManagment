package com.elaine.tasksandroutine.data

data class Task(val name: String, val description: String? = null, var completed: Boolean = false)

