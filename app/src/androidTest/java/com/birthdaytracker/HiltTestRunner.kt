package com.birthdaytracker

/*
import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * A custom runner for Hilt to set up the instrumented test application.
 * This runner is essential for all Hilt UI and integration tests. It solves
 * the "cannot use a @HiltAndroidApp application" error by swapping in
 * HiltTestApplication during the test run.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}








/*
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.Configuration
import androidx.work.WorkManager
import com.birthdaytracker.data.BirthdayDao
import com.birthdaytracker.data.BirthdayDatabase
import com.birthdaytracker.di.AppModule
import com.birthdaytracker.notification.BirthdayNotificationHelper
import com.birthdaytracker.repository.BirthdayRepository
import com.birthdaytracker.util.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Custom test runner that uses com.birthdaytracker.HiltTestApplication
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideTestBirthdayDatabase(@ApplicationContext context: Context): BirthdayDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            BirthdayDatabase::class.java
        )
            .allowMainThreadQueries()
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

    @Provides
    @Singleton
    fun provideBirthdayNotificationHelper(): BirthdayNotificationHelper {
        return BirthdayNotificationHelper()
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        // Provide a WorkManager instance with a test-friendly configuration.
        // This configuration uses a synchronous executor, which runs all work
        // immediately and on the same thread, preventing background thread issues.
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

        // Initialize WorkManager manually for the test.
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }
}*/