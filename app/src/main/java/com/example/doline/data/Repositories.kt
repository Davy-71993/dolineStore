package com.example.doline.data

import androidx.activity.result.contract.ActivityResultContracts
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: Auth
){
    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus
    val currentUser: UserInfo? get() = auth.currentUserOrNull()
    val currentSession: UserSession? get() = auth.currentSessionOrNull()
    suspend fun register(email: String, password: String): Boolean {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun resendVerificationEmail(email: String) {
        auth.resendEmail(
            type = OtpType.Email.SIGNUP,
            email = email
        )
    }
    suspend fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Log error if needed, but signOut usually succeeds even on network issues
            e.printStackTrace()
        }
    }
    suspend fun refreshSession() {
        try {
            auth.refreshCurrentSession()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun isUserAuthenticated(): Boolean {
        return currentSession != null && currentUser != null
    }

}

class UserProfileRepository @Inject constructor(
    private  val dao: UserProfileDao,
    private  val supabaseClient: SupabaseClient
){
    fun getProfile(): Flow<UserProfile?> = dao.getProfile()
    suspend fun insertProfile(profile: UserProfile) = dao.insertProfile(profile)
    suspend fun updateProfile(profile: UserProfile) = dao.updateProfile(profile)
    suspend fun fetchProfileFromCloud(userId: String): UserProfile {
        return withContext(Dispatchers.IO){
            supabaseClient.from("profiles").select(
                Columns.list("userId:user_id, phone, username:name, fullNames:full_name, avatarUrl:avatar_url, about, defaultAddress:default_address")
            ) {
                filter {
                    eq("user_id", userId)
                }
            }
        }.decodeSingle<UserProfile>()
    }
}

class StoreRepository @Inject constructor(
    private val dao: StoreDao,
    private val supabaseClient: SupabaseClient
) {
    fun getStoreById(storeId: Long): Flow<Store?> = dao.getStoreById(storeId)
    fun getStoreWithItems(storeId: Long): Flow<StoreWithItems?> = dao.getStoreWithItems(storeId)
    fun getAllStores(): Flow<List<Store>> = dao.getAllStores()
    suspend fun insertStore(store: Store) = dao.insertStore(store)
    suspend fun updateStore(store: Store) = dao.update(store)
    suspend fun deleteStore(store: Store) = dao.delete(store)
    suspend fun fetchStoresFromCloud(userId: String): List<Store> {
        return withContext(Dispatchers.IO) {
            val result = supabaseClient.from("stores")
                .select(Columns.list("cloudId:id, name, description, address, logo, status, items:ads(name:title, description, specs, categorySlug:category_id, subCategorySlug:sub_category_id, quantity, units)")){
                    filter {
                        eq("keeper_id", userId)
                    }
                }.decodeList<Store>()
            result
        }
    }
}

class ItemRepository @Inject constructor(
    private val dao: ItemDao,
    private val upcApiService: UPCApiService
) {
    fun getAllItems(storeId: Long): Flow<List<ItemWithBatches>> = dao.getAllItems(storeId)
    fun getItemById(itemId: Long): Flow<ItemWithBatches?> = dao.getItemById(itemId)
    fun getItem(itemId: Long): Flow<ItemEntity?> = dao.getItem(itemId)
    fun getAllActiveItems(storeID: Long): Flow<List<ItemWithBatches>> {
        return dao.getAllItems(storeID)
            .map { list ->
                list.map { (item, batches) -> ItemWithBatches(item = item, batches = batches.filter { it.batch.quantity > 0 }) }
            }
    }
    fun getAllOutOfStockItems(storeID: Long): Flow<List<ItemWithBatches>> {
        return dao.getAllItems(storeID)
            .map { list ->
                list.map { (item, batches) ->
                    ItemWithBatches(item = item, batches = batches.filter {
                        it.batch.quantity == 0.toDouble()
                    })
                }
            }
    }
    suspend fun fetchInitialItemDetails(upc: String): UPCItem? {
        return try {
            val response = upcApiService.lookupUPC(upc)
            response.items?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun insertItem(item: ItemEntity) = dao.insert(item)
    suspend fun bulkInsertItems(items: List<ItemEntity>) = dao.bulkInsert(items)
    suspend fun updateItem(item: ItemEntity) = dao.update(item)
    suspend fun deleteItem(item: ItemEntity) = dao.delete(item)
    suspend fun decreaseQuantity(batchId: Long, amount: Double) = dao.decreaseQuantity(batchId, amount)
    suspend fun increaseQuantity(batchId: Long, amount: Double) = dao.increaseQuantity(batchId, amount)
}

class PricingRepository @Inject constructor(
    private val dao: PricingDao
) {
    fun getItemPricings(itemId: Long) = dao.getAllPricings(itemId)
    fun getPricingById(id: Long) = dao.getPricingById(id)
    suspend fun getPrevPrices(batchId: Long) = dao.getPrevPrices(batchId)
    suspend fun insertPricing(pricing: Pricing) = dao.insert(pricing)
    suspend fun updatePricing(pricing: Pricing) = dao.update(pricing)
    suspend fun deletePricing(pricing: Pricing) = dao.delete(pricing)
}

class BatchRepository @Inject constructor(
    private val dao: BatchDao,
){
    suspend fun insertBatch(batch: BatchEntity) = dao.insertBatch(batch)
    suspend fun insertBatchPricings(pairs: List<BatchPricingCrossRef>) = dao.insertBatchPricings(pairs)
    suspend fun decreaseQuantity(batchId: Long, qty: Double) = dao.decreaseQuantity(batchId, qty)
    suspend fun increaseQuantity(batchId: Long, qty: Double) = dao.increaseQuantity(batchId, qty)
    suspend fun getBatchWithItem(batchId: Long) = dao.getBatchWithItem(batchId)
    suspend fun deletePricing(pair: BatchPricingCrossRef) = dao.deletePricing(pair)
}

class CategoryRepository @Inject constructor(
    private val dao: CategoryDao,
    private  val supabaseClient: SupabaseClient
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = dao.getAllCategories()
    fun getAllCategoriesWithSubCategories(): Flow<List<CategoryWithSubCategories>> = dao.getAllCategoriesWithSubCategories()
    fun getCategoryBySlug(slug: String): Flow<CategoryEntity?> = dao.getCategoryBySlug(slug)
    fun getCategoryBySlugWithSubCategories(slug: String): Flow<CategoryWithSubCategories?> = dao.getCategoryBySlugWithSubCategories(slug)
    suspend fun insert(category: CategoryEntity) = dao.insert(category)
    suspend fun fetchCategoriesFromCloud(): List<SupabaseCategory> {
        return withContext(Dispatchers.IO) {
            val result = supabaseClient.from("categories")
                .select(Columns.list("name, slug, specs:default_specs, subCategories:sub_categories(name, slug, categorySlug:category_id, specs:default_specs)")).decodeList<SupabaseCategory>()
            result
        }
    }

}

class SubCategoryRepository @Inject constructor(
    private val dao: SubCategoryDao,
) {
    suspend fun insert(subCategory: SubCategory) = dao.insert(subCategory)
}

class ExpenseRepository @Inject constructor(
    private val dao: ExpenseDao
) {
    fun getExpenseById(id: Long): Flow<Expense?> = dao.getExpenseById(id)
    fun getAllStoreExpenses(storeId: Long): Flow<List<Expense>> = dao.getAllStoreExpenses(storeId)
    fun getAllStoreExpensesPerDay(storeId: Long, start: Long, end: Long): Flow<List<Expense>> = dao.getAllStoreExpensesPerDay(storeId, start, end)
    suspend fun insert(expense: Expense) = dao.insert(expense)
}

class NoteRepository @Inject constructor(
    private val dao: NoteDao
){
    fun getNotes(storeId: Long): Flow<List<NotesEntity>> = dao.getAllStoreNotes(storeId)
    suspend fun insert(note: NotesEntity): Long = dao.insert(note)
}

class CartItemRepository @Inject constructor(
    private val dao: CartItemDao
){
    fun getAllItems(): Flow<List<CartItem>> = dao.getAllItems()
    suspend fun insert(item: CartItemEntity): Long = dao.insert(item)
    suspend fun editItem(item: CartItemEntity) = dao.editItem(item)
    suspend fun deleteItem(item: CartItemEntity) = dao.deleteItem(item)
    suspend fun clearCart() = dao.clearCart()
}

class OrderRepository @Inject constructor(
    private val dao: OrderDao
){
    suspend fun insert(item: OrderEntity): Long = dao.insert(item)
    fun getAllOrders(storeId: Long): Flow<List<Order>> = dao.getStoreOrders(storeId)
    fun getOrderById(orderId: Long): Flow<Order> = dao.getOrderById(orderId)
    suspend fun updateCreditBalance(amount: Double, orderId: Long) = dao.updateCreditBalance(amount, orderId)
    suspend fun editOrder(item: OrderEntity) = dao.editOrder(item)
    suspend fun delete(orderId: Long) = dao.delete(orderId)
    suspend fun returnItems(items: List<OrderItemEntity>) = dao.returnItems(items)

}

class OrderItemRepository @Inject constructor(
    private val dao: OrderItemDao
){
    suspend fun insert(item: OrderItemEntity): Long = dao.insert(item)
}

class ClientRepository @Inject constructor(
    private val dao: ClientDao
){
    suspend fun insert(client: ClientEntity): Long = dao.insert(client)
    fun getClients(storeId: Long): Flow<List<ClientEntity>> = dao.getClients(storeId)
    fun getClientById(clientId: Long): Flow<ClientEntity?> = dao.getClientById(clientId)
    suspend fun editClient(client: ClientEntity) = dao.update(client)
    suspend fun deleteClient(client: ClientEntity) = dao.delete(client)
}

class StaffRepository @Inject constructor(
    private val dao: StaffDao
){
    suspend fun insert(staff: StaffEntity): Long = dao.insert(staff)
    fun getStaffs(storeId: Long): Flow<List<StaffEntity>> = dao.getStaffs(storeId)
    fun getStaffById(staffId: Long): Flow<StaffEntity?> = dao.getStaffById(staffId)
    suspend fun editStaff(staff: StaffEntity) = dao.update(staff)
    suspend fun deleteStaff(staff: StaffEntity) = dao.delete(staff)
}

class CreditPaymentRepository @Inject constructor(
    private val dao: CreditPaymentDao
){
    suspend fun insert(cp: CreditPayment): Long = dao.insert(cp)
    fun getCreditPayments(orderId: Long): Flow<List<CreditPayment>> = dao.getCreditPayments(orderId)
}
