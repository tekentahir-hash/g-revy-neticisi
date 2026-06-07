package com.senin.taskmanager.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dueDate = :date AND isDeleted = 0 ORDER BY priority ASC, dueTime ASC")
    fun getTasksForDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY dueDate ASC, dueTime ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND frequency != 'ONCE' AND groupId != '' GROUP BY groupId ORDER BY frequency ASC, title ASC")
    fun getRecurringGroups(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND frequency = 'ONCE' ORDER BY dueDate ASC")
    fun getOnceOffTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate < :today AND isCompleted = 0 AND isDeleted = 0 AND frequency != 'ONCE'")
    suspend fun getOverdueTasks(today: String): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("UPDATE tasks SET isDeleted = 1 WHERE groupId = :groupId")
    suspend fun deleteGroup(groupId: String)

    @Query("UPDATE tasks SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    @Query("SELECT COUNT(*) FROM tasks WHERE isDefault = 1")
    suspend fun defaultTaskCount(): Int
}

class Converters {
    @TypeConverter fun fromFreq(v: String) = TaskFrequency.valueOf(v)
    @TypeConverter fun toFreq(f: TaskFrequency) = f.name
    @TypeConverter fun fromPrio(v: String) = TaskPriority.valueOf(v)
    @TypeConverter fun toPrio(p: TaskPriority) = p.name
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    companion object {
        @Volatile private var INSTANCE: TaskDatabase? = null
        fun getDatabase(context: Context) = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context, TaskDatabase::class.java, "task_db")
                .build().also { INSTANCE = it }
        }
    }
}
