package com.example.doline.views.screens.store.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.DeviceConfiguration
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.ItemWithBatches
import com.example.doline.data.Store
import com.example.doline.data.StoreRepository
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.EmptyMessage
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.ImageView
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.PricingsTag
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inventory(navController: NavController, viewModel: InventoryViewModel){

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val columns = DeviceConfiguration.getGridColumnCount(deviceConfig)

    val uiState by viewModel.uiState.collectAsState()
    val store by viewModel.store.collectAsState()
    val items by viewModel.filteredItems.collectAsState()
    val filterBy by viewModel.filterBy.collectAsState(FilterBy.ALL)
    val storeId = viewModel.storeId

    LaunchedEffect(filterBy) {
        viewModel.filter(filterBy)
    }

    Screen (
        topAppBar = {
            TopAppBar(
                title = { AppText(store?.name ?: "Loading...", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier
                    .padding(vertical = 0.dp)
                    .shadow(10.dp),
                colors = TopAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground,
                    subtitleContentColor = colorScheme.onBackground,
                ),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = "Search",
                            modifier = Modifier.size(IconSize.BIG),
                            tint = colorScheme.onBackground
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("${storeId}/inventory/new-item")},
                containerColor = colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    8.dp,
                    6.dp
                ),
                shape = RoundedCornerShape(Rounding.FULL),
                modifier = Modifier
                    .padding(Spacing.SM)
                    .absoluteOffset(y = (-60).dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = "Add new item",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(IconSize.BIG)
                )
            }
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
        ) {
            FilterBy.entries.forEach { entry ->
                FilterBtn({viewModel.onFilterChange(entry)}, entry.name, filterBy)
            }
        }
        when (val state = uiState) {
            is InventoryScreenUiState.Loading -> {
                LoadingScreen()
            }

            is InventoryScreenUiState.Success -> {
                if (items.isEmpty()) {
                    EmptyMessage("No items in inventory yet.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorScheme.background)
                            .padding(bottom = 80.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items.size) { index ->
                            val item = items[index]
                            InventoryItemCard({ navController.navigate("${storeId}/inventory/${item.item.id}") },item)
                        }
                    }
                }
            }

            is InventoryScreenUiState.Error -> {
                ErrorMessage(message = state.message)
            }
        }
    }
}

@Composable
fun InventoryItemCard( onClick: () -> Unit, record: ItemWithBatches) {
    val rect = MaterialTheme.shapes.medium
    val qty = record.batches.sumOf { it.batch.available }
    val image = record.item.images?.find { it.sortOrder == 0 }
    val pricings = setOf(record.batches.flatMap { it.pricings }).toList().flatten()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rect)
            .clickable { onClick() }
            .background(colorScheme.surface.copy(.6f), rect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(90.dp)
                .height(IntrinsicSize.Min)
                .background(colorScheme.onBackground.copy(alpha = 0.4f)),
        ){
            image?.let {
                ImageView(
                    it.url,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Column(
            Modifier
                .weight(1f)
                .height(IntrinsicSize.Min)
                .padding(10.dp)
        ) {
            AppText(record.item.name, variant = TextType.Label, maxLines = 1)
            Spacer(Modifier.height(5.dp))
            if(pricings.isEmpty()){
                AppText("No prices set!", variant = TextType.LabelSmall, color = colorScheme.primary)
            }else{
                PricingsTag(pricings)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.orders),
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.SMALL),
                    tint = colorScheme.onBackground.copy(.8f)
                )
                AppText(
                    "$qty ${record.item.sku?.capitalize()} in stock",
                    variant = TextType.Small,
                    color = colorScheme.onBackground.copy(.8f)
                )
            }
        }
    }
}

@Composable
fun FilterBtn(onClick: () -> Unit, option: String, filter: FilterBy = FilterBy.ALL ){
    TextButton(
        onClick = onClick,
        colors = ButtonColors(
            contentColor = if(option == filter.name) colorScheme.onPrimary else colorScheme.onSurface,
            containerColor = if(option == filter.name) colorScheme.primary else colorScheme.surface,
            disabledContentColor = colorScheme.onSurface,
            disabledContainerColor = colorScheme.surface
        )
    ) {
        AppText(
            option.replace("_", " ").capitalize(),
            variant = TextType.Small,
            color = if(option == filter.name) colorScheme.onPrimary else colorScheme.onSurface
        )
    }
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storeRepository: StoreRepository
) : ViewModel() {
    val storeId = savedStateHandle.get<Long>("storeId")
    private val _uiState = MutableStateFlow<InventoryScreenUiState>(InventoryScreenUiState.Loading)
    val uiState: StateFlow<InventoryScreenUiState> = _uiState.asStateFlow()

    private val _store = MutableStateFlow<Store?>(null)
    val store: StateFlow<Store?> = _store.asStateFlow()

    private val _items = MutableStateFlow<List<ItemWithBatches>>(emptyList())
    val items: StateFlow<List<ItemWithBatches>> = _items
    private val _searchText = MutableStateFlow("")
    val searchText: Flow<String> = _searchText
    private val _filteredBy = MutableStateFlow(FilterBy.ALL)
    val filterBy: Flow<FilterBy> = _filteredBy
    private val _filteredItems = MutableStateFlow(_items.value)
    val filteredItems: StateFlow<List<ItemWithBatches>> = _filteredItems

    init {
        fetchItems()
    }

    fun filter(filter: FilterBy) {
        when(filter){
            FilterBy.ALL -> {
                _filteredItems.value = _items.value
            }
            FilterBy.IN_STOCK -> {
                _filteredItems.value = _items.value.filter { item ->
                    val qty = item.batches.sumOf { it.batch.quantity }
                    qty > 5
                }
            }
            FilterBy.LOW_STOCK -> {
                _filteredItems.value = _items.value.filter { item ->
                    val qty = item.batches.sumOf { it.batch.quantity }
                    qty <= 5
                }
            }
            FilterBy.OUT_OF_STOCK -> {
                _filteredItems.value = _items.value.filter { item ->
                    val qty = item.batches.sumOf { it.batch.quantity }
                    qty == 0.0
                }
            }
        }
    }

    fun onFilterChange(filter: FilterBy){
        _filteredBy.value = filter
    }
    fun fetchItems() = viewModelScope.launch {
        if (storeId == null){
            _uiState.value = InventoryScreenUiState.Error("The store ID can not be null.")
            return@launch
        }
        try {
            storeRepository.getStoreWithItems(storeId).collect {
                if(it == null){
                    _uiState.value = InventoryScreenUiState.Error("There was no data found for storeId, $storeId")
                    return@collect
                }
                _store.emit(it.store)
                _items.emit(it.items)
                _filteredItems.emit(it.items)

                _uiState.value = InventoryScreenUiState.Success
            }
        }catch (e: Exception){
            _uiState.value = InventoryScreenUiState.Error(e.message ?: "Failed to fetch store items")
        }
    }
    private fun onSearch(text: String){
        _filteredItems.value = _items.value.filter {
            it.item.upc?.contains(text) == true ||
                    it.item.name.lowercase().contains(text.lowercase()) ||
                    it.item.description?.lowercase()?.contains(text.lowercase()) == true ||
                    it.item.categorySlug.lowercase().contains(text.lowercase()) ||
                    it.item.subCategorySlug?.lowercase()?.contains(text.lowercase()) == true
        }
    }
    fun handleSearch(text: String){
        _searchText.value = text
        onSearch(text)
    }
}

sealed class InventoryScreenUiState {
    data object Loading : InventoryScreenUiState()
    data object Success: InventoryScreenUiState()
    data class Error(val message: String) : InventoryScreenUiState()
}

enum class FilterBy {
    ALL, IN_STOCK, LOW_STOCK, OUT_OF_STOCK
}