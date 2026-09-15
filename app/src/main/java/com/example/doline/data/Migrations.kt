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

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE staffs ADD COLUMN username TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE staffs ADD COLUMN passKeyHash TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE staffs ADD COLUMN isAdmin INTEGER NOT NULL DEFAULT 0")
    }
}

// Staffs/suppliers/clients now reference a profiles row for their name/phone/address/username
// instead of storing it locally, and profileId is required. None of the existing rows have a
// matching profile, so they're dropped rather than left dangling with an unfillable column.
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS staffs")
        db.execSQL(
            """
            CREATE TABLE staffs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                storeId INTEGER NOT NULL,
                role TEXT,
                passKeyHash TEXT NOT NULL DEFAULT '',
                isAdmin INTEGER NOT NULL DEFAULT 0,
                profileId TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS suppliers")
        db.execSQL(
            """
            CREATE TABLE suppliers (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                storeId INTEGER NOT NULL,
                profileId TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(storeId) REFERENCES stores(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS clients")
        db.execSQL(
            """
            CREATE TABLE clients (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                storeId INTEGER NOT NULL,
                profileId TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

// Profiles now get their own local auto-increment id as the primary key (userId becomes a
// plain nullable column, set only for the device owner's Supabase-synced profile), and
// staff/supplier/client profileId columns switch from the old cloud-userId TEXT reference to
// that local INTEGER id. None of the existing profileId values match a local id, so those
// tables are dropped rather than left with unresolvable references.
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS profiles")
        db.execSQL(
            """
            CREATE TABLE profiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT,
                email TEXT,
                username TEXT,
                fullNames TEXT,
                phone TEXT,
                avatarUrl TEXT,
                about TEXT,
                defaultAddress TEXT
            )
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS staffs")
        db.execSQL(
            """
            CREATE TABLE staffs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                storeId INTEGER NOT NULL,
                role TEXT,
                passKeyHash TEXT NOT NULL DEFAULT '',
                isAdmin INTEGER NOT NULL DEFAULT 0,
                profileId INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS suppliers")
        db.execSQL(
            """
            CREATE TABLE suppliers (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                storeId INTEGER NOT NULL,
                profileId INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(storeId) REFERENCES stores(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS clients")
        db.execSQL(
            """
            CREATE TABLE clients (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                storeId INTEGER NOT NULL,
                profileId INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

// Adds cloud-sync bookkeeping: updatedAt/deletedAt on profiles and stores (needed to detect
// "changed since last pull" and to propagate deletes as tombstones instead of hard deletes),
// plus the sync_queue outbox and sync_state pull-watermark tables. Only profiles and stores are
// wired into sync for now; other tables can adopt the same columns/queue later without another
// schema change to sync_queue/sync_state themselves.
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE profiles ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE profiles ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE profiles ADD COLUMN deletedAt INTEGER")

        db.execSQL("ALTER TABLE stores ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE stores ADD COLUMN deletedAt INTEGER")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                entityType TEXT NOT NULL,
                localId INTEGER NOT NULL,
                operation TEXT NOT NULL,
                payload TEXT,
                cloudId TEXT,
                createdAt INTEGER NOT NULL,
                attempts INTEGER NOT NULL DEFAULT 0,
                lastError TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_state (
                entityType TEXT PRIMARY KEY NOT NULL,
                lastPulledAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

// Add each new Migration here as the schema evolves, instead of relying on
// destructive fallback, so upgrading the app doesn't wipe local data.
val ALL_MIGRATIONS: Array<Migration> = arrayOf(
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13,
    MIGRATION_13_14,
    MIGRATION_14_15,
    MIGRATION_15_16,
    MIGRATION_16_17
)
