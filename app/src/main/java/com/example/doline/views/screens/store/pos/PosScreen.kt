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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
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
import com.example.doline.data.ClientWithProfile
import com.example.doline.data.UserProfile
import com.example.doline.data.UserProfileRepository
import com.example.doline.data.BatchRepository
import com.example.doline.data.CartItem
import com.example.doline.data.CartItemEntity
import com.example.doline.data.CartItemRepository
import com.example.doline.data.ClientEntity
import com.example.doline.data.ClientRepository
import com.example.doline.data.Currency
import com.example.doline.data.ItemEntity
import com.example.doline.data.ItemRepository
import com.example.doline.data.ItemWithBatches
import com.example.doline.data.OrderEntity
import com.example.doline.data.OrderItemEntity
import com.example.doline.data.OrderItemRepository
import com.example.doline.data.OrderProgress
import com.example.doline.data.OrderRepository
import com.example.doline.data.OrderStatus
import com.example.doline.data.Pricing
import com.example.doline.data.PricingDetails
import com.example.doline.data.PricingScheme
import com.example.doline.prepareBatchDetails
import com.example.doline.rememberCameraPermissionState
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.CameraScannerOverlay
import com.example.doline.views.components.ClientSelection
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
import com.example.doline.views.components.ScannerBtn
import com.example.doline.views.components.Screen
import com.example.doline.views.components.ShoppingCart
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import com.example.doline.views.components.UnitPriceAddToCartForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(navController: NavController, viewModel: PosScreenViewModel){
    rememberCameraPermissionState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val receivedAmount by viewModel.receivedAmount.collectAsState()
    val submitFeedback by viewModel.submitFeedback.collectAsState()
    val submitError by viewModel.submitError.collectAsState()
    val isSheetOpen by viewModel.openAddToCartSheet.collectAsState()
    val item by viewModel.item.collectAsState()
    val scannerState by viewModel.posUiState.collectAsState()
    val scanError by viewModel.scanError.collectAsState()
    val client by viewModel.client.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val storeId = viewModel.storeId

    val cartTotal = cartItems.sumOf { ci -> (ci.cartItem.qty * ci.pricing.amount) }
    val feedback = submitFeedback

    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
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
                            onCompleteSale = { p ->
                                viewModel.sell(p)
                            },
                            onClose = {scope.launch { drawerState.close() }},
                            onCartItemChanged = { item, index ->
                                viewModel.editCartItem(item, index)
                            },
                            onDelete = { item ->
                                viewModel.deleteFromCart(item)
                            },
                            onClear = {
                                viewModel.clearCart()
                            },
                            items = cartItems,
                            deviceSize = DeviceSize.MOBILE,
                            onReceivedAmountChanged = {
                                viewModel.onReceivedAmountChanged(it)
                            },
                            receivedAmount = receivedAmount,
                            error = submitError,
                            clients = clients,
                            storeId = storeId,
                            onAddClient = {
                                viewModel.onClientSelectionChanged(it)
                            },
                            client = client
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
                    val record = item
                    if (isSheetOpen && record != null){
                        AddToCartSheet({viewModel.closeAddToCartSheet()}, {viewModel.addToCart(it)}, record)
                    }
                    if (scanError.isNotEmpty()){
                        AlertDialog(
                            onDismissRequest = {viewModel.onClearScanError()},
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.onClearScanError()
                                }) {
                                    AppText("Ok", color = colorScheme.primary)
                                }
                            },
                            title = {AppText("Scan error!")},
                            text = {AppText(scanError)}
                        )
                    }
                    if (scannerState.scanningModeActive && scannerState.isCameraOpen){
                        CameraScannerOverlay(
                            isPaused = scannerState.isScanningPaused,
                            onUpcScanned = { viewModel.onUpcScanned(it) },
                            onClose = { viewModel.closeScanner()}
                        )
                    }
                    if (feedback != null){
                        val message = feedback.message
                        val confirmAction = feedback.action1
                        val dismissAction = feedback.action2
                        AlertDialog(
                            onDismissRequest = {
                                viewModel.onDismiss()
                            },
                            title = {
                                AppText(text = "Alert!")
                            },
                            text = {
                                AppText(text = message)
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = confirmAction.second
                                ) {
                                    AppText(text = confirmAction.first, color = colorScheme.primary)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = dismissAction.second
                                ) {
                                    AppText(text = dismissAction.first)
                                }
                            }
                        )
                    }
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
                                    uiState = uiState,
                                    columns = columns,
                                    onItemClicked = {},
                                    onOpenScanner = {}
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
                                    onCompleteSale = { p->
                                        viewModel.sell(p)
                                    },
                                    onClose = {scope.launch { drawerState.close() }},
                                    onCartItemChanged = { item, index ->
                                        viewModel.editCartItem(item,index )
                                    },
                                    onDelete = { item ->
                                        viewModel.deleteFromCart(item)
                                    },
                                    onClear = {
                                        viewModel.clearCart()
                                    },
                                    items = cartItems,
                                    deviceSize = DeviceSize.TABLET,
                                    onReceivedAmountChanged = {
                                        viewModel.onReceivedAmountChanged(it)
                                    },
                                    receivedAmount = receivedAmount,
                                    error = submitError,
                                    clients = clients,
                                    storeId = storeId,
                                    onAddClient = {
                                        viewModel.onClientSelectionChanged(it)
                                    },
                                    client = client
                                )
                            }
                        }
                    }else{
                        ItemsPane(
                            uiState = uiState,
                            columns = columns,
                            onOpenScanner = {viewModel.openScanner()},
                            onItemClicked = {viewModel.launchAddToCartSheet(it)}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemsPane(
    uiState: ScreenUiState,
    columns: Int,
    onOpenScanner: () -> Unit,
    onItemClicked: (item: ItemWithBatches) -> Unit
){
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = Spacing.MD)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = if (columns > 1) Spacing.MD else 0.dp),
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
                TextInputField(value = "", onValueChange = {}, placeHolder = "Search stock items")
            }
            if (columns > 1){
                Spacer(Modifier.width(20.dp))
            }
            ScannerBtn(
                onPermissionGranted = onOpenScanner,
                modifier = Modifier
            )
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
                        PaneItemCard(
                            onClick = onItemClicked,
                            record = item
                        )
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaneItemCard(
    record: ItemWithBatches,
    onClick: (ItemWithBatches) -> Unit
) {
    val rect = MaterialTheme.shapes.medium
    val qty = record.batches.sumOf { it.batch.available }
    val image = record.item.images?.find { it.sortOrder == 0 }
    val pricings = setOf(record.batches.flatMap { it.pricings }).toList().flatten()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rect)
            .clickable { onClick(record) }
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
    onNewCartItem: (item: CartItem) -> Unit,
    record: ItemWithBatches
) {
    val sheetState = rememberModalBottomSheetState()
    val item = record.item
    val currentBatch = record.batches.find { r -> r.batch.createdAt == record.batches.maxOf { r -> r.batch.createdAt } }
    val totalQty = record.batches.sumOf { r -> r.batch.available }

    var errorMessage by remember { mutableStateOf("") }
    if (currentBatch == null){
        errorMessage = "There is no active batch. Please restock before continuing."
    }
    if (totalQty <= 0){
        errorMessage = "${item.name} is out of stock. Please restock before continuing."
    }

    var cartItemDraft by remember { mutableStateOf(CartItemDraft(
        qty = 1.0,
        item = item
    )) }

    LaunchedEffect(Unit) {
        cartItemDraft = when(item.pricingScheme){
            PricingScheme.FIXED -> {
                val pricing = currentBatch?.pricings?.firstOrNull()
                cartItemDraft.copy(pricing = pricing, maxQty = totalQty)
            }
            PricingScheme.UNIT -> {
                val pricing = currentBatch?.pricings?.find { p -> (p.details as PricingDetails.UnitPrice).conversionFactor == 1.0 }
                val factor = (pricing?.details as PricingDetails.UnitPrice?)?.conversionFactor ?: 0.0
                cartItemDraft.copy(pricing = pricing, maxQty = totalQty*factor)
            }
            PricingScheme.RANGE -> {
                val pricing = currentBatch?.pricings?.find { p -> p.amount == currentBatch.pricings.minOf { r -> r.amount } }
                val qty = (pricing?.details as PricingDetails.PriceRange?)?.qty ?: 0.0
                cartItemDraft.copy(pricing = pricing, maxQty = qty)
            }
            PricingScheme.RECURRING -> {
                val pricing = currentBatch?.pricings?.find { p-> p.amount == currentBatch.pricings.minOf { r -> r.amount } }
                cartItemDraft.copy(pricing = pricing, maxQty = 10000.0)
            }
            PricingScheme.MENU -> {
                val pricing = currentBatch?.pricings?.firstOrNull()
                cartItemDraft.copy(pricing = pricing, maxQty = totalQty)
            }
        }
    }

    if (errorMessage.isNotEmpty()){
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                AppText(text = "Out of stock!")
            },
            text = {
                AppText(text = errorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    AppText(text = "Ok", color = colorScheme.primary)
                }
            }
        )
    }else{
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
                                    itemId = item.id,
                                    pricingId = pricing.id,
                                    specs = cartItemDraft.specs,
                                    maxQty = cartItemDraft.maxQty
                                )
                                val cartI = CartItem(
                                    cartItem = c,
                                    item = item,
                                    pricing = pricing
                                )
                                onNewCartItem(cartI)
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

}


@HiltViewModel
class PosScreenViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val itemRepository: ItemRepository,
    private val cartItemRepository: CartItemRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val batchRepository: BatchRepository,
    private val clientRepository: ClientRepository,
    private val profileRepository: UserProfileRepository
): ViewModel(){
    val storeId = savedState.get<Long>("storeId")
    private val _uiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Loading)
    val uiState: StateFlow<ScreenUiState> = _uiState
    private val _posUiState = MutableStateFlow(PosUiState())
    val posUiState: StateFlow<PosUiState> = _posUiState.asStateFlow()
    private val _items = MutableStateFlow<List<ItemWithBatches>>(emptyList())
    private val _item = MutableStateFlow<ItemWithBatches?>(null)
    val item: StateFlow<ItemWithBatches?> = _item
    private val _openAddToCartSheet = MutableStateFlow(false)
    val openAddToCartSheet: StateFlow<Boolean> = _openAddToCartSheet
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems
    private val _submitError = MutableStateFlow("")
    val submitError: StateFlow<String> = _submitError
    private val _receivedAmount = MutableStateFlow<Double?>(null)
    val receivedAmount: StateFlow<Double?> = _receivedAmount
    private val _submitFeedback = MutableStateFlow<SubmitFeedback?>(null)
    val submitFeedback: StateFlow<SubmitFeedback?> = _submitFeedback
    private val _scanError = MutableStateFlow("")
    val scanError: StateFlow<String> = _scanError.asStateFlow()

    private val _clients = MutableStateFlow<List<ClientWithProfile>>(emptyList())
    val clients: StateFlow<List<ClientWithProfile>> = _clients

    private val _client = MutableStateFlow<ClientWithProfile?>(null)
    val client: StateFlow<ClientWithProfile?> = _client

    fun onClientSelectionChanged(selection: ClientSelection){
        when (selection) {
            is ClientSelection.None -> _client.value = null
            is ClientSelection.Existing -> _client.value = selection.client
            is ClientSelection.New -> createClient(selection.name, selection.phone, selection.address)
        }
    }

    private fun createClient(name: String, phone: String?, address: String?){
        val storeId = storeId ?: return
        viewModelScope.launch {
            val profile = UserProfile(fullNames = name, phone = phone, defaultAddress = address)
            val profileId = profileRepository.insertProfile(profile)
            val clientEntity = ClientEntity(storeId = storeId, profileId = profileId)
            val clientId = clientRepository.insert(clientEntity)
            _client.value = ClientWithProfile(clientEntity.copy(id = clientId), profile.copy(id = profileId))
        }
    }

    private suspend fun fetchClients(){
        storeId?: return
        clientRepository.getClients(storeId).collect {
            _clients.value = it
        }
    }
    fun openScanner() {
        _posUiState.update { it.copy(scanningModeActive = true, isCameraOpen = true, isScanningPaused = false) }
    }
    fun pauseScanner(){
        _posUiState.update { it.copy(isScanningPaused = true, isCameraOpen = false) }
    }
    fun resumeScanning(){
        _posUiState.update { it.copy(isScanningPaused = false, isCameraOpen = true) }
    }
    fun closeScanner() {
        _posUiState.update { it.copy(isCameraOpen = false, isScanningPaused = false, scanningModeActive = false) }
    }
    private fun findItemByUpc(upc: String): ItemWithBatches? {
        return _items.value.find { it.item.upc == upc }
    }
    fun onUpcScanned(upc: String) {
        if (_posUiState.value.isScanningPaused) return
        pauseScanner()
        val itemWithBatches = findItemByUpc(upc)
        if (itemWithBatches == null ){
            _scanError.value = "No stock item matched UPC: $upc"
        }else{
            launchAddToCartSheet(itemWithBatches)
        }
    }
    fun onClearScanError(){
        _scanError.value = ""
        resumeScanning()
    }
    fun launchAddToCartSheet(it: ItemWithBatches){
        _item.value = it
        openAddToCartSheet()
    }
    private fun openAddToCartSheet(){
        _openAddToCartSheet.value = true
    }
    fun closeAddToCartSheet(){
        _openAddToCartSheet.value = false
        _item.value = null
        if(_posUiState.value.scanningModeActive){
            resumeScanning()
        }
    }
    fun addToCart(cartItem: CartItem){
        _submitError.value = ""
        val item = cartItem.cartItem
        val itemWithBatches = _items.value.find { i -> i.item.id == cartItem.item.id }
        val validBatches = itemWithBatches?.batches?.filter { b -> b.batch.available > 0 }?.map { r -> r.batch }
        val factor = when(val d = cartItem.pricing.details){
            is PricingDetails.UnitPrice -> {
                d.conversionFactor ?: 1.0
            }
            else -> {
                1.0
            }
        }

        if(validBatches.isNullOrEmpty()){
            return
        }
        val finalizedCartItem = item.copy(batchesDetails = prepareBatchDetails(validBatches, item.qty, factor))

        val items = _cartItems.value.toMutableList()

        val existingItem = _cartItems.value.find { i -> i.cartItem.pricingId == finalizedCartItem.pricingId}
        if (existingItem == null){
            viewModelScope.launch {
                try {
                   val id =  cartItemRepository.insert(finalizedCartItem)
                    val newCartItem = cartItem.copy(cartItem = finalizedCartItem.copy(id = id))
                    _cartItems.value += newCartItem
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            return
        }

        val index = items.indexOf(existingItem)
        val totalQty = existingItem.cartItem.qty.plus(item.qty)

        val newItem = existingItem.copy(cartItem = existingItem.cartItem.copy(qty = totalQty))
        editCartItem(newItem, index)
    }
    fun deleteFromCart(cartItem: CartItem){
        val item = cartItem.cartItem
        viewModelScope.launch {
            try {
                cartItemRepository.deleteItem(item)
                _cartItems.value -= cartItem
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
    fun editCartItem(cartItem: CartItem, index: Int){

        val itemWithBatches = _items.value.find { i -> i.item.id == cartItem.item.id }
        val validBatches = itemWithBatches?.batches?.filter { b -> b.batch.available > 0 }?.map { r -> r.batch }
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

        val finalizedCartItem = cartItem.copy(cartItem = cartItem.cartItem.copy(batchesDetails = prepareBatchDetails(validBatches, cartItem.cartItem.qty, factor)))

        viewModelScope.launch {
            try {
                cartItemRepository.editItem(finalizedCartItem.cartItem)
                val list = _cartItems.value.toMutableList()
                list[index] = finalizedCartItem
                _cartItems.value = list
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    fun onReceivedAmountChanged(amount: Double?){
        _receivedAmount.value = amount
    }
    fun onDismiss(){
        _submitFeedback.value = null
    }
    fun sell(progress: OrderProgress){
        val cartItems = _cartItems.value
        if(cartItems.isEmpty()){
            _submitError.value = "The cart is empty."
            return
        }
        if (progress == OrderProgress.DRAFT){
            _submitFeedback.value = SubmitFeedback(
                message = "Are you sure you want to save this order as a draft?",
                action1 = ("Save" to { completeSale(status = OrderStatus.DRAFT, progress = progress) } ),
                action2 = ("Cancel" to {})
            )

            return
        }
        val amount = _receivedAmount.value
        val total = _cartItems.value.sumOf { i -> i.pricing.amount * i.cartItem.qty }
        if(amount == null || amount <= 0){
           _submitFeedback.value = SubmitFeedback(
               message = "Was this order paid or offered at credit?",
               action1 = ("Paid" to { completeSale(status = OrderStatus.PAID, progress = progress) } ),
               action2 = ("Credit" to { completeSale(status = OrderStatus.CREDIT, progress = progress) })
           )
            return
        }
        if( amount < total){
            _submitFeedback.value = SubmitFeedback(
                message = "The amount received is less than the order amount. Do you want to offer the balance as a credit?",
                action1 = ("Yes" to {completeSale(status = OrderStatus.CREDIT, progress) }),
                action2 = ("No" to { _submitFeedback.value = null })
            )
            return
        }
        completeSale(status = OrderStatus.PAID, progress = progress)
    }
    private fun completeSale(status: OrderStatus, progress: OrderProgress){
        if (storeId == null) {
            _submitError.value = "The store id is undefined."
            return
        }
        val cb = when(status){
            OrderStatus.CREDIT -> {
                val orderAmount = _cartItems.value.sumOf { ci -> ci.pricing.amount * ci.cartItem.qty }
                 (orderAmount - (_receivedAmount.value ?: 0.0)).coerceAtMost(orderAmount)
            }
            else -> null
        }

        // 1. Create order and get the orderId
        val orderEntity = OrderEntity(
            storeId = storeId,
            progress = progress,
            status = status,
            amountReceived = _receivedAmount.value,
            clientId = _client.value?.client?.id,
            creditBalance = cb
        )
        viewModelScope.launch {
            try {
                val orderId = orderRepository.insert(orderEntity)
                // 2. a. For each cartItem, create a corresponding orderItem with the orderId
                _cartItems.value.forEach { ci ->
                    val orderItemEntity = OrderItemEntity(
                        orderId = orderId,
                        itemId = ci.item.id,
                        pricingId = ci.pricing.id,
                        qty = ci.cartItem.qty,
                        batchesDetails = ci.cartItem.batchesDetails,
                        specs = ci.cartItem.specs
                    )
                    orderItemRepository.insert(orderItemEntity)
                    //    b. Update the stock quantities based on the cartItem batchDetails
                    updateStockQuantity(ci.cartItem.batchesDetails)
                }

                // 3. Clear the cart.
                clearCart()
                _submitFeedback.value = null
                _submitError.value = ""
                _receivedAmount.value = null
                _client.value = null
            }catch (e: Exception){
                _submitError.value = e.message ?: "Unknown error occurred"
            }
        }
    }
    private suspend fun updateStockQuantity(details: Map<String, Double>){
        details.forEach { pair ->
            val batchId = pair.key.split("BAT").last().toLong()
            val qty = pair.value
            batchRepository.decreaseQuantity(batchId, qty)
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
        }catch (e: Exception){
            _uiState.value = ScreenUiState.Error(e.message ?: "Unknown Error.")
        }
    }
    private  suspend fun fetchCartItems(){
        try {
            cartItemRepository.getAllItems().collect { items ->
                _cartItems.value = items
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    init {
        viewModelScope.launch {
            launch { fetchCartItems() }
            launch { fetchItems() }
            launch { fetchClients() }
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

data class SubmitFeedback(
    val message: String,
    val action1: Pair<String, () -> Unit>,
    val action2: Pair<String, () -> Unit>
)

data class PosUiState(
    val scanningModeActive: Boolean = false,
    val isCameraOpen: Boolean = false,
    val isScanningPaused: Boolean = false
)