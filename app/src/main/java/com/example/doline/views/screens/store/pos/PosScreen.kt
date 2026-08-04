package com.example.doline.views.screens.store.pos


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.DeviceConfiguration
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.CartItemEntity
import com.example.doline.data.CartItemRepository
import com.example.doline.data.Currency
import com.example.doline.data.ItemEntity
import com.example.doline.data.ItemRepository
import com.example.doline.data.ItemWithBatches
import com.example.doline.data.Pricing
import com.example.doline.data.PricingDetails
import com.example.doline.data.PricingScheme
import com.example.doline.prepareBatchDetails
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.DeviceSize
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.FixedPriceAddToCartForm
import com.example.doline.views.components.IconButtonWithBadge
import com.example.doline.views.components.ImageView
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.PriceRangeAddToCartForm
import com.example.doline.views.components.PriceTag
import com.example.doline.views.components.PricingsTag
import com.example.doline.views.components.RecurringAddToCartForm
import com.example.doline.views.components.Screen
import com.example.doline.views.components.ShoppingCart
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import com.example.doline.views.components.UnitPriceAddToCartForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(navController: NavController, viewModel: PosScreenViewModel){
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartTotal = cartItems.sumOf { ci -> (ci.qty * ci.pricing.amount) }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val columns = DeviceConfiguration.getGridColumnCount(deviceConfig)
    val showSidePane = deviceConfig == DeviceConfiguration.TABLET_LANDSCAPE

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState= drawerState,
            gesturesEnabled = false,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        drawerContainerColor = colorScheme.background,
                        drawerTonalElevation = Spacing.MD,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ShoppingCart(
                            {},
                            {scope.launch { drawerState.close() }},
                            {item, index ->
                                viewModel.editCartItem(item, index)
                            },
                            { item ->
                                viewModel.deleteFromCart(item)
                            },
                            {
                                viewModel.clearCart()
                            },
                            cartItems,
                            DeviceSize.MOBILE
                        )
                    }
                }
            }){
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Screen(
                    topAppBar = {
                        TopAppBar(
                            title = { AppText("Point Of Sale", variant = TextType.Heading, maxLines = 1) },
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
                                if(!showSidePane){
                                    IconButtonWithBadge(
                                        onClick = {
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        },
                                        iconSize = IconSize.BIG,
                                        contentDescription = "shopping cart",
                                        icon = painterResource(R.drawable.cart),
                                        badgeCount = cartItems.size,
                                        badgeColor = colorScheme.surface
                                    )
                                }

                            }
                        )
                    },
                    floatingActionButton = {
                        if (!showSidePane){
                            FloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                },
                                containerColor = colorScheme.primary,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    8.dp,
                                    6.dp
                                ),
                                shape = RoundedCornerShape(Rounding.SM),
                                modifier = Modifier
                                    .padding(Spacing.SM)
                                    .wrapContentWidth()
                                    .widthIn(max = 400.dp)
                                    .absoluteOffset(y = (-60).dp, x = 18.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    AppText(
                                        "Total Amount: ",
                                        color=colorScheme.onPrimary,
                                        variant = TextType.LabelSmall
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    PriceTag(
                                        Pricing(itemId = 0, amount = cartTotal, currency = Currency.UGX),
                                        textColor = colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                ) {
                    if (showSidePane){
                        Row(
                            Modifier.fillMaxWidth()
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .weight(1f),
                            ) {
                                ItemsPane(
                                    uiState,
                                    columns,
                                    { item ->
                                        viewModel.addToCart(item)
                                    },
                                    navController
                                )
                            }
                            Row(
                                Modifier
                                    .fillMaxHeight()
                                    .widthIn(max = 350.dp)
                                ) {
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.2f)
                                                )
                                            )
                                        )
                                )
                                ShoppingCart(
                                    {},
                                    {scope.launch { drawerState.close() }},
                                    {item, index ->
                                        viewModel.editCartItem(item,index )
                                    },
                                    {item ->
                                        viewModel.deleteFromCart(item)
                                    },
                                    {
                                        viewModel.clearCart()
                                    },
                                    cartItems,
                                    DeviceSize.TABLET
                                )
                            }
                        }
                    }else{
                        ItemsPane(
                            uiState,
                            columns,
                            { item ->
                                viewModel.addToCart(item)
                            },
                            navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemsPane(uiState: ScreenUiState, columns: Int, onAddToCart: (item: CartItemEntity) -> Unit, navController: NavController){
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = Spacing.MD)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = if(columns > 1) Spacing.MD else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton({}) {
                Icon(
                    painter = painterResource(R.drawable.filter),
                    contentDescription = "filters",
                    modifier = Modifier.size(IconSize.NORMAL),
                    tint = colorScheme.onBackground
                )
                if (columns > 1){
                    Spacer(Modifier.width(5.dp))
                    AppText("Filters", variant = TextType.Label)
                }
            }
            if (columns > 1){
                Spacer(Modifier.width(20.dp))
            }
            Box(Modifier.weight(1f)){
                TextInputField("", {})
            }
            if (columns > 1){
                Spacer(Modifier.width(20.dp))
            }
            TextButton({}) {
                Icon(
                    painter = painterResource(R.drawable.scanner),
                    contentDescription = "scanner",
                    modifier = Modifier.size(IconSize.NORMAL),
                    tint = colorScheme.onBackground
                )
                if (columns > 1){
                    Spacer(Modifier.width(5.dp))
                    AppText("Scan UPC", variant = TextType.Label)
                }
            }
        }
        when(uiState){
            is ScreenUiState.Loading -> {
                LoadingScreen()
            }
            is ScreenUiState.Error -> {
                ErrorMessage(uiState.message)
            }
            is ScreenUiState.Success -> {
                val items = uiState.items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.background)
                        .padding(bottom = 60.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items.size) { index ->
                        val item = items[index]
                        PaneItemCard(onAddToCart,item, navController)
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaneItemCard(onAddToCart: (item: CartItemEntity) -> Unit, record: ItemWithBatches, navController: NavController) {
    val rect = MaterialTheme.shapes.medium
    val qty = record.batches.sumOf { it.batch.quantity }
    val image = record.item.images?.find { it.sortOrder == 0 }
    val pricings = setOf(record.batches.flatMap { it.pricings }).toList().flatten()
    val batches = record.batches.filter { b -> b.batch.quantity > 0 && b.pricings.isNotEmpty() }.map { r -> r.batch }

    var isSheetOpen by remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }

    if (isSheetOpen){
        AddToCartSheet({isSheetOpen = false}, onAddToCart, record)
    }

    if (isDialogOpen){
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog if they tap outside or press back
                isDialogOpen = false
            },
            title = {
                AppText(text = "Out of stock!")
            },
            text = {
                AppText(text = "${record.item.name} is out currently out of stock. Click \"Restock now\" to restock.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDialogOpen = false
                        navController.navigate("${record.item.storeId}/inventory/restock/${record.item.id}")
                    }
                ) {
                    AppText(text = "Restock now", color = colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isDialogOpen = false } // Just close dialog
                ) {
                    AppText(text = "Cancel")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rect)
            .clickable { if (batches.isNotEmpty()) isSheetOpen = true else isDialogOpen = true }
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
                AppText("Out of stock", variant = TextType.LabelSmall, color = colorScheme.error)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCartSheet(
    onDismiss: () -> Unit,
    onNewCartItem: (item: CartItemEntity) -> Unit,
    record: ItemWithBatches
) {
    val sheetState = rememberModalBottomSheetState()
    val item = record.item
    val currentBatch = record.batches.find { r -> r.batch.createdAt == record.batches.maxOf { r -> r.batch.createdAt } }
    val totalQty = record.batches.sumOf { r -> r.batch.quantity }
    if (currentBatch == null){
        return
    }

    var cartItemDraft by remember { mutableStateOf(CartItemDraft(
        qty = 1.0,
        item = item
    )) }

    LaunchedEffect(Unit) {
        cartItemDraft = when(item.pricingScheme){
            PricingScheme.FIXED -> {
                val pricing = currentBatch.pricings.firstOrNull()
                cartItemDraft.copy(pricing = pricing, maxQty = totalQty)
            }
            PricingScheme.UNIT -> {
                val pricing = currentBatch.pricings.find { p -> (p.details as PricingDetails.UnitPrice).conversionFactor == 1.0 }
                val factor = (pricing?.details as PricingDetails.UnitPrice).conversionFactor ?: 0.0
                cartItemDraft.copy(pricing = pricing, maxQty = totalQty*factor)
            }
            PricingScheme.RANGE -> {
                val pricing = currentBatch.pricings.find { p -> p.amount == currentBatch.pricings.minOf { r -> r.amount } }
                val qty = (pricing?.details as PricingDetails.PriceRange?)?.qty ?: 0.0
                cartItemDraft.copy(pricing = pricing, maxQty = qty)
            }
            PricingScheme.RECURRING -> {
                val pricing = currentBatch.pricings.find { p-> p.amount == currentBatch.pricings.minOf { r -> r.amount } }
                cartItemDraft.copy(pricing = pricing, maxQty = 10000.0)
            }
            PricingScheme.MENU -> {
                val pricing = currentBatch.pricings.firstOrNull()
                cartItemDraft.copy(pricing = pricing, maxQty = totalQty)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.XL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(record.item.name, variant = TextType.Label)
            IconButton(onDismiss) {
                Icon(
                    painter = painterResource(R.drawable.x),
                    contentDescription = null,
                    tint = colorScheme.error,
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            }
        }
        HorizontalDivider()
        Column(
            Modifier
                .fillMaxWidth()
                .padding(Spacing.XL)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
        ){
            when(item.pricingScheme){
                PricingScheme.FIXED -> {
                    FixedPriceAddToCartForm(
                        onChange = {c -> cartItemDraft = c},
                        record = record,
                        cartItem = cartItemDraft
                    )
                }

                PricingScheme.UNIT -> {
                    UnitPriceAddToCartForm(
                        onChange = {c -> cartItemDraft = c},
                        record = record,
                        cartItem = cartItemDraft
                    )
                }

                PricingScheme.RANGE -> {
                    PriceRangeAddToCartForm(
                        onChange = {c -> cartItemDraft = c},
                        record = record,
                        cartItem = cartItemDraft
                    )
                }

                PricingScheme.RECURRING -> {
                    RecurringAddToCartForm(
                        onChange = {c -> cartItemDraft = c},
                        record = record,
                        cartItem = cartItemDraft
                    )
                }
                else -> {
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    val item = cartItemDraft.item
                    val pricing = cartItemDraft.pricing
                    cartItemDraft.qty?.let {
                        if (it > 0 && item != null && pricing != null ){
                            val c = CartItemEntity(
                                qty = it,
                                item = item,
                                pricing = pricing,
                                specs = cartItemDraft.specs,
                                maxQty = cartItemDraft.maxQty
                            )
                            onNewCartItem(c)
                            onDismiss()
                        }
                    }

                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.NORMAL),
                            tint = colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(Spacing.MD))
                        AppText("Add to cart", color = colorScheme.onPrimary, variant = TextType.Label)
                    }
                }
            }
        }
    }
}


@HiltViewModel
class PosScreenViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val itemRepository: ItemRepository,
    private val cartItemRepository: CartItemRepository
): ViewModel(){
    val storeId = savedState.get<Long>("storeId")

    private val _uiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Loading)
    val uiState: StateFlow<ScreenUiState> = _uiState
    private val _items = MutableStateFlow<List<ItemWithBatches>>(emptyList())
    private val _cartItems = MutableStateFlow<List<CartItemEntity>>(emptyList())
    val cartItems: StateFlow<List<CartItemEntity>> = _cartItems

    fun addToCart(item: CartItemEntity){
        val itemWithBatches = _items.value.find { i -> i.item.id == item.item.id }
        val validBatches = itemWithBatches?.batches?.filter { b -> b.batch.quantity > 0 }?.map { r -> r.batch }
        val factor = when(val pricingDetails = item.pricing.details){
            is PricingDetails.UnitPrice -> {
                pricingDetails.conversionFactor ?: 1.0
            }
            else  -> {
                1.0
            }
        }
        if(validBatches.isNullOrEmpty()){
            return
        }
        val finalizedCartItem = item.copy(batchesDetails = prepareBatchDetails(validBatches, item.qty, factor))

        val items = _cartItems.value.toMutableList()

        val existingItem = _cartItems.value.find { i -> i.item == finalizedCartItem.item && i.pricing == finalizedCartItem.pricing }
        if (existingItem == null){
            viewModelScope.launch {
                try {
                    cartItemRepository.insert(item)
                    _cartItems.value += item
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            return
        }

        val index = items.indexOf(existingItem)
        val totalQty = existingItem.qty.plus(item.qty)

        val newItem = existingItem.copy(qty = totalQty)

        editCartItem(newItem, index)
    }
    fun deleteFromCart(item: CartItemEntity){
        viewModelScope.launch {
            try {
                cartItemRepository.deleteItem(item)
                _cartItems.value -= item
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }
    fun clearCart(){
        viewModelScope.launch {
            try {
                cartItemRepository.clearCart()
                _cartItems.value = emptyList()
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    fun editCartItem(cartItem: CartItemEntity, index: Int){

        val itemWithBatches = _items.value.find { i -> i.item.id == cartItem.item.id }
        val validBatches = itemWithBatches?.batches?.filter { b -> b.batch.quantity > 0 }?.map { r -> r.batch }
        val factor = when(val pricingDetails = cartItem.pricing.details){
            is PricingDetails.UnitPrice -> {
                pricingDetails.conversionFactor ?: 1.0
            }
            else  -> {
                1.0
            }
        }
        if(validBatches.isNullOrEmpty()){
            return
        }

        val finalizedCartItem = cartItem.copy(batchesDetails = prepareBatchDetails(validBatches, cartItem.qty, factor))

        viewModelScope.launch {
            try {
                cartItemRepository.editItem(finalizedCartItem)
                val list = _cartItems.value.toMutableList()
                list[index] = finalizedCartItem
                _cartItems.value = list
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private suspend fun fetchItems(){
        if(storeId == null) {
            _uiState.value = ScreenUiState.Error("The store id is undefined.")
            return
        }
        try {
            itemRepository.getAllActiveItems(storeId).collect { items ->
                _items.value = items
                _uiState.value = ScreenUiState.Success(items)
            }
            cartItemRepository.getAllItems().collect { cartItems ->
                _cartItems.value = cartItems
            }

        }catch (e: Exception){
            _uiState.value = ScreenUiState.Error(e.message ?: "Unknown Error.")
        }
    }

    init {
        viewModelScope.launch {
            fetchItems()
        }
    }
}

sealed class ScreenUiState {
    data object Loading : ScreenUiState()
    data class Success(val items: List<ItemWithBatches>): ScreenUiState()
    data class Error(val message: String) : ScreenUiState()
}

data class CartItemDraft(
    val item: ItemEntity? = null,
    val batchesDetails: Map<String, Double> = emptyMap(),
    val qty: Double? = null,
    val pricing: Pricing? = null,
    val specs: Map<String, Any>? = null,
    val maxQty: Double = 0.0
)