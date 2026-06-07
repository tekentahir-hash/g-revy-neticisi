package com.senin.taskmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senin.taskmanager.data.TaskFrequency
import com.senin.taskmanager.data.TaskPriority
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(initialDate: LocalDate = LocalDate.now(), onBack: () -> Unit, viewModel: TaskViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var freq by remember { mutableStateOf(TaskFrequency.DAILY) }
    var prio by remember { mutableStateOf(TaskPriority.IMPORTANT) }
    var dueDate by remember { mutableStateOf(initialDate) }
    var timeHour by remember { mutableStateOf<Int?>(null) }
    var timeMin by remember { mutableStateOf<Int?>(null) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dayLabels = listOf(1 to "Pzt", 2 to "Sal", 3 to "Çrş", 4 to "Per", 5 to "Cum", 6 to "Cmt", 7 to "Paz")

    if (showTimePicker) {
        val ts = rememberTimePickerState(initialHour = timeHour ?: 9, initialMinute = timeMin ?: 0, is24Hour = true)
        AlertDialog(onDismissRequest = { showTimePicker = false },
            confirmButton = { TextButton(onClick = { timeHour = ts.hour; timeMin = ts.minute; showTimePicker = false }) { Text("Tamam") } },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("İptal") } },
            text = { TimePicker(state = ts) })
    }
    if (showDatePicker) {
        val ds = rememberDatePickerState(initialSelectedDateMillis = dueDate.toEpochDay() * 86400000L)
        DatePickerDialog(onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { ds.selectedDateMillis?.let { dueDate = LocalDate.ofEpochDay(it / 86400000L) }; showDatePicker = false }) { Text("Tamam") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("İptal") } }) { DatePicker(state = ds) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Yeni Görev") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Görev adı *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Açıklama (opsiyonel)") }, modifier = Modifier.fillMaxWidth())
            Text("Tarih", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("📅 ${dueDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr")))}")
            }
            Text("Saat (bildirim için opsiyonel)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showTimePicker = true }) { Text(if (timeHour != null) "🕐 %02d:%02d".format(timeHour, timeMin) else "Saat seç") }
                if (timeHour != null) TextButton(onClick = { timeHour = null; timeMin = null }) { Text("Kaldır") }
            }
            Text("Tekrar Sıklığı", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(freq == TaskFrequency.ONCE, { freq = TaskFrequency.ONCE }, label = { Text("Tek seferlik") })
                    FilterChip(freq == TaskFrequency.DAILY, { freq = TaskFrequency.DAILY }, label = { Text("Günlük") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(freq == TaskFrequency.EVERY_2_DAYS, { freq = TaskFrequency.EVERY_2_DAYS }, label = { Text("2 Günlük") })
                    FilterChip(freq == TaskFrequency.EVERY_3_DAYS, { freq = TaskFrequency.EVERY_3_DAYS }, label = { Text("3 Günlük") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(freq == TaskFrequency.WEEKLY, { freq = TaskFrequency.WEEKLY }, label = { Text("Haftada 1") })
                    FilterChip(freq == TaskFrequency.BIWEEKLY, { freq = TaskFrequency.BIWEEKLY }, label = { Text("Haftada 2") })
                    FilterChip(freq == TaskFrequency.SPECIFIC_DAYS, { freq = TaskFrequency.SPECIFIC_DAYS }, label = { Text("Belirli günler") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(freq == TaskFrequency.MONTHLY, { freq = TaskFrequency.MONTHLY }, label = { Text("Ayda bir") })
                }
            }
            if (freq == TaskFrequency.SPECIFIC_DAYS) {
                Text("Hangi günler?", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayLabels.forEach { (num, label) ->
                        FilterChip(num in selectedDays, { selectedDays = if (num in selectedDays) selectedDays - num else selectedDays + num },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) })
                    }
                }
            }
            if (freq == TaskFrequency.WEEKLY) Text("Her ${getDayName(dueDate.dayOfWeek.value)} tekrarlanacak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            if (freq == TaskFrequency.MONTHLY) Text("Her ayın ${dueDate.dayOfMonth}. günü tekrarlanacak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Text("Öncelik", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(prio == TaskPriority.IMPORTANT, { prio = TaskPriority.IMPORTANT }, label = { Text("⭐ Önemli") })
                FilterChip(prio == TaskPriority.OTHER, { prio = TaskPriority.OTHER }, label = { Text("📌 Diğer") })
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (title.isNotBlank()) {
                    val timeStr = if (timeHour != null) "%02d:%02d".format(timeHour, timeMin) else null
                    viewModel.addTask(title, desc, freq, prio, dueDate, timeStr, selectedDays.toList(), dueDate.dayOfMonth)
                    onBack()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
        }
    }
}

fun getDayName(v: Int) = when(v) { 1->"Pazartesi"; 2->"Salı"; 3->"Çarşamba"; 4->"Perşembe"; 5->"Cuma"; 6->"Cumartesi"; 7->"Pazar"; else->"" }
