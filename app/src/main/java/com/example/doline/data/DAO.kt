package com.example.doline.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface UserProfileDao {
    @Insert
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("SELECT * FROM profiles LIMIT 1")
    fun getProfile(): Flow<UserProfile?>
}
@Dao
interface StoreDao {

    // Get a single store
    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    fun getStoreById(id: Long): Flow<Store?>

    @Insert
    suspend fun insertStore(store: Store): Long

    // Fetch a single store with all its nested items and prices
    @Transaction
    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    fun getStoreWithItems(id: Long): Flow<StoreWithItems?>

    // Fetch all stores
    @Query("SELECT * FROM stores")
    fun getAllStores(): Flow<List<Store>>


    @Update
    suspend fun update(store: Store)

    @Delete
    suspend fun delete(store: Store)
}

@Dao
interface ItemDao {
    @Transaction
    @Query("SELECT * FROM items WHERE storeId = :storeId ORDER BY createdAt ASC")
    fun getAllItems(storeId: Long): Flow<List<ItemWithBatches>>
    @Transaction
    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    fun getItemById(itemId: Long): Flow<ItemWithBatches?>
    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    fun getItem(itemId: Long): Flow<ItemEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bulkInsert(items: List<ItemEntity>)
    @Update
    suspend fun update(item: ItemEntity)
    @Delete
    suspend fun delete(item: ItemEntity)
    @Query("UPDATE batches SET quantity = quantity - :amount WHERE id = :batchId")
    suspend fun decreaseQuantity(batchId: Long, amount: Double)
    @Query("UPDATE batches SET quantity = quantity + :amount WHERE id = :batchId")
    suspend fun increaseQuantity(batchId: Long, amount: Double)
}

@Dao
interface PricingDao {
    @Query("SELECT * FROM pricings WHERE itemId = :itemId")
    fun getAllPricings(itemId: Long): Flow<List<Pricing>>
    @Query("SELECT * FROM pricings WHERE id = :id LIMIT 1")
    fun getPricingById(id: Long): Flow<Pricing?>

    @Transaction
    @Query("""SELECT p.* FROM pricings AS p INNER JOIN batch_pricing_cross_ref AS crossRef ON p.id = crossRef.pricingId WHERE crossRef.batchId < :batchId""")
    suspend fun getPrevPrices(batchId: Long): List<Pricing>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Pricing): Long
    @Update
    suspend fun update(item: Pricing)
    @Delete
    suspend fun delete(item: Pricing)
}

@Dao
interface BatchDao{
    @Insert
    suspend fun insertBatch(batch: BatchEntity): Long
    @Insert
    suspend fun insertBatchPricings(crossRefs: List<BatchPricingCrossRef>)
    @Query("UPDATE batches SET available = available - :qty WHERE id = :batchId")
    suspend fun decreaseQuantity(batchId: Long, qty: Double)
    @Query("UPDATE batches SET available = available + :qty WHERE id = :batchId")
    suspend fun increaseQuantity(batchId: Long, qty: Double)
    @Transaction
    @Query("SELECT * FROM batches WHERE id = :batchId")
    suspend fun getBatchWithItem(batchId: Long): BatchWithItem?
    @Delete
    suspend fun deletePricing(pair: BatchPricingCrossRef)
}


@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    @Transaction
    @Query("SELECT * FROM categories")
    fun getAllCategoriesWithSubCategories(): Flow<List<CategoryWithSubCategories>>
    @Query("SELECT * FROM categories WHERE slug = :slug LIMIT 1")
    fun getCategoryBySlug(slug: String): Flow<CategoryEntity?>
    @Transaction
    @Query("SELECT * FROM categories WHERE slug = :slug LIMIT 1")
    fun getCategoryBySlugWithSubCategories(slug: String): Flow<CategoryWithSubCategories?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)
}

@Dao
interface SubCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subCategory: SubCategory)
}

@Dao
interface  ExpenseDao {
    @Query("SELECT * FROM expenses WHERE storeId = :storeId")
    fun getAllStoreExpenses(storeId: Long): Flow<List<Expense>>
    @Query("SELECT * FROM expenses WHERE storeId = :storeId AND createdAt >= :start OR createdAt <= :end")
    fun getAllStoreExpensesPerDay(storeId: Long, start: Long, end: Long): Flow<List<Expense>>
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    fun getExpenseById(id: Long): Flow<Expense?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE storeId = :storeId")
    fun getAllStoreNotes(storeId: Long): Flow<List<NotesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NotesEntity): Long
}

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items")
    fun getAllItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity): Long

    @Update
    suspend fun editItem(item: CartItemEntity)

    @Delete
    suspend fun deleteItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}