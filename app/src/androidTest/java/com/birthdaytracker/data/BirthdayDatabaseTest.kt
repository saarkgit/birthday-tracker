package com.birthdaytracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Database tests for BirthdayDatabase
 * Location: src/androidTest/java/com/birthdaytracker/data/BirthdayDatabaseTest.kt
 */
@RunWith(AndroidJUnit4::class)
class BirthdayDatabaseTest {

    private lateinit var database: BirthdayDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            BirthdayDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun database_creation_successful() {
        assertNotNull(database)
    }

    @Test
    fun database_provides_dao() {
        val dao = database.birthdayDao()
        assertNotNull(dao)
    }

    @Test
    fun database_migration_not_required_for_version_1() = runTest {
        // This test ensures the database schema is stable
        val dao = database.birthdayDao()

        // Insert a birthday
        val birthday = Birthday(0, "Test", LocalDate.now(), "Test")
        dao.insertBirthday(birthday)

        // Should work without migration
        assertNotNull(dao.getBirthdayById(1))
    }

    @Test
    fun database_type_converter_works() = runTest {
        val dao = database.birthdayDao()
        val testDate = LocalDate.of(1995, 6, 15)

        val birthday = Birthday(0, "Test User", testDate, "Friend")
        val id = dao.insertBirthday(birthday)

        val retrieved = dao.getBirthdayById(id)
        assertNotNull(retrieved)
        assertEquals(testDate, retrieved.birthDate)
    }

    @Test
    fun database_handles_special_characters_in_names() = runTest {
        val dao = database.birthdayDao()

        val specialNames = listOf(
            "O'Brien",
            "José García",
            "François Müller",
            "李明",
            "Владимир"
        )

        specialNames.forEach { name ->
            val birthday = Birthday(0, name, LocalDate.now(), "Test")
            val id = dao.insertBirthday(birthday)

            val retrieved = dao.getBirthdayById(id)
            assertNotNull(retrieved)
            assertEquals(name, retrieved.name)
        }
    }

    @Test
    fun database_handles_long_names() = runTest {
        val dao = database.birthdayDao()
        val longName = "A".repeat(100)

        val birthday = Birthday(0, longName, LocalDate.now(), "Test")
        val id = dao.insertBirthday(birthday)

        val retrieved = dao.getBirthdayById(id)
        assertNotNull(retrieved)
        assertEquals(longName, retrieved.name)
    }

    @Test
    fun database_handles_old_dates() = runTest {
        val dao = database.birthdayDao()
        val oldDate = LocalDate.of(1900, 1, 1)

        val birthday = Birthday(0, "Old Person", oldDate, "Test")
        val id = dao.insertBirthday(birthday)

        val retrieved = dao.getBirthdayById(id)
        assertNotNull(retrieved)
        assertEquals(oldDate, retrieved.birthDate)
    }

    @Test
    fun database_handles_leap_year_dates() = runTest {
        val dao = database.birthdayDao()
        val leapDay = LocalDate.of(2000, 2, 29)

        val birthday = Birthday(0, "Leap Baby", leapDay, "Test")
        val id = dao.insertBirthday(birthday)

        val retrieved = dao.getBirthdayById(id)
        assertNotNull(retrieved)
        assertEquals(leapDay, retrieved.birthDate)
    }
}