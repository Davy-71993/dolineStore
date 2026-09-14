package com.example.doline.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.doline.zeroed
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
    fun getAllItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity): Long

    @Update
    suspend fun editItem(item: CartItemEntity)

    @Delete
    suspend fun deleteItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderEntity: OrderEntity) : Long

    @Transaction
    @Query("SELECT * FROM orders WHERE storeId = :storeId AND deletedAt IS NULL")
    fun getStoreOrders(storeId: Long) : Flow<List<Order>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Long) : Flow<Order>

    @Update
    suspend fun editOrder(order: OrderEntity)

    @Query("""
        UPDATE orders SET creditBalance = creditBalance - :amount WHERE id = :orderId AND creditBalance IS NOT NULL AND creditBalance >= :amount
    """)
    suspend fun pay(orderId: Long, amount: Double): Int

    @Insert
    suspend fun insertPayment(cp: CreditPayment): Long

    @Transaction
    suspend fun updateCreditBalance(amount: Double, orderId: Long){
        val updated = pay(orderId, amount)
        if (updated == 0){
            throw IllegalStateException("Failed to make payments on #${orderId.zeroed()}")
        }
        val payment = CreditPayment(orderId = orderId, amount = amount)
        insertPayment(payment)
    }

    @Query("""UPDATE orders SET deletedAt = :deletedAt WHERE id = :orderId""")
    suspend fun delete(orderId: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("""UPDATE order_items SET returned = :returned WHERE id = :id""")
    suspend fun returnOrderItem(id: Long, returned: Double)

    @Query("""UPDATE orders SET progress = "RETURNED" WHERE id = :orderId""")
    suspend fun markReturned(orderId: Long)

    @Transaction
    suspend fun returnItems(items: List<OrderItemEntity>) {
        val orderIds = mutableSetOf<Long>()
        for (item in items) {
            returnOrderItem(item.id, item.returned)
            orderIds.add(item.orderId)
        }
        orderIds.forEach { markReturned(it) }
    }
}

@Dao
interface OrderItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderItemEntity: OrderItemEntity) : Long
}

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)

    @Query("SELECT * FROM clients WHERE storeId = :storeId")
    fun getClients(storeId: Long): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    fun getClientById(id: Long): Flow<ClientEntity?>
}

@Dao
interface StaffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(staff: StaffEntity): Long

    @Update
    suspend fun update(staff: StaffEntity)

    @Delete
    suspend fun delete(staff: StaffEntity)

    @Query("SELECT * FROM staffs WHERE storeId = :storeId")
    fun getStaffs(storeId: Long): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staffs WHERE id = :staffId LIMIT 1")
    fun getStaffById(staffId: Long): Flow<StaffEntity>
}

@Dao
interface CreditPaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(creditPayment: CreditPayment): Long

    @Query("SELECT * FROM credit_payments WHERE orderId = :orderId")
    fun getCreditPayments(orderId: Long): Flow<List<CreditPayment>>
}

