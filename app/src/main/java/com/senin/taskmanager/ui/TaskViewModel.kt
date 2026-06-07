package com.senin.taskmanager.ui

import android.app.Application
import androidx.lifecycle.*
import com.senin.taskmanager.data.*
import com.senin.taskmanager.notification.scheduleNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = TaskDatabase.getDatabase(application).taskDao()
    private val app = application

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    fun setSelectedDate(date: LocalDate) { _selectedDate.value = date }
    fun getTasksForDate(date: LocalDate) = dao.getTasksForDate(date.toString())

    val allTasks = dao.getAllTasks()
    val recurringGroups = dao.getRecurringGroups()
    val onceOffTasks = dao.getOnceOffTasks()

    fun seedDefaultTasksIfNeeded() {
        viewModelScope.launch {
            if (dao.defaultTaskCount() == 0) {
                buildDefaultTasks(LocalDate.now()).forEach { dao.insertTask(it) }
            }
        }
    }

    fun addTask(title: String, desc: String, freq: TaskFrequency, prio: TaskPriority,
                dueDate: LocalDate, dueTime: String?, specificDays: List<Int> = emptyList(), monthDay: Int? = null) {
        viewModelScope.launch {
            val gid = if (freq == TaskFrequency.ONCE) "" else UUID.randomUUID().toString()
            val endDate = dueDate.plusDays(365)

            suspend fun insert(date: LocalDate) {
                val id = dao.insertTask(Task(groupId=gid, title=title, description=desc, frequency=freq,
                    priority=prio, dueDate=date.toString(), dueTime=dueTime,
                    specificDays=specificDays.sorted().joinToString(","), monthDay=monthDay))
                if (dueTime != null) scheduleNotification(app, id.toInt(), title, date.toString(), dueTime)
            }

            when (freq) {
                TaskFrequency.ONCE -> insert(dueDate)
                TaskFrequency.DAILY -> { var c = dueDate; while (!c.isAfter(endDate)) { insert(c); c = c.plusDays(1) } }
                TaskFrequency.EVERY_2_DAYS -> { var c = dueDate; while (!c.isAfter(endDate)) { insert(c); c = c.plusDays(2) } }
                TaskFrequency.EVERY_3_DAYS -> { var c = dueDate; while (!c.isAfter(endDate)) { insert(c); c = c.plusDays(3) } }
                TaskFrequency.WEEKLY -> { var c = dueDate; while (!c.isAfter(endDate)) { insert(c); c = c.plusDays(7) } }
                TaskFrequency.BIWEEKLY -> {
                    var c = dueDate
                    while (!c.isAfter(endDate)) { insert(c); c = c.plusDays(3); if (!c.isAfter(endDate)) { insert(c); c = c.plusDays(4) } }
                }
                TaskFrequency.SPECIFIC_DAYS -> {
                    val days = specificDays.toSet()
                    var c = dueDate
                    while (!c.isAfter(endDate)) { if (c.dayOfWeek.value in days) insert(c); c = c.plusDays(1) }
                }
                TaskFrequency.MONTHLY -> {
                    val td = monthDay ?: dueDate.dayOfMonth
                    var c = dueDate
                    while (!c.isAfter(endDate)) { insert(c); c = try { c.plusMonths(1).withDayOfMonth(td) } catch (e: Exception) { c.plusMonths(1) } }
                }
            }
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch { dao.updateTask(task.copy(isCompleted = !task.isCompleted)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { dao.softDelete(task.id) }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch { dao.deleteGroup(groupId) }
    }

    fun rolloverOverdue() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            dao.getOverdueTasks(today).forEach { dao.updateTask(it.copy(dueDate = today)) }
        }
    }
}
