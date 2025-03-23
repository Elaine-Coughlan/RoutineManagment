package com.elaine.tasksandroutine.data

data class Routine(val name: String, val tasks: MutableList<Task> = mutableListOf())