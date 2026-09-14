package com.example.doline.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [
        UserProfile::class,
        ItemEntity::class,
        Store::class,
        Pricing::class,
        CategoryEntity::class,
        SubCategory::class,
        Expense::class,
        BatchEntity::class,
        BatchPricingCrossRef::class,
        NotesEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ClientEntity::class,
        StaffEntity::class,
        CreditPayment::class,
        SupplierEntity::class,
        ItemSupplierCrossRef::class],
    version = 12,
    exportSchema = false
)
@TypeConverters(
    CurrencyConverter::class,
    PricingDetailsConverter::class,
    PricingSchemeConverter::class,
    FlashPricingConverter::class,
    SpecsConverter::class,
    ItemImageConverter::class,
    StoreCategoryConverter::class,
    DeterminantsConverter::class,
    CartItemConverters::class
)
abstract class DolineStoreDatabase: RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun itemDao(): ItemDao
    abstract fun storeDao(): StoreDao
    abstract fun pricingDao(): PricingDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subCategoryDao(): SubCategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun batchDao(): BatchDao
    abstract fun noteDao(): NoteDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderDao(): OrderDao
    abstract  fun orderItemDao(): OrderItemDao
    abstract fun clientDao(): ClientDao
    abstract fun staffDao(): StaffDao
    abstract fun creditPaymentDao(): CreditPaymentDao
    abstract fun supplierDao(): SupplierDao

    companion object {
        @Volatile
        private var INSTANCE: DolineStoreDatabase? = null

        fun getDatabase(context: Context): DolineStoreDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    DolineStoreDatabase::class.java,
                    "inventory_database"
                )
                    .fallbackToDestructiveMigration(false) // Use only in dev
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}