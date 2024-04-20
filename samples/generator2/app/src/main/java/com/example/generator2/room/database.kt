package com.example.generator2.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [EntityPlaylist::class], exportSchema = true, version = 1,
    //autoMigrations = [AutoMigration(from = 1, to = 2),
    //    AutoMigration(from = 2, to = 3)]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun maindao(): EntityMainDao


}


val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE EntityMain ADD COLUMN i INTEGER")

    }
}