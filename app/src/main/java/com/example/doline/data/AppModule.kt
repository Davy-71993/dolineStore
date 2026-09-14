package com.example.doline.data

import android.content.Context
import androidx.room.Room
import com.example.doline.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideUpcRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.upcitemdb.com/")   // Base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideUPCApiService(retrofit: Retrofit): UPCApiService {
        return retrofit.create(UPCApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ){
        install(Auth){
            // For Android deep links (email confirmation / future OAuth)
            flowType = FlowType.PKCE
            scheme = "doline"
            host = "auth"
        }
        install(Postgrest)
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DolineStoreDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            DolineStoreDatabase::class.java,
            "doline_database"
        )
            .addMigrations(*ALL_MIGRATIONS)
            .fallbackToDestructiveMigration(false) // Safety net only for versions with no migration path above
            .build()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: DolineStoreDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideStoreDao(database: DolineStoreDatabase): StoreDao {
        return database.storeDao()
    }

    @Provides
    @Singleton
    fun provideItemDao(database: DolineStoreDatabase): ItemDao {
        return database.itemDao()
    }

    @Provides
    @Singleton
    fun providePricingDao(database: DolineStoreDatabase): PricingDao {
        return database.pricingDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: DolineStoreDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideSubCategoryDao(database: DolineStoreDatabase): SubCategoryDao {
        return database.subCategoryDao()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: DolineStoreDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideBatchDao(database: DolineStoreDatabase): BatchDao {
        return database.batchDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: DolineStoreDatabase): NoteDao {
        return  database.noteDao()
    }

    @Provides
    @Singleton
    fun provideCartItemDao(database: DolineStoreDatabase): CartItemDao {
        return  database.cartItemDao()
    }

    @Provides
    @Singleton
    fun provideOrderDao(database: DolineStoreDatabase): OrderDao {
        return  database.orderDao()
    }

    @Provides
    @Singleton
    fun provideOrderItemDao(database: DolineStoreDatabase): OrderItemDao {
        return  database.orderItemDao()
    }

    @Provides
    @Singleton
    fun provideClientDao(database: DolineStoreDatabase): ClientDao {
        return  database.clientDao()
    }

    @Provides
    @Singleton
    fun provideStaffDao(database: DolineStoreDatabase): StaffDao {
        return  database.staffDao()
    }

    @Provides
    @Singleton
    fun provideCreditBalanceDao(database: DolineStoreDatabase): CreditPaymentDao {
        return  database.creditPaymentDao()
    }

    @Provides
    @Singleton
    fun provideUserProfileRepository(dao: UserProfileDao, supabaseClient: SupabaseClient) = UserProfileRepository(dao, supabaseClient)

    @Provides
    @Singleton
    fun provideAuthRepository(auth: Auth) = AuthRepository(auth)

    @Provides
    @Singleton
    fun provideStoreRepository(dao: StoreDao, supabaseClient: SupabaseClient) = StoreRepository(dao, supabaseClient)

    @Provides
    @Singleton
    fun provideItemRepository(dao: ItemDao, upcApiService: UPCApiService) = ItemRepository(dao, upcApiService)

    @Provides
    @Singleton
    fun providePricingRepository(dao: PricingDao) = PricingRepository(dao)

    @Provides
    @Singleton
    fun provideCategoryRepository(dao: CategoryDao, supabaseClient: SupabaseClient) = CategoryRepository(dao, supabaseClient)

    @Provides
    @Singleton
    fun provideExpenseRepository(dao: ExpenseDao) = ExpenseRepository(dao)

    @Provides
    @Singleton
    fun provideBatchRepository(dao: BatchDao) = BatchRepository(dao)

    @Provides
    @Singleton
    fun provideCartItemRepository(dao: CartItemDao) = CartItemRepository(dao)

    @Provides
    @Singleton
    fun provideOrderRepository(dao: OrderDao) = OrderRepository(dao)

    @Provides
    @Singleton
    fun provideOrderItemRepository(dao: OrderItemDao) = OrderItemRepository(dao)

    @Provides
    @Singleton
    fun provideClientRepository(dao: ClientDao) = ClientRepository(dao)

    @Provides
    @Singleton
    fun provideStaffRepository(dao: StaffDao) = StaffRepository(dao)

    @Provides
    @Singleton
    fun provideCreditBalanceRepository(dao: CreditPaymentDao) = CreditPaymentRepository(dao)
}