package com.example.doline.views.screens.store.inventory


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.DeviceConfiguration
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.BatchPricingCrossRef
import com.example.doline.data.BatchRepository
import com.example.doline.data.BatchWithItem
import com.example.doline.data.ItemEntity
import com.example.doline.data.Pricing
import com.example.doline.data.PricingRepository
import com.example.doline.formatWithCommas
import com.example.doline.timestampToDate
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.onSuccessLight
import com.example.doline.ui.theme.successLight
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.ImageView
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.PriceTag
import com.example.doline.views.components.PricingForm
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
import com.example.doline.zeroed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(navController: NavController, viewmodel: BatchDetailsViewModel) {

    val rect = RoundedCornerShape(Rounding.SM)
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val columns = DeviceConfiguration.getGridWideColumnCount(deviceConfig)
    val uiState by viewmodel.uiState.collectAsState()
    val isBottomSheetOpen by viewmodel.isBottomSheetOpen.collectAsState()
    val prices by viewmodel.batchPrices.collectAsState()
    val batchId = viewmodel.batchId

    when(val state = uiState){
        is UiState.Loading -> {
            LoadingScreen()
        }
        is UiState.Error -> {
            ErrorMessage(state.message)
        }
        is UiState.Success -> {
            val batchWithItem = state.batchWithItem
            val item = batchWithItem.item
            Screen(
                topAppBar = {
                    TopAppBar(
                        title = { AppText("Batch Details", variant = TextType.Heading, maxLines = 1) },
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
                        }
                    )
                },
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    columns = GridCells.Fixed(columns),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XL),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.XL)
                ){
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(rect)
                                .border(
                                    2.dp,
                                    colorScheme.onBackground.copy(.2f),
                                    rect
                                )
                                .background(colorScheme.surface.copy(.5f))
                                .padding(26.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    AppText("BATCH NUMBER", variant = TextType.ExtraSMall)
                                    AppText("#BAT${batchWithItem.batch.id.zeroed()}", variant = TextType.Heading, color = colorScheme.primary)
                                }

                                Row(
                                    Modifier
                                        .wrapContentSize()
                                        .clip(RoundedCornerShape(Rounding.FULL))
                                        .background(successLight)
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(Rounding.FULL))
                                        .background(onSuccessLight))
                                    AppText("Arrived", color = onSuccessLight)
                                }

                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    AppText("BUYING PRICE", variant = TextType.ExtraSMall)
                                    AppText("UGX ${batchWithItem.batch.buyingPrice?.formatWithCommas()} for ${batchWithItem.batch.quantity.formatWithCommas()} ${batchWithItem.batch.units}", variant = TextType.Label)
                                }

                            }
                            Spacer(Modifier.height(20.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    AppText("SUPPLIER", variant = TextType.ExtraSMall)
                                    AppText("TWSA Trading Company LTD", variant = TextType.Label)
                                }

                                Column(modifier = Modifier.wrapContentSize()) {
                                    AppText("DATE RECEIVED", variant = TextType.ExtraSMall)
                                    AppText(timestampToDate(batchWithItem.batch.createdAt), variant = TextType.Label)
                                }
                            }
                        }
                    }
                    item {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(rect)
                                .border(2.dp, colorScheme.onBackground.copy(.2f), rect)
                                .background(colorScheme.surface.copy(.5f))
                                .padding(Spacing.MD),
                            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                AppText("Selling Prices", variant = TextType.Label)
                                IconButton(
                                    {viewmodel.onChangeSheetState(true)},
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = colorScheme.primaryContainer.copy(.5f)
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.plus),
                                        contentDescription = null,
                                        tint = colorScheme.primary
                                    )
                                }
                            }

                            if (isBottomSheetOpen && batchId != null) {
                                AddPriceBottomSheet(
                                    onDismiss = { viewmodel.onChangeSheetState(false) },
                                    item = item,
                                    viewModel = viewmodel
                                )
                            }

                            if (prices.isEmpty()){
                                AppText("The price list is empty!", variant = TextType.ExtraSMall)
                            }
                            FlowRow(
                                Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                                prices.forEach { p ->
                                    PrevPrice(p, viewmodel)
                                 }
                            }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(rect)
                                .border(
                                    2.dp,
                                    colorScheme.onBackground.copy(.2f),
                                    rect
                                )
                                .background(colorScheme.surface.copy(.5f))
                                .padding(26.dp),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            AppText("Tracking Status")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    AppText("TOTAL ITEMS STOCKED", variant = TextType.ExtraSMall)
                                    AppText("${batchWithItem.batch.quantity.formatWithCommas()} ${batchWithItem.batch.units}", variant = TextType.Label)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    AppText("REMAINING IN STOCK", variant = TextType.ExtraSMall)
                                    AppText("${batchWithItem.batch.available.formatWithCommas()} ${batchWithItem.batch.units}", variant = TextType.Label)
                                }
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    AppText("FIRST SALE", variant = TextType.ExtraSMall)
                                    AppText("24 Jun, 20206", variant = TextType.Label)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    AppText("LAST SALE", variant = TextType.ExtraSMall)
                                    AppText("24 Jun, 20206", variant = TextType.Label)
                                }
                            }
                        }
                    }
                    item {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                        ) {
                            ItemCard({}, item = batchWithItem.item)
                        }
                    }
                    item { Spacer(Modifier.height(50.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPriceBottomSheet(
    onDismiss: () -> Unit,
    item: ItemEntity,
    viewModel: BatchDetailsViewModel
) {
    val sheetState = rememberModalBottomSheetState()
    val prevPrices by viewModel.prevPrices.collectAsState()
    val pricings by viewModel.pricings.collectAsState()
    val loading by viewModel.loading.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(Spacing.MD),
            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
        ){
            PricingForm(
                item = item,
                pricings = pricings,
                onChange = {prs ->
                    viewModel.onChange(prs)
                },
                prevPricings = prevPrices,
            )
            AppButton(
                text = "Save Changes",
                onClick = {viewModel.saveChanges()},
                type = ButtonType.Primary,
                isLoading = loading
            )
        }
    }
}

@Composable
fun PrevPrice(pricing: Pricing, viewModel: BatchDetailsViewModel){
    val rect = RoundedCornerShape(Rounding.SM)
    var openDialog by remember { mutableStateOf(false) }
    Row(
        Modifier
            .wrapContentSize()
            .clip(rect)
            .background(colorScheme.primaryContainer.copy(.5f))
            .padding(start = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
        verticalAlignment = Alignment.CenterVertically,
    ){
        PriceTag(pricing)
        IconButton(
            onClick = { openDialog = true }
        ) {
            Icon(
                painter = painterResource(R.drawable.trash),
                contentDescription = null,
                tint = colorScheme.error
            )
        }
        if (openDialog) {
            AlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog if they tap outside or press back
                    openDialog = false
                },
                title = {
                    AppText(text = "Delete Pricing?")
                },
                text = {
                    AppText(text = "Are you sure you want to delete this pricing? This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDialog = false
                            viewModel.deletePricing(pricing.id)

                        }
                    ) {
                        AppText(text = "Delete", color = colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { openDialog = false } // Just close dialog
                    ) {
                        AppText(text = "Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ItemCard(onClick: ()-> Unit, item: ItemEntity){
    val rect = MaterialTheme.shapes.medium
    val image = item.images?.find { it.sortOrder == 0 }
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .clip(rect)
            .border(
                2.dp,
                colorScheme.onBackground.copy(.2f),
                rect
            )
            .background(colorScheme.surface.copy(.5f))
            .clickable { onClick() }
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .width(90.dp)
                .background(colorScheme.onBackground.copy(alpha = 0.4f)),
        ){
            image?.let {
                ImageView(it.url, modifier = Modifier.fillMaxSize())
            }
        }
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            AppText(item.name, variant = TextType.Label, maxLines = 1)
            AppText("upc: ${item.upc}", variant = TextType.LabelSmall, maxLines = 1, color = colorScheme.onBackground.copy(.5f))
            Spacer(Modifier.height(10.dp))
            AppText("${item.description}", variant = TextType.Small)
            Spacer(Modifier
                .height(10.dp)
                .weight(1f))
            AppText("${item.categorySlug.capitalize()} | ${item.subCategorySlug?.capitalize()}", variant = TextType.ExtraSMall)
        }
    }
}

@HiltViewModel
class BatchDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val batchRepository: BatchRepository,
    private val pricingRepository: PricingRepository
) : ViewModel() {
    val batchId = savedStateHandle.get<Long>("batchId")
    val itemId = savedStateHandle.get<Long>("itemId")

    init {
        viewModelScope.launch {
            fetchBatchWithItem()
            fetchAllPrices()
        }
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState
    private val _isBottomSheetOpen = MutableStateFlow(false)
    val isBottomSheetOpen: StateFlow<Boolean> = _isBottomSheetOpen

    private val _prevPrices = MutableStateFlow<List<Pricing>>(emptyList())
    val prevPrices: StateFlow<List<Pricing>> = _prevPrices
    private val _batchPricings = MutableStateFlow<List<Pricing>>(emptyList())
    val batchPrices: StateFlow<List<Pricing>> = _batchPricings
    private val _pricings = MutableStateFlow<List<Pricing>>(emptyList())
    val pricings: StateFlow<List<Pricing>> = _pricings
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun onChange(prs: List<Pricing>){
        _pricings.value = prs
    }
    fun onChangeSheetState(state: Boolean){
        _isBottomSheetOpen.value = state
    }

    fun saveChanges(){
        _loading.value = true
        if (batchId == null){
            _loading.value = false
            return
        }
        val ps = _pricings.value
        val pps = _prevPrices.value
        val bps = _batchPricings.value

        viewModelScope.launch {
            ps.forEach {p->
                when(p){
                    in bps ->{

                    }
                    in pps -> {
                        try {
                            val pair = BatchPricingCrossRef(batchId = batchId, pricingId = p.id)
                            batchRepository.insertBatchPricings(listOf(pair))
                            val newBps = _batchPricings.value.toMutableList()
                            newBps.add(p)
                            _batchPricings.value = newBps
                        }catch (e: Exception){ e.printStackTrace() }
                    }
                    else -> {
                        try {
                            val pid = pricingRepository.insertPricing(p)
                            val pair = BatchPricingCrossRef(batchId = batchId, pricingId = pid)
                            batchRepository.insertBatchPricings(listOf(pair))
                            val newBps = _batchPricings.value.toMutableList()
                            newBps.add(p)
                            _batchPricings.value = newBps
                        }catch (e: Exception){ e.printStackTrace() }
                    }
                }
            }
            _loading.value = false
            onChangeSheetState(false)
        }
    }

    private suspend fun fetchBatchWithItem(){
        val bid = batchId ?: return
        try {
            val b = batchRepository.getBatchWithItem(bid)
            if(b == null){
                _uiState.value = UiState.Error("Failed the get batch with id: $batchId")
            }else{
                _pricings.value = b.pricings
                _batchPricings.value = b.pricings
                _uiState.value = UiState.Success(b)
            }
        }catch (e: Exception){
            _uiState.value = UiState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchAllPrices(){
        itemId ?: return

        try {
            pricingRepository.getItemPricings(itemId).collect { prices ->
                _prevPrices.value = prices
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun deletePricing(pid: Long){
        batchId ?: return
        viewModelScope.launch {
            val pair = BatchPricingCrossRef(
                batchId, pid
            )
            try {
                batchRepository.deletePricing(pair)
                fetchBatchWithItem()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}

sealed class UiState {
    data object Loading: UiState()
    data class Success(val batchWithItem: BatchWithItem): UiState()
    data class Error(val message: String): UiState()
}
