package com.birthdaytracker.repository

import app.cash.turbine.test
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.data.BirthdayDao
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for handling twins (multiple people with same birth date)
 * and duplicate entries (same name and same date)
 * Location: src/test/java/com/birthdaytracker/repository/TwinsAndDuplicateTest.kt
 */
class TwinsAndDuplicateTest {

    private lateinit var dao: BirthdayDao
    private lateinit var repository: BirthdayRepository

    @Before
    fun setup() {
        dao = mock()
        repository = BirthdayRepository(dao)
    }

    @Test
    fun `repository handles multiple people with same date - twins`() = runTest {
        val birthDate = LocalDate.of(1995, 6, 15)

        val twin1 = Birthday(1, "Alice Smith", birthDate, "Family")
        val twin2 = Birthday(2, "Bob Smith", birthDate, "Family")
        val twins = listOf(twin1, twin2)

        whenever(dao.getAllBirthdays()).thenReturn(flowOf(twins))

        repository.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(2, birthdays.size)

            // Both should have same birth date
            assertEquals(birthDate, birthdays[0].birthDate)
            assertEquals(birthDate, birthdays[1].birthDate)

            // But different IDs and names
            assertEquals(1L, birthdays[0].id)
            assertEquals(2L, birthdays[1].id)
            assertEquals("Alice Smith", birthdays[0].name)
            assertEquals("Bob Smith", birthdays[1].name)

            awaitComplete()
        }
    }

    @Test
    fun `repository allows same name and same date with different IDs`() = runTest {
        val birthDate = LocalDate.of(1995, 6, 15)

        val person1 = Birthday(1, "John Smith", birthDate, "Friend")
        val person2 = Birthday(2, "John Smith", birthDate, "Family")

        whenever(dao.insertBirthday(any())).thenReturn(1L, 2L)

        val id1 = repository.insertBirthday(person1)
        val id2 = repository.insertBirthday(person2)

        // Should return different IDs
        assertEquals(1L, id1)
        assertEquals(2L, id2)

        verify(dao, times(2)).insertBirthday(any())
    }

    @Test
    fun `repository handles complete duplicates - same name, date, and category`() = runTest {
        val birthDate = LocalDate.of(1995, 6, 15)

        val person1 = Birthday(1, "John Smith", birthDate, "Friend")
        val person2 = Birthday(2, "John Smith", birthDate, "Friend")
        val people = listOf(person1, person2)

        whenever(dao.getAllBirthdays()).thenReturn(flowOf(people))

        repository.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(2, birthdays.size)

            // Everything identical except ID
            assertEquals(birthdays[0].name, birthdays[1].name)
            assertEquals(birthdays[0].birthDate, birthdays[1].birthDate)
            assertEquals(birthdays[0].category, birthdays[1].category)

            // IDs must be different
            assert(birthdays[0].id != birthdays[1].id)

            awaitComplete()
        }
    }

    @Test
    fun `repository handles triplets scenario`() = runTest {
        val birthDate = LocalDate.of(1995, 6, 15)

        val triplets = listOf(
            Birthday(1, "Alice Johnson", birthDate, "Family"),
            Birthday(2, "Bob Johnson", birthDate, "Family"),
            Birthday(3, "Charlie Johnson", birthDate, "Family")
        )

        whenever(dao.getAllBirthdays()).thenReturn(flowOf(triplets))

        repository.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(3, birthdays.size)

            // All have same birth date
            birthdays.forEach {
                assertEquals(birthDate, it.birthDate)
                assertEquals("Family", it.category)
            }

            // All have unique IDs
            val ids = birthdays.map { it.id }
            assertEquals(3, ids.toSet().size)

            awaitComplete()
        }
    }

    @Test
    fun `repository handles multiple people with same month and day but different years`() = runTest {
        val people = listOf(
            Birthday(1, "Alice", LocalDate.of(1990, 6, 15), "Friend"),
            Birthday(2, "Bob", LocalDate.of(1995, 6, 15), "Friend"),
            Birthday(3, "Charlie", LocalDate.of(2000, 6, 15), "Friend")
        )

        whenever(dao.getAllBirthdays()).thenReturn(flowOf(people))

        repository.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(3, birthdays.size)

            // All should have same month and day
            birthdays.forEach {
                assertEquals(6, it.birthDate.monthValue)
                assertEquals(15, it.birthDate.dayOfMonth)
            }

            awaitComplete()
        }
    }

    @Test
    fun `update does not affect other entries with same date`() = runTest {
        val birthDate = LocalDate.of(1995, 6, 15)

        val twin1 = Birthday(1, "Alice", birthDate, "Family")
        val twin2 = Birthday(2, "Bob", birthDate, "Family")
        val updatedTwin1 = Birthday(1, "Alice Smith", birthDate, "Family")

        whenever(dao.getBirthdayById(1)).thenReturn(updatedTwin1)
        whenever(dao.getBirthdayById(2)).thenReturn(twin2)

        repository.updateBirthday(updatedTwin1)

        val retrieved1 = repository.getBirthdayById(1)
        val retrieved2 = repository.getBirthdayById(2)

        assertNotNull(retrieved1)
        assertNotNull(retrieved2)

        // Alice updated
        assertEquals("Alice Smith", retrieved1.name)

        // Bob unchanged
        assertEquals("Bob", retrieved2.name)

        verify(dao).updateBirthday(updatedTwin1)
    }

    @Test
    fun `delete does not affect other entries with same date`() = runTest {
        val birthDate = LocalDate.of(1995, 6, 15)

        val twin1 = Birthday(1, "Alice", birthDate, "Family")
        val twin2 = Birthday(2, "Bob", birthDate, "Family")
        val twin3 = Birthday(3, "Charlie", birthDate, "Family")

        val remainingTwins = listOf(twin1, twin3)
        whenever(dao.getAllBirthdays()).thenReturn(flowOf(remainingTwins))

        repository.deleteBirthday(twin2)

        repository.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(2, birthdays.size)

            // Alice and Charlie remain
            val names = birthdays.map { it.name }.sorted()
            assertEquals(listOf("Alice", "Charlie"), names)

            awaitComplete()
        }

        verify(dao).deleteBirthday(twin2)
    }

    @Test
    fun `repository handles large family with same birthday`() = runTest {
        val birthDate = LocalDate.of(1995, 6, 15)
        val largeFamil = (1..10).map { i ->
            Birthday(i.toLong(), "Person$i", birthDate, "Test")
        }

        whenever(dao.getAllBirthdays()).thenReturn(flowOf(largeFamil))

        repository.getAllBirthdays().test {
            val birthdays = awaitItem()
            assertEquals(10, birthdays.size)

            // All have same birth date
            birthdays.forEach {
                assertEquals(birthDate, it.birthDate)
            }

            // All have unique IDs
            val ids = birthdays.map { it.id }
            assertEquals(10, ids.toSet().size)

            awaitComplete()
        }
    }

    @Test
    fun `getBirthdayById works correctly with duplicate data`() = runTest {
        val birthDate = LocalDate.of(1995, 6, 15)

        val person1 = Birthday(1, "John Smith", birthDate, "Friend")
        val person2 = Birthday(2, "John Smith", birthDate, "Friend")

        whenever(dao.getBirthdayById(1)).thenReturn(person1)
        whenever(dao.getBirthdayById(2)).thenReturn(person2)

        val retrieved1 = repository.getBirthdayById(1)
        val retrieved2 = repository.getBirthdayById(2)

        assertNotNull(retrieved1)
        assertNotNull(retrieved2)

        // Should retrieve correct entries by ID
        assertEquals(1L, retrieved1.id)
        assertEquals(2L, retrieved2.id)

        // Both have same data
        assertEquals(retrieved1.name, retrieved2.name)
        assertEquals(retrieved1.birthDate, retrieved2.birthDate)
    }
}