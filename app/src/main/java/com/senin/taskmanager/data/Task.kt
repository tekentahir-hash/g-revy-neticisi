package com.senin.taskmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskFrequency {
    ONCE, DAILY, EVERY_2_DAYS, EVERY_3_DAYS,
    SPECIFIC_DAYS, WEEKLY, BIWEEKLY, MONTHLY
}

enum class TaskPriority { IMPORTANT, OTHER }

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: String = "",
    val title: String,
    val description: String = "",
    val frequency: TaskFrequency,
    val priority: TaskPriority,
    val dueDate: String = java.time.LocalDate.now().toString(),
    val dueTime: String? = null,
    val specificDays: String = "",
    val monthDay: Int? = null,
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val isDefault: Boolean = false
)
