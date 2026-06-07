package com.senin.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senin.taskmanager.notification.createNotificationChannel
import com.senin.taskmanager.ui.AddTaskScreen
import com.senin.taskmanager.ui.CalendarScreen
import com.senin.taskmanager.ui.SettingsScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        setContent {
            MaterialTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "calendar") {
                    composable("calendar") { CalendarScreen(onAddTask = { nav.navigate("add/$it") }, onSettings = { nav.navigate("settings") }) }
                    composable("add/{date}") { back ->
                        val d = back.arguments?.getString("date") ?: LocalDate.now().toString()
                        AddTaskScreen(initialDate = LocalDate.parse(d), onBack = { nav.popBackStack() })
                    }
                    composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
                }
            }
        }
    }
}
