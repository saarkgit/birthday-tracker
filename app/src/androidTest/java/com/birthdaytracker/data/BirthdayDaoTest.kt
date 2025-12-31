package com.birthdaytracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Database tests for BirthdayDao
 * Location: src/androidTest/java/com/birthdaytracker/data/BirthdayDaoTest.kt
 */
@RunWith(AndroidJUnit4::class)
class BirthdayDaoTest {

    private lateinit var database: BirthdayDatabase
    private lateinit var dao: BirthdayDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            BirthdayDatabase::class.java
        ).build()

        dao = database.birthdayDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertBirthday_and_getBirthdayById() = runTest {
        val birthday = Birthday(
            id = 0,
            name = "John Doe",
            birthDate = LocalDate.of(1990, 5, 15),
            category = "Friend"
        )

        val id = dao.insertBirthday(birthday)
        val retrieved = dao.getBirthdayById(id)

        assertNotNull(retrieved)
        assertEquals("John Doe", retrieved.name)
        assertEquals(LocalDate.of(1990, 5, 15), retrieved.birthDate)
        assertEquals("Friend", retrieved.category)
    }

    @Test
    fun getAllBirthdays_emits_inserted_birthdays() = runTest {
        val birthday1 = Birthday(0, "Alice", LocalDate.of(1995, 3, 20), "Family")
        val birthday2 = Birthday(0, "Bob", LocalDate.of(1988, 7, 10), "Friend")

        dao.insertBirthday(birthday1)
        dao.insertBirthday(birthday2)

        dao.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(2, birthdays.size)
            assertEquals("Bob", birthdays[0].name)  //bob is older (born 1988)
            assertEquals("Alice", birthdays[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateBirthday_updates_existing_birthday() = runTest {
        val birthday = Birthday(0, "Charlie", LocalDate.of(1992, 1, 1), "Work")
        val id = dao.insertBirthday(birthday)

        val updated = Birthday(id, "Charles", LocalDate.of(1992, 1, 1), "Colleague")
        dao.updateBirthday(updated)

        val retrieved = dao.getBirthdayById(id)
        assertNotNull(retrieved)
        assertEquals("Charles", retrieved.name)
        assertEquals("Colleague", retrieved.category)
    }

    @Test
    fun deleteBirthday_removes_birthday() = runTest {
        val birthday = Birthday(0, "David", LocalDate.of(1985, 12, 25), "Family")
        val id = dao.insertBirthday(birthday)

        val toDelete = dao.getBirthdayById(id)!!
        dao.deleteBirthday(toDelete)

        val retrieved = dao.getBirthdayById(id)
        assertNull(retrieved)
    }

    @Test
    fun getAllBirthdays_returns_empty_for_empty_database() = runTest {
        dao.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(0, birthdays.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertBirthday_with_duplicate_replaces_existing() = runTest {
        val birthday1 = Birthday(1, "Eve", LocalDate.of(1993, 6, 6), "Friend")
        val birthday2 = Birthday(1, "Eve Updated", LocalDate.of(1993, 6, 6), "Best Friend")

        dao.insertBirthday(birthday1)
        dao.insertBirthday(birthday2)

        val retrieved = dao.getBirthdayById(1)
        assertNotNull(retrieved)
        assertEquals("Eve Updated", retrieved.name)
        assertEquals("Best Friend", retrieved.category)
    }

    @Test
    fun getAllBirthdays_sorted_by_birthDate() = runTest {
        dao.insertBirthday(Birthday(0, "Mike", LocalDate.of(1990, 12, 31), ""))
        dao.insertBirthday(Birthday(0, "Alice", LocalDate.of(1990, 1, 1), ""))
        dao.insertBirthday(Birthday(0, "Zoe", LocalDate.of(1990, 6, 15), ""))

        dao.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(3, birthdays.size)
            assertEquals("Alice", birthdays[0].name) // Jan 1
            assertEquals("Zoe", birthdays[1].name)  // Jun 15
            assertEquals("Mike", birthdays[2].name)   // Dec 31
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertBirthday_with_empty_category() = runTest {
        val birthday = Birthday(0, "Frank", LocalDate.of(1987, 4, 4), "")
        val id = dao.insertBirthday(birthday)

        val retrieved = dao.getBirthdayById(id)
        assertNotNull(retrieved)
        assertEquals("", retrieved.category)
    }

    @Test
    fun getBirthdayById_returns_null_for_nonexistent_id() = runTest {
        val retrieved = dao.getBirthdayById(999)
        assertNull(retrieved)
    }
}