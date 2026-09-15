package com.example.doline.views.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.data.AuthRepository
import com.example.doline.data.CategoryEntity
import com.example.doline.data.CategoryRepository
import com.example.doline.data.StoreRepository
import com.example.doline.data.SubCategory
import com.example.doline.data.SubCategoryRepository
import com.example.doline.data.SupabaseCategory
import com.example.doline.data.UserProfileRepository
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun InitializationScreen(
    navController: NavController,
    viewModel: InitializationScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    when(val state = uiState){

        is InitializationScreenUiState.Error -> {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                AppText(
                    state.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.shapes.medium
                        )
                        .padding(Spacing.MD)
                )
            }
        }

        else -> {

        }
    }

    if(uiState is InitializationScreenUiState.Success){
        navController.navigate("stores"){
            popUpTo("initialization") { inclusive = true  }
        }
    }
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@HiltViewModel
class InitializationScreenViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val profileRepository: UserProfileRepository,
    private val storeRepository: StoreRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InitializationScreenUiState>(InitializationScreenUiState.Loading)
    val uiState: StateFlow<InitializationScreenUiState> = _uiState.asStateFlow()

    init {
        val userId = authRepository.currentUser?.id

        if (userId == null) {
            _uiState.value = InitializationScreenUiState.Error("User not authenticated")
        }else{
            initializeData(userId)
        }
    }

    private fun initializeData(userId: String) {
        viewModelScope.launch {
            try {
                // Pull profile/stores from Supabase and upsert them locally (matched by their
                // cloud id, so re-running this doesn't pile up duplicates), and fetch categories.
                // Run all three in parallel and wait for them to complete.
                val profileDeferred = async { profileRepository.pull(userId) }
                val storesDeferred = async { storeRepository.pull(userId) }
                val categoriesDeferred = async { fetchCategoriesFromCloud() }

                profileDeferred.await()
                storesDeferred.await()
                val categories = categoriesDeferred.await()

                categories.forEach { c->
                    categoryRepository.insert(
                        CategoryEntity(
                            slug = c.slug,
                            name = c.name,
                            specs = c.specs
                        )
                    )

                    c.subCategories.forEach { subCategory ->
                        subCategoryRepository.insert(
                            SubCategory(
                                subCategory.slug,
                                subCategory.name,
                                c.slug,
                                subCategory.specs
                            )
                        )
                    }
                }

                _uiState.value = InitializationScreenUiState.Success

            } catch (e: Exception) {
                _uiState.value = InitializationScreenUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun fetchCategoriesFromCloud(): List<SupabaseCategory>{
        return categoryRepository.fetchCategoriesFromCloud()
    }
}

sealed class InitializationScreenUiState {
    data object Loading: InitializationScreenUiState()
    data object Success: InitializationScreenUiState()
    data class Error(val message: String): InitializationScreenUiState()
}