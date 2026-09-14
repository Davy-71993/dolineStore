package com.example.doline.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey val userId: String,
    val email: String? = null,
    val username: String? = null,
    val fullNames: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val about: String? = null,
    val defaultAddress: String? = null
)

@Serializable
@Entity(tableName = "stores")
data class Store(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cloudId: Long? = null,
    @param:TypeConverters(StoreCategoryConverter::class)
    val category: StoreCategory = StoreCategory.DOLINE_STORE,
    val image: String? = null,
    val name: String,
    val description: String,
    val address: String? = null,
    val status: String? = null,
    val logo: String? = null,
    val slug: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class StoreWithItems(
    @Embedded val store: Store,
    @Relation(
        entity = ItemEntity::class,
        parentColumn = "id",
        entityColumn = "storeId"
    )
    val items: List<ItemWithBatches>
)

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeId: Long,
    val name: String,
    @param:TypeConverters(SpecsConverter::class)
    val specs: Map<String, Any?>? = null,
    @param:TypeConverters(ItemImageConverter::class)
    val images: List<ItemImage>? = null,
    val categorySlug: String,
    val subCategorySlug: String? = null,
    val description: String? = null,
    val quantity: Double? = 0.toDouble(),
    val sku: String? = null,
    val upc: String? = null,
    @param:TypeConverters(PricingSchemeConverter::class)
    val pricingScheme: PricingScheme,
    @param:TypeConverters(DeterminantsConverter::class)
    val priceDeterminants: Map<String, List<String>?>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ItemWithBatches(
    @Embedded val item: ItemEntity,
    @Relation(
        entity = BatchEntity::class,
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val batches: List<BatchWithPricings>
)

data class BatchWithItem(
    @Embedded
    val batch: BatchEntity,   // or whatever your Batch entity class is named

    @Relation(
        parentColumn = "itemId",     // Foreign key column in Batch table
        entityColumn = "id"          // Primary key column in ItemEntity
    )
    val item: ItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BatchPricingCrossRef::class,
            parentColumn = "batchId",
            entityColumn = "pricingId"
        )
    )
    val pricings: List<Pricing>
)

@Entity(
    tableName = "pricings",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Pricing @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val amount: Double,
    val discount: Double? = null,
    @param:TypeConverters(CurrencyConverter::class)
    val currency: Currency,
    @param:TypeConverters(PricingDetailsConverter::class)
    val details: PricingDetails? = null,
    @param:TypeConverters(FlashPricingConverter::class)
    val flashSale: FlashPricing? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "batches",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchNumber: String,
    val itemId: Long,
    val quantity: Double,
    val available: Double,
    val units: String,
    val buyingPrice: Double? = null,
    val expiryDate: Long? = null,
    val manufactureDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "batch_pricing_cross_ref",
    primaryKeys = ["batchId", "pricingId"]
)
data class BatchPricingCrossRef(
    val batchId: Long,
    val pricingId: Long
)

data class BatchWithPricings(
    @Embedded val batch: BatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BatchPricingCrossRef::class,
            parentColumn = "batchId",
            entityColumn = "pricingId"
        )
    )
    val pricings: List<Pricing>
)

@Serializable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val slug: String,
    val name: String,
    val specs: String? = null
)

@Serializable
@Entity(
    tableName = "sub_categories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["slug"],
            childColumns = ["categorySlug"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SubCategory(
    @PrimaryKey val slug: String,
    val name: String,
    val categorySlug: String,
    val specs: String? = null
)

@Serializable
data class CategoryWithSubCategories(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "slug",
        entityColumn = "categorySlug"
    )
    val subCategories: List<SubCategory>
)

@Serializable
data class  SupabaseCategory(
    val slug: String,
    val name: String,
    val specs: String? = null,
    val subCategories: List<SubCategory>
)

@Entity("expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long,
    val category: ExpenseCategory,
    val description: String,
    val amount: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val cloudId: Long? = null
)

@Entity("notes")
data class NotesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity("cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    @param:TypeConverters(CartItemConverters::class)
    val batchesDetails: Map<String, Double> = emptyMap(),
    val qty: Double,
    val pricingId: Long,
    @param:TypeConverters(CartItemConverters::class)
    val specs: Map<String, Any>? = null,
    val maxQty: Double = 0.0
)

data class CartItem(
    @Embedded
    val cartItem: CartItemEntity,

    @Relation(
        parentColumn = "itemId",
        entityColumn = "id"
    )
    val item: ItemEntity,

    @Relation(
        parentColumn = "pricingId",
        entityColumn = "id"
    )
    val pricing: Pricing,

)

@Entity("orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long,
    val progress: OrderProgress = OrderProgress.PENDING,
    val status: OrderStatus,
    val amountReceived: Double? = null,
    val creditBalance: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clientId: Long? = null,
    val staffId: Long? = null,
    val deletedAt: Long? = null,
)

@Entity("order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val itemId: Long,
    @param:TypeConverters(CartItemConverters::class)
    val batchesDetails: Map<String, Double> = emptyMap(),
    val qty: Double,
    val returned: Double = 0.0,
    val pricingId: Long,
    @param:TypeConverters(CartItemConverters::class)
    val specs: Map<String, Any>? = null,
)

data class Order(
    @Embedded
    val fields: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId",
        entity = CreditPayment::class
    )
    val creditPayments: List<CreditPayment>,

    @Relation(
        parentColumn = "id",
        entityColumn = "orderId",
        entity = OrderItemEntity::class
    )
    val items: List<OrderItem>,

    @Relation(
        parentColumn = "clientId",
        entityColumn = "id",
        entity = ClientEntity::class
    )
    val client: ClientEntity?,

    @Relation(
        parentColumn = "staffId",
        entityColumn = "id",
        entity = StaffEntity::class
    )
    val staff: StaffEntity?,
)

data class OrderItem(
    @Embedded
    val fields: OrderItemEntity,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "id",
        entity = ItemEntity::class
    )
    val item: ItemEntity,
    @Relation(
        parentColumn = "pricingId",
        entityColumn = "id",
        entity = Pricing::class
    )
    val pricing: Pricing
)

@Entity("clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long,
    val name: String,
    val address: String?,
    val phone: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity("staffs")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long,
    val name: String,
    val role: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity("credit_payments")
data class CreditPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val amount: Double,
    val createdAt: Long = System.currentTimeMillis()
)





