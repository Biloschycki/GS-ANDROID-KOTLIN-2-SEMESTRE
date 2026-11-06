package br.com.fiap.horacerta.di

import android.content.Context
import androidx.room.Room
import br.com.fiap.horacerta.data.local.AppDatabase
import br.com.fiap.horacerta.data.local.MedicamentoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "hora-certa.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideMedicamentoDao(database: AppDatabase): MedicamentoDao = database.medicamentoDao()
}
