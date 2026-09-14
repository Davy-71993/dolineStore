package com.example.doline.views.screens.store.inventory


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.BatchDraft
import com.example.doline.data.BatchEntity
import com.example.doline.data.BatchPricingCrossRef
import com.example.doline.data.BatchRepository
import com.example.doline.data.Currency
import com.example.doline.data.ItemEntity
import com.example.doline.data.ItemRepository
import com.example.doline.data.Pricing
import com.example.doline.data.PricingRepository
import com.example.doline.data.RestockFormError
import com.example.doline.timestampToDate
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.FormScreen
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.NumberInputField
import com.example.doline.views.components.PriceInputField
import com.example.doline.views.components.PricingForm
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun RestockScreen(navController: NavController, viewModel: RestockViewModel){

    val uiState by viewModel.uiState.collectAsState(RestockUiState.Fetching)
    val item by viewModel.item.collectAsState(null)
    val openDatePicker by viewModel.openDatePicker.collectAsState(false)
    val datePickerScope by viewModel.datePickerScope.collectAsState(null)
    val prevPrices by viewModel.prevPricings.collectAsState()
    val pricings by viewModel.pricings.collectAsState(emptyList())
    val batch by viewModel.batch.collectAsState()
    val error by viewModel.error.collectAsState()

    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val today = Clock.System.now().toEpochMilliseconds()
                return when(datePickerScope){
                    DatePickerScope.MANUFACTURE -> {
                        utcTimeMillis <= today
                    }
                    DatePickerScope.EXPIRY ->{
                        utcTimeMillis > today
                    }
                    else -> {
                        true
                    }
                }
            }
        })
    val itm = item
    val determinants = item?.priceDeterminants

    FormScreen(appBar = {
        TopAppBar(
            title = { AppText("Stock - ${itm?.name ?: "Loading..."}", variant = TextType.Heading, maxLines = 1) },
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
        )
    }) {
        if (openDatePicker) {
            DatePickerDialog(
                onDismissRequest = { viewModel.onOpenDatePickerChange(false) },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.onDateChange(millis)
                        }
                        viewModel.onOpenDatePickerChange(false)
                    }) {
                        AppText("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onOpenDatePickerChange(false) }) {
                        AppText("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    title = {
                        val title = if (datePickerScope == DatePickerScope.MANUFACTURE) "Select Manufacture Date" else "Select Expiry Date"
                        AppText(
                            title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.MD),
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
        when(val state = uiState){
            is RestockUiState.Fetching ->{
                LoadingScreen()
            }
            is RestockUiState.FetchError -> {
                ErrorMessage(state.err)
            }
            is RestockUiState.Success -> {
                navController.popBackStack()
            }
            else -> {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        Box(Modifier.weight(1f)){
                            NumberInputField(
                                onChange = { qty ->
                                    val edit = batch.copy(quantity = qty?.toDouble() ?: 0.0, available = qty?.toDouble() ?: 0.0)
                                    viewModel.onBatchEdit(edit)
                                },
                                number = batch.quantity,
                                label = "QUANTITY",
                                placeholder = "Enter quantity",
                                isError = !error.qty.isNullOrEmpty(),
                                errorMessage = error.qty
                            )
                        }
                        Box(Modifier.weight(1f)){
                            TextInputField(
                                batch.units ?: "",
                                { units ->
                                    val edit = batch.copy(units = units)
                                    viewModel.onBatchEdit(edit)
                                },
                                label = "UNITS",
                                placeHolder = "e.g. kg, pcs",
                                isError = !error.units.isNullOrEmpty(),
                                errorMessage = error.units
                            )
                        }
                    }
                    PriceInputField(
                        Currency.UGX,
                        { bp->
                            val edit = batch.copy(buyingPrice = bp)
                            viewModel.onBatchEdit(edit)
                        },
                        batch.buyingPrice,
                        label = "BUYING PRICE",
                        placeholder = "eg. 100,000",
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        Box(Modifier.weight(1f)){
                            TextInputField(
                                timestampToDate(batch.manufactureDate),
                                {},
                                placeHolder = "YY-MM-DD",
                                readOnly = true,
                                label = "MANUFACTURE DATE",
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.onOpenDatePickerChange(true, DatePickerScope.MANUFACTURE) }) {
                                        Icon(
                                            painter = painterResource(R.drawable.calendar_today),
                                            contentDescription = "Select Date",
                                            modifier = Modifier.size(IconSize.NORMAL),
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                        Box(Modifier.weight(1f)){
                            TextInputField(
                                timestampToDate(batch.expiryDate),
                                {},
                                placeHolder = "YY-MM-DD",
                                readOnly = true,
                                label = "EXPIRY DATE",
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.onOpenDatePickerChange(true, DatePickerScope.EXPIRY) }) {
                                        Icon(
                                            painter = painterResource(R.drawable.calendar_today),
                                            contentDescription = "Select Date",
                                            modifier = Modifier.size(IconSize.NORMAL),
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    if(itm != null){
                        PricingForm(
                            pricings,
                            { list ->
                                viewModel.onPricingEdit(list)
                            },
                            itm,
                            prevPrices,
                        )
                        if(!error.prices.isNullOrEmpty()){
                            AppText(error.prices ?: "Pricing error!", color = colorScheme.error)
                        }
                    }else{
                        ErrorMessage("Failed to fetch item details")
                    }

                }
                if (uiState is RestockUiState.Error){
                    ErrorMessage((uiState as RestockUiState.Error).message)
                }
                if (!error.general.isNullOrEmpty()){
                    AppText(error.general ?: "An error occurred!", color = colorScheme.error)
                }
                AppButton(
                    "Save to Stock",
                    {viewModel.handleSubmit()},
                    type = ButtonType.Primary,
                    isLoading = uiState is RestockUiState.Loading,

                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@HiltViewModel
class RestockViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val itemRepository: ItemRepository,
    private val batchRepository: BatchRepository,
    private val pricingRepository: PricingRepository
): ViewModel(){
    val itemId = savedState.get<Long>("itemId")
    val storeId = savedState.get<Long>("storeId")
    private val _uiState = MutableStateFlow<RestockUiState>(RestockUiState.Fetching)
    val uiState: Flow<RestockUiState> = _uiState
    private val _item = MutableStateFlow<ItemEntity?>(null)
    val item: Flow<ItemEntity?> = _item

    private val _batch = MutableStateFlow(BatchDraft(itemId = itemId?: 0, units = _item.value?.sku))
    val batch: StateFlow<BatchDraft> = _batch

    private val _pricings = MutableStateFlow<List<Pricing>>(emptyList())
    val pricings: Flow<List<Pricing>> = _pricings



    private val _error = MutableStateFlow(RestockFormError())
    val error: StateFlow<RestockFormError> = _error

    private val _openDatePicker = MutableStateFlow(false)
    val openDatePicker: Flow<Boolean> = _openDatePicker
    private val _datePickerScope = MutableStateFlow<DatePickerScope?>(null)
    val datePickerScope: Flow<DatePickerScope?> = _datePickerScope
    private val _prevPricings = MutableStateFlow<List<Pricing>>(emptyList())
    val prevPricings: StateFlow<List<Pricing>> = _prevPricings
    private val _inStock = MutableStateFlow(0.0)

    fun onBatchEdit(b: BatchDraft){
        _batch.value = b
        _error.value = RestockFormError()
    }
    fun onPricingEdit(l: List<Pricing>){
        _pricings.value = l
        _error.value = RestockFormError()
    }
    fun onDateChange(date: Long){
        _datePickerScope.value?.let {
            if(it == DatePickerScope.MANUFACTURE){
                val b = _batch.value.copy(manufactureDate = date)
                _batch.value = b
            }else if (it == DatePickerScope.EXPIRY){
                val b = _batch.value.copy(expiryDate = date)
                _batch.value = b
            }
        }
    }
    fun onOpenDatePickerChange(open: Boolean, scope: DatePickerScope? = null) {
        _openDatePicker.value = open
        _datePickerScope.value = scope
    }

    fun handleSubmit(){
        _uiState.value = RestockUiState.Loading
        val itmId = itemId
        if(itmId == null ){
            _uiState.value = RestockUiState.Error("Invalid store ID: $storeId")
            return
        }

        viewModelScope.launch {
            var er = _error.value
            val batch = _batch.value
            val pricings = _pricings.value
            val bn = batch.batchNumber
            val qty = batch.quantity
            val uts = batch.units
            val inStock = _inStock.value

            if (qty == null || qty <= 0){
                er = er.copy(qty = "The quantity is required.")
            }
            if (uts.isNullOrEmpty()){
                er = er.copy(units = "Specify the stocking units.")
            }
            if (pricings.isEmpty()){
                er = er.copy(prices = "The selling price(S) can not be empty.")
            }
            try {
                if (er.hasErrors()){
                    _error.value = er
                    _uiState.value = RestockUiState.Idle
                    return@launch
                }

                val b = BatchEntity(
                    batchNumber = bn ?: "1",
                    itemId = itmId,
                    quantity = qty!!,
                    available = if(inStock > 0) qty else inStock+qty,
                    units = uts!!,
                    expiryDate = _batch.value.expiryDate,
                    manufactureDate = _batch.value.manufactureDate,
                    buyingPrice = _batch.value.buyingPrice
                )

                val batchId = batchRepository.insertBatch(b)
                val pricingIds = mutableListOf<Long>()
                _pricings.value.forEach { p ->
                    val pid = pricingRepository.insertPricing(p)
                    pricingIds += pid
                }

                val pairs = pricingIds.map { pid ->
                    BatchPricingCrossRef(batchId = batchId, pricingId = pid)
                }

                batchRepository.insertBatchPricings(pairs)
                _uiState.value = RestockUiState.Success
            }catch (e: Exception){
                e.printStackTrace()
                _error.value = er.copy(general=e.message ?: "Unknown error occurred!")
                _uiState.value = RestockUiState.Idle
            }
        }
    }
    fun getItemWithBatches(){
        viewModelScope.launch {
            _uiState.value = RestockUiState.Fetching
            if(itemId == null){
                _uiState.value = RestockUiState.FetchError("Error: The item id is null")
                return@launch
            }
            try {
                itemRepository.getItemById(itemId)
                    .catch {
                        _uiState.value = RestockUiState.FetchError(it.message ?: "Unknown Error occurred!")
                    }
                    .collect {
                        _item.value = it?.item
                        val b = _batch.value
                        _batch.value = b.copy(batchNumber = ((it?.batches?.size?.plus(1)).toString()), units = it?.item?.sku)
                        _uiState.value = RestockUiState.Idle
                        _inStock.value = it?.batches?.sumOf { r -> r.batch.available } ?: 0.0
                        val prices = it?.batches?.flatMap { b ->
                            b.pricings
                        } ?: emptyList()

                        _prevPricings.value = prices
                    }
            }catch (e: Exception){
                _uiState.value = RestockUiState.FetchError(e.message ?: "Unknown Error occurred!")
            }
        }

    }

    init {
        getItemWithBatches()
    }

}

sealed class RestockUiState {
    data object Idle: RestockUiState()
    data class FetchError(val err: String): RestockUiState()
    data object Fetching: RestockUiState()
    data class  Error(val message:String): RestockUiState()
    data object Success: RestockUiState()
    data object Loading: RestockUiState()
}
enum class DatePickerScope {
    EXPIRY, MANUFACTURE
}