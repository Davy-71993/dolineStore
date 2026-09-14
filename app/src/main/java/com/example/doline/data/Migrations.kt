package com.example.doline.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE orders ADD COLUMN deletedAt INTEGER")
    }
}

// Add each new Migration here as the schema evolves, instead of relying on
// destructive fallback, so upgrading the app doesn't wipe local data.
val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_10_11)
