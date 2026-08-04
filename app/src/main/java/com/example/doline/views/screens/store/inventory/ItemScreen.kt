package com.example.doline.views.screens.store.inventory



import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.DeviceConfiguration
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.ItemEntity
import com.example.doline.data.ItemImage
import com.example.doline.data.ItemRepository
import com.example.doline.data.ItemWithBatches
import com.example.doline.data.Pricing
import com.example.doline.formatWithCommas
import com.example.doline.timestampToDate
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.ImageView
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.PriceTag
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(navController: NavController, viewModel: ItemViewModel){
    val uiState by viewModel.uiState.collectAsState()
    val storeId = viewModel.storeId
    val itemWithBatches by viewModel.item.collectAsState(null)
    val batches = itemWithBatches?.batches
    val item = itemWithBatches?.item ?: return
    val currentBatch = batches?.find { it.batch.createdAt == batches.maxOf { b -> b.batch.createdAt }  }
    val pricings = currentBatch?.pricings

    var expanded by remember {
        mutableStateOf(false)
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val columns = DeviceConfiguration.getGridWideColumnCount(deviceConfig)

    Screen(
        topAppBar = {
            TopAppBar(
                title = { AppText(itemWithBatches?.item?.name ?: "Loading...", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier
                    .padding(vertical = 0.dp)
                    .shadow(10.dp),
                colors = TopAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground,
                    subtitleContentColor = colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(IconSize.NORMAL),
                            tint = colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {expanded = true}) {
                        Icon(
                            painter = painterResource(R.drawable.more),
                            contentDescription = "Menu",
                            modifier = Modifier.size(IconSize.BIG)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        AppText("Actions", variant = TextType.Label, modifier = Modifier.padding(horizontal = 16.dp))
                        DropdownMenuItem(
                            text = { AppText("Re-Stock") },
                            onClick = {
                                expanded = false
                                navController.navigate("$storeId/inventory/restock/${item.id}")
                            },
                            leadingIcon = {
                                Icon(painter = painterResource(R.drawable.orders), contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { AppText("Edit Item") },
                            onClick = {
                                expanded = false
                                navController.navigate("${item.storeId}/inventory/edit-item/${item.id}")
                            },
                            leadingIcon = {
                                Icon(painter = painterResource(R.drawable.edit), contentDescription = null)
                            }
                        )
                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(20.dp))
                        AppText("Promotions", variant = TextType.Label, modifier = Modifier.padding(horizontal = 16.dp))
                        DropdownMenuItem(
                            text = { AppText("Flash Sale") },
                            onClick = {
                                expanded = false

                            },
                            leadingIcon = {
                                Icon(painter = painterResource(R.drawable.clock), contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { AppText("Offer Discount") },
                            onClick = {
                                expanded = false

                            },
                            leadingIcon = {
                                Icon(painter = painterResource(R.drawable.tag), contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { AppText("Advertise") },
                            onClick = {
                                expanded = false

                            },
                            leadingIcon = {
                                Icon(painter = painterResource(R.drawable.marketing), contentDescription = null)
                            }
                        )
                    }
                }
            )
        }
    ) {
        when(val state = uiState){
            is ItemUiState.Loading -> {
                LoadingScreen()
            }
            is ItemUiState.Error -> {
                ErrorMessage(state.message)
            }
            is ItemUiState.Success -> {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        ItemHero(item, columns, pricings, navController)
                    }
                    item {
                        batches?.let { b ->
                            b.size.let {
                                if(it > 0){
                                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(
                                        Spacing.XXS)) {
                                        AppText("Batches & Stock", variant = TextType.LabelSmall)
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = Rounding.SM,
                                                            topEnd = Rounding.SM
                                                        )
                                                    )
                                                    .background(colorScheme.onSurface.copy(.2f))
                                            ) {
                                                Box(modifier = Modifier
                                                    .weight(1f)
                                                    .padding(Spacing.XS), contentAlignment = Alignment.Center){AppText(variant = TextType.Small, text = "Bat NO", maxLines = 1)}
                                                Box(modifier = Modifier
                                                    .weight(1.6f)
                                                    .padding(Spacing.XS), contentAlignment = Alignment.Center){AppText(variant = TextType.Small, text = "Date", maxLines = 1)}
                                                Box(modifier = Modifier
                                                    .weight(1f)
                                                    .padding(Spacing.XS), contentAlignment = Alignment.Center){AppText(variant = TextType.Small, text = "Stocked", maxLines = 1)}
                                                Box(modifier = Modifier
                                                    .weight(1f)
                                                    .padding(Spacing.XS), contentAlignment = Alignment.Center){AppText(variant = TextType.Small, text = "Available", maxLines = 1)}
                                            }
                                            batches.sortedByDescending { b -> b.batch.createdAt }.forEach { b->
                                                val index = batches.indexOf(b)
                                                val rounding = if(index == 0)  Rounding.SM else 0.dp
                                                val modulus = index % 2
                                                val color = if(modulus == 0) colorScheme.onSurface.copy(.1f) else colorScheme.surface.copy(.6f)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(
                                                            RoundedCornerShape(
                                                                bottomEnd = rounding,
                                                                bottomStart = rounding
                                                            )
                                                        )
                                                        .background(color)
                                                        .clickable { navController.navigate("$storeId/inventory/${item.id}/batch-details/${b.batch.id}") }
                                                ) {
                                                    Box(modifier = Modifier
                                                        .weight(1f)
                                                        .padding(Spacing.XS), contentAlignment = Alignment.Center){AppText(variant = TextType.Small, text = b.batch.batchNumber, maxLines = 1)}
                                                    Box(modifier = Modifier
                                                        .weight(1.6f)
                                                        .padding(Spacing.XS), contentAlignment = Alignment.Center){AppText(variant = TextType.Small, text = timestampToDate(b.batch.createdAt), maxLines = 1)}
                                                    Box(modifier = Modifier
                                                        .weight(1f)
                                                        .padding(Spacing.XS), contentAlignment = Alignment.Center){AppText(variant = TextType.Small, text = b.batch.quantity.formatWithCommas(), maxLines = 1)}
                                                    Box(modifier = Modifier
                                                        .weight(1f)
                                                        .padding(Spacing.XS), contentAlignment = Alignment.Center){AppText(variant = TextType.Small, text = b.batch.available.formatWithCommas(), maxLines = 1)}
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(60.dp))}
                }
            }
        }
    }
}

@Composable
fun ItemHero(item: ItemEntity, columns: Int, pricings: List<Pricing>?, navController: NavController){
    if (columns == 1) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
        ) {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ItemImages(item.images ?: emptyList())
            }
            Column(Modifier.fillMaxWidth()) {
                ItemDetails(item, pricings, navController)
            }
        }
    } else {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                Modifier.widthIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ItemImages(item.images ?: emptyList())
            }
            Column(Modifier.weight(1f)) {
                ItemDetails(item, pricings, navController)
            }
        }
    }
}

@Composable
fun ItemImages(images: List<ItemImage>){

    val rect = MaterialTheme.shapes.medium
    val heroImage = images.find { it.sortOrder == 0 }

    Column(
        modifier = Modifier
            .wrapContentSize()
            .widthIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
    ) {
        heroImage?.let {
            ImageView(
                heroImage.url,
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Rounding.MD))
            )
        }
    }
}

@Composable
fun ItemDetails(item: ItemEntity, pricings: List<Pricing>?, navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
    ) {
        Spacer(Modifier.weight(1f))
        Column(modifier = Modifier.fillMaxWidth()) {
            AppText("Description", variant = TextType.LabelSmall)
            HorizontalDivider(color = colorScheme.onBackground.copy(.2f))
            Spacer(Modifier.height(5.dp))
            AppText(item.description ?: "No description.", variant = TextType.Small, color = colorScheme.onBackground.copy(.7f))
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            AppText("Category", variant = TextType.LabelSmall)
            HorizontalDivider(color = colorScheme.onBackground.copy(.2f))
            Spacer(Modifier.height(5.dp))
            AppText("${item.categorySlug.capitalize()} > ${item.subCategorySlug?.capitalize()}", variant = TextType.Small, color = colorScheme.onBackground.copy(.7f))
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            AppText("Universal Product Code", variant = TextType.LabelSmall)
            HorizontalDivider(color = colorScheme.onBackground.copy(.2f))
            Spacer(Modifier.height(5.dp))
            AppText(item.upc ?: "No UPC provided.", variant = TextType.Small, color = colorScheme.onBackground.copy(.7f))
        }
        Column(Modifier.fillMaxWidth()) {
            AppText("Prices", variant = TextType.LabelSmall)
            HorizontalDivider(color = colorScheme.onBackground.copy(.2f))
            Spacer(Modifier.height(5.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
            ) {
                if(pricings.isNullOrEmpty()){
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        AppText(
                            "There are no active prices set for this item.",
                            textAlign = TextAlign.Center,
                            variant = TextType.Small,
                            color = colorScheme.onBackground.copy(.7f)
                        )
                        TextButton(
                            {navController.navigate("${item.storeId}/inventory/restock/${item.id}")}
                        ) { AppText("Restock now!", color = colorScheme.primary) }
                    }
                }else{
                    pricings.forEach { p ->
                        Box(
                            modifier = Modifier
                                .wrapContentSize()
                                .clip(RoundedCornerShape(Rounding.SM))
                                .background(colorScheme.primary.copy(.1f))
                                .padding(
                                    horizontal = Spacing.SM,
                                    vertical = Spacing.XXS
                                )
                        ){
                            PriceTag(p,)
                        }
                    }
                }

            }
        }
    }
}

@HiltViewModel
class ItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository
) : ViewModel() {

    val itemId = savedStateHandle.get<Long>("itemId")
    val storeId = savedStateHandle.get<Long>("storeId")
    private val _uiState = MutableStateFlow<ItemUiState>(ItemUiState.Loading)
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()
    private val _item = MutableStateFlow<ItemWithBatches?>(null)
    val item: Flow<ItemWithBatches?> = _item

    init {
        loadItem()
    }
    private  fun loadItem() {
        viewModelScope.launch {
            _uiState.value = ItemUiState.Loading
            if (itemId == null){
                _uiState.value = ItemUiState.Error("The item id is null")
                return@launch
            }
            try {
                // Using Flow from repository (recommended)
                itemRepository.getItemById(itemId)
                    .catch { e ->
                        _uiState.value = ItemUiState.Error(
                            e.message ?: "Failed to load item"
                        )
                    }
                    .collect { itemWithBatches ->
                        _uiState.value = if (itemWithBatches != null) {
                            _item.value = itemWithBatches
                            ItemUiState.Success
                        } else {
                            ItemUiState.Error("Item not found")
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = ItemUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

}

sealed class  ItemUiState {
    data object Loading: ItemUiState()
    data object Success: ItemUiState()
    data class Error(val message: String): ItemUiState()
}