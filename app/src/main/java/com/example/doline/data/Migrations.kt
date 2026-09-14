package com.example.doline.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE orders ADD COLUMN deletedAt INTEGER")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS suppliers (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                storeId INTEGER NOT NULL,
                name TEXT NOT NULL,
                address TEXT,
                phone TEXT,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(storeId) REFERENCES stores(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS item_supplier_cross_ref (
                itemId INTEGER NOT NULL,
                supplierId INTEGER NOT NULL,
                PRIMARY KEY(itemId, supplierId),
                FOREIGN KEY(itemId) REFERENCES items(id) ON DELETE CASCADE,
                FOREIGN KEY(supplierId) REFERENCES suppliers(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }
}

// Add each new Migration here as the schema evolves, instead of relying on
// destructive fallback, so upgrading the app doesn't wipe local data.
val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_10_11, MIGRATION_11_12)
