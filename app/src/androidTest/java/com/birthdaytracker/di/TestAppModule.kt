package com.birthdaytracker.di

import android.content.Context
import androidx.room.Room
import com.birthdaytracker.data.BirthdayDao
import com.birthdaytracker.data.BirthdayDatabase
import com.birthdaytracker.repository.BirthdayRepository
import com.birthdaytracker.util.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Test module that replaces AppModule for instrumented tests
 * This provides in-memory database for testing instead of persistent database
 *
 * Location: src/androidTest/java/com/birthdaytracker/di/TestAppModule.kt
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideTestBirthdayDatabase(@ApplicationContext context: Context): BirthdayDatabase {
        // Use in-memory database for tests - data is cleared after each test
        return Room.inMemoryDatabaseBuilder(
            context,
            BirthdayDatabase::class.java
        )
            .allowMainThreadQueries() // Allow for testing purposes
            .build()
    }

    @Provides
    @Singleton
    fun provideBirthdayDao(database: BirthdayDatabase): BirthdayDao {
        return database.birthdayDao()
    }

    @Provides
    @Singleton
    fun provideBirthdayRepository(birthdayDao: BirthdayDao): BirthdayRepository {
        return BirthdayRepository(birthdayDao)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}