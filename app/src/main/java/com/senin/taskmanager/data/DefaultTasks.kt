package com.senin.taskmanager.data

import java.time.LocalDate
import java.util.UUID

fun buildDefaultTasks(startDate: LocalDate): List<Task> {
    val tasks = mutableListOf<Task>()
    val today = startDate
    val endDate = today.plusDays(365)

    fun spread(title: String, prio: TaskPriority, freq: TaskFrequency, step: Long) {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId=gid, title=title, frequency=freq, priority=prio, dueDate=cur.toString(), isDefault=true))
            cur = cur.plusDays(step)
        }
    }

    fun spreadMonthly(title: String, prio: TaskPriority, targetDay: Int? = null) {
        val gid = UUID.randomUUID().toString()
        var cur = if (targetDay != null) {
            var d = today.withDayOfMonth(targetDay)
            if (d.isBefore(today)) d = d.plusMonths(1).withDayOfMonth(targetDay)
            d
        } else today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId=gid, title=title, frequency=TaskFrequency.MONTHLY, priority=prio, dueDate=cur.toString(), monthDay=targetDay, isDefault=true))
            cur = cur.plusMonths(1)
        }
    }

    fun spreadSpecificDays(title: String, prio: TaskPriority, days: Set<Int>) {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            if (cur.dayOfWeek.value in days) {
                tasks.add(Task(groupId=gid, title=title, frequency=TaskFrequency.SPECIFIC_DAYS, priority=prio, dueDate=cur.toString(), specificDays=days.sorted().joinToString(","), isDefault=true))
            }
            cur = cur.plusDays(1)
        }
    }

    fun spreadWeekly(title: String, prio: TaskPriority, dayOfWeek: Int) {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (cur.dayOfWeek.value != dayOfWeek) cur = cur.plusDays(1)
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId=gid, title=title, frequency=TaskFrequency.WEEKLY, priority=prio, dueDate=cur.toString(), specificDays="$dayOfWeek", isDefault=true))
            cur = cur.plusDays(7)
        }
    }

    fun spreadBiweekly(title: String, prio: TaskPriority) {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId=gid, title=title, frequency=TaskFrequency.BIWEEKLY, priority=prio, dueDate=cur.toString(), isDefault=true))
            cur = cur.plusDays(3)
            if (!cur.isAfter(endDate)) {
                tasks.add(Task(groupId=gid, title=title, frequency=TaskFrequency.BIWEEKLY, priority=prio, dueDate=cur.toString(), isDefault=true))
                cur = cur.plusDays(4)
            }
        }
    }

    // GÜNLÜK
    spread("Yemek yap", TaskPriority.IMPORTANT, TaskFrequency.DAILY, 1)
    spread("Ranın çantasını kontrol et", TaskPriority.IMPORTANT, TaskFrequency.DAILY, 1)
    spread("Lazımlık boşalt", TaskPriority.OTHER, TaskFrequency.DAILY, 1)
    spread("Çamaşır makinesi doldur ve boşalt", TaskPriority.OTHER, TaskFrequency.DAILY, 1)
    spread("Bulaşık makinesi doldur ve boşalt", TaskPriority.OTHER, TaskFrequency.DAILY, 1)

    // 3 GÜNDE BİR
    spread("Çiçekleri sula", TaskPriority.OTHER, TaskFrequency.EVERY_3_DAYS, 3)
    spread("Kuşlara yem ve su ver", TaskPriority.OTHER, TaskFrequency.EVERY_3_DAYS, 3)

    // BELİRLİ GÜNLER
    spreadSpecificDays("Fitness", TaskPriority.IMPORTANT, setOf(1, 3, 5))
    spreadSpecificDays("Hamam / Havuz", TaskPriority.IMPORTANT, setOf(2, 4, 6))
    spreadSpecificDays("Çocuklar banyo", TaskPriority.IMPORTANT, setOf(4, 7))

    // HAFTALIK
    spreadWeekly("Bale: patik, tütülü etek, k.çorap koy", TaskPriority.IMPORTANT, 2)
    spreadWeekly("Havuz: bone, terlik, mayo, havlu, gözlük, patiği hazırla", TaskPriority.IMPORTANT, 3)
    spreadWeekly("İade ve değişim paketleme, iade kodu hazırla", TaskPriority.OTHER, 3)
    spreadWeekly("Oyuncak koy (Vera)", TaskPriority.OTHER, 4)
    spreadWeekly("Yemek planla ve Pazar alışveriş listesi oluştur", TaskPriority.IMPORTANT, 5)
    spreadWeekly("Veranın suluğunu yıka", TaskPriority.OTHER, 1)

    // HAFTADA 2 KEZ
    spreadBiweekly("Yastık yüzleri değiştir", TaskPriority.OTHER)
    spreadBiweekly("Havlu değiştir", TaskPriority.OTHER)
    spreadBiweekly("Lazımlık yıka", TaskPriority.OTHER)

    // AYDA BİR
    spreadMonthly("Betül kıyafet sirkülasyonu", TaskPriority.OTHER)
    spreadMonthly("Veranın oyuncak sirkülasyonu", TaskPriority.OTHER)
    spreadMonthly("Aşı takvimi ve boy-kilo kontrol", TaskPriority.IMPORTANT, targetDay = 12)

    return tasks
}
