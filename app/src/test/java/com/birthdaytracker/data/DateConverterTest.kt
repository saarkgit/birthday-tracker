package com.birthdaytracker.data

import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for DateConverter
 * Location: src/test/java/com/birthdaytracker/data/DateConverterTest.kt
 */
class DateConverterTest {

    private lateinit var converter: DateConverter

    @Before
    fun setup() {
        converter = DateConverter()
    }

    @Test
    fun `fromTimestamp converts timestamp to LocalDate correctly`() {
        val expectedDate = LocalDate.of(2000, 1, 1)
        // Calculate what midnight of this date is in the current timezone
        val timestamp = expectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val result = converter.fromTimestamp(timestamp)

        assertEquals(expectedDate, result)
    }

    @Test
    fun `fromTimestamp returns null for null timestamp`() {
        val result = converter.fromTimestamp(null)

        assertNull(result)
    }

    @Test
    fun `dateToTimestamp converts LocalDate to timestamp correctly`() {
        val date = LocalDate.of(2000, 1, 1)

        val result = converter.dateToTimestamp(date)

        // Should be January 1, 2000 at midnight (timestamp varies by timezone)
        assertEquals(true, result != null && result > 0)
    }

    @Test
    fun `dateToTimestamp returns null for null date`() {
        val result = converter.dateToTimestamp(null)

        assertNull(result)
    }

    @Test
    fun `roundtrip conversion preserves date`() {
        val originalDate = LocalDate.of(1995, 6, 15)

        val timestamp = converter.dateToTimestamp(originalDate)
        val convertedDate = converter.fromTimestamp(timestamp)

        assertEquals(originalDate, convertedDate)
    }

    @Test
    fun `converts current date correctly`() {
        val today = LocalDate.now()

        val timestamp = converter.dateToTimestamp(today)
        val convertedDate = converter.fromTimestamp(timestamp)

        assertEquals(today, convertedDate)
    }

    @Test
    fun `converts very old date correctly`() {
        val oldDate = LocalDate.of(1900, 1, 1)

        val timestamp = converter.dateToTimestamp(oldDate)
        val convertedDate = converter.fromTimestamp(timestamp)

        assertEquals(oldDate, convertedDate)
    }

    @Test
    fun `converts leap year date correctly`() {
        val leapDay = LocalDate.of(2000, 2, 29)

        val timestamp = converter.dateToTimestamp(leapDay)
        val convertedDate = converter.fromTimestamp(timestamp)

        assertEquals(leapDay, convertedDate)
    }
}