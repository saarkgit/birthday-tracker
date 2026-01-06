package com.birthdaytracker.notification

import com.birthdaytracker.data.Birthday
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BirthdayNotificationHelperTest {

    private lateinit var helper: BirthdayNotificationHelper

    @Before
    fun setup() {
        helper = BirthdayNotificationHelper()
    }

    @Test
    fun `returns birthday when it's today and day-of enabled`() {
        val today = LocalDate.now()
        val birthdays = listOf(
            Birthday(1, "Alice", today.minusYears(25), "Friend")
        )

        val result = helper.getBirthdaysToNotify(birthdays, notificationDayOf = true, notificationWeekBefore = false)

        assertEquals(1, result.size)
        assertEquals("Alice", result[0].first.name)
        assertEquals(0, result[0].second)
    }

    @Test
    fun `returns birthday when it's in one week and week-before enabled`() {
        val inOneWeek = LocalDate.now().plusDays(7)
        val birthdays = listOf(
            Birthday(1, "Bob", inOneWeek.minusYears(30), "Family")
        )

        val result = helper.getBirthdaysToNotify(birthdays, notificationDayOf = false, notificationWeekBefore = true)

        assertEquals(1, result.size)
        assertEquals("Bob", result[0].first.name)
        assertEquals(7, result[0].second)
    }

    @Test
    fun `returns empty when notifications are disabled`() {
        val today = LocalDate.now()
        val birthdays = listOf(
            Birthday(1, "Alice", today.minusYears(25), "Friend")
        )

        val result = helper.getBirthdaysToNotify(birthdays, notificationDayOf = false, notificationWeekBefore = false)

        assertEquals(0, result.size)
    }

    @Test
    fun `handles multiple birthdays on same day`() {
        val today = LocalDate.now()
        val birthdays = listOf(
            Birthday(1, "Alice", today.minusYears(25), "Friend"),
            Birthday(2, "Bob", today.minusYears(30), "Friend"),
            Birthday(3, "Charlie", today.minusYears(22), "Friend")
        )

        val result = helper.getBirthdaysToNotify(birthdays, notificationDayOf = true, notificationWeekBefore = false)

        assertEquals(3, result.size)
    }

    // ...existing code...
    @Test
    fun `days until uses total days across year boundary`() {
        val helper = BirthdayNotificationHelper()
        val today = LocalDate.of(2024, 12, 31)
//        val oldDate = LocalDate.of(1990, 1, 1)
//        val timeBetweenDates = today.toEpochDay() - oldDate.toEpochDay()
        val birthdays = listOf(
            Birthday( name = "new year", birthDate = LocalDate.of(1990, 1, 7))
        )

        val notify = helper.getBirthdaysToNotify(
            birthdays,
            notificationWeekBefore = true,
            notificationDayOf = true,
            today = today
        )

//        assertEquals(timeBetweenDates, 12783)
        assertTrue(notify.isNotEmpty())
        assertEquals(7, notify[0].second)
    }

    @Test
    fun `leap year birthday notifies on feb 21 in non leap years`() {
        val helper = BirthdayNotificationHelper()
        val today = LocalDate.of(2023, 2, 21) // non-leap year
        val birthdays = listOf(
            Birthday(name = "Leap", birthDate = LocalDate.of(1992, 2, 29))
        )

        val notify = helper.getBirthdaysToNotify(birthdays, notificationWeekBefore = true, notificationDayOf = false, today = today)
        assertTrue(notify.isNotEmpty())
    }
}