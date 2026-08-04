package com.example.doline.views.screens.store.inventory



import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.ItemEntity
import com.example.doline.data.ItemImage
import com.example.doline.data.ItemRepository
import com.example.doline.data.PricingScheme
import com.example.doline.data.SelectOption
import com.example.doline.getPersistentImageUrl
import com.example.doline.hasAnyError
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.FormScreen
import com.example.doline.views.components.GhostInputField
import com.example.doline.views.components.ImageView
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.SelectField
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.contains
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemForm(navController: NavController, viewModel: EditItemScreenViewModel ){
    val rect = RoundedCornerShape(8.dp)

    val uiState by viewModel.uiState.collectAsState()
    val item by viewModel.item.collectAsState()
    val error by viewModel.error.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val itemId = viewModel.itemId

    val context = LocalContext.current
    var sortIndex by remember { mutableIntStateOf(0) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        uris.forEach { uri ->
            // Convert URI to persistent URL (copy to app storage or use content resolver)
            val persistentUrl = getPersistentImageUrl(context, uri)
            // Add as new image, defaulting to sort order 0 for the first selection

            val currentImages = item.images ?: emptyList()

            val updatedImages = currentImages.map { image ->
                if (image.sortOrder == sortIndex) {
                    // Replace existing image
                    ItemImage(
                        url = persistentUrl,
                        sortOrder = sortIndex,
                    )
                } else {
                    image
                }
            }.toMutableList()

            // If no image with this sortOrder exists, add new one
            if (updatedImages.none { it.sortOrder == sortIndex }) {
                updatedImages.add(
                    ItemImage(
                        url = persistentUrl,
                        sortOrder = sortIndex
                    )
                )
            }

            val readyImages = updatedImages.sortedBy { it.sortOrder }
            val newItem = item.copy(images = readyImages)
            viewModel.onEdit(newItem)
        }
    }

    FormScreen(appBar = {
        TopAppBar(
            title = { AppText(
                if(itemId == null) "Create stock item" else "Edit stock item",
                variant = TextType.Heading,
                maxLines = 1
            ) },
            modifier = Modifier
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
            }
        )
    }) {

        when(val state = uiState){
            is EditScreenUiState.Loading -> {
                LoadingScreen()
            }
            is EditScreenUiState.Error -> {
                ErrorMessage(state.message)
            }
            is EditScreenUiState.Idle -> {
                TextInputField(
                    item.upc ?: "",
                    {
                        val newItem = item.copy(upc = it)
                        viewModel.onEdit(newItem)
                    },
                    label = "UPC",
                    placeHolder = "Universal Product Code",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                TextInputField(
                    item.name,
                    {
                        val newItem = item.copy(name = it)
                        viewModel.onEdit(newItem)
                    },
                    label = "PRODUCT TITLE",
                    placeHolder = "e.g Handcrafted Oak Minimalist Lamp",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    isError = !error?.name.isNullOrEmpty(),
                    errorMessage = error?.name
                )

                TextInputField(
                    item.description ?: "",
                    {
                        val newItem = item.copy(description = it)
                        viewModel.onEdit(newItem)
                    },
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5,
                    label = "PRODUCT DESCRIPTION",
                    placeHolder = "Describe your product or service",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    isError = !error?.description.isNullOrEmpty(),
                    errorMessage = error?.description
                )

                TextInputField(
                    item.sku ?: "",
                    {
                        val newItem = item.copy(sku = it)
                        viewModel.onEdit(newItem)
                    },
                    label = "SKU",
                    placeHolder = "Stock keeping units e.g Kilograms",
                    isError = !error?.sku.isNullOrEmpty(),
                    errorMessage = error?.sku
                )

                TextInputField(
                    item.categorySlug.capitalize(),
                    {
                        val newItem = item.copy(categorySlug = it)
                        viewModel.onEdit(newItem)
                    },
                    label = "CATEGORY",
                    placeHolder = "e.g Cosmetics",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                TextInputField(
                    item.subCategorySlug?.capitalize() ?: "",
                    {
                        val newItem = item.copy(subCategorySlug = it)
                        viewModel.onEdit(newItem)
                    },
                    label = "SUB-CATEGORY",
                    placeHolder = "e.g Beauty Lotion",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XXS),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val specs = item.specs ?: emptyMap<String, Any>()
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText("ITEM SPECIFICATIONS", variant = TextType.LabelSmall)
                        AddSpecForm(
                            { key, value ->
                                val map = mutableMapOf<String, Any?>(key to value)
                                val newMap = map.plus(item.specs ?: emptyMap())
                                val newItem = item.copy(specs = newMap)
                                viewModel.onEdit(newItem)
                            },
                            specs.keys.toList()
                        )
                    }
                    if (specs.isEmpty()) {
                        AppText(
                            "No item specifications! Click the PLUS sign to add.",
                            color = colorScheme.onBackground.copy(.6f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        FlowRow(
                                Modifier
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(Spacing.MD),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                            specs.keys.forEach { key ->
                                Column(
                                    Modifier
                                        .weight(.5f)
                                        .fillMaxWidth()
                                        .background(colorScheme.surface.copy(.6f), rect)
                                        .padding(horizontal = Spacing.SM)
                                        .padding(bottom = Spacing.XXS),
                                ) {
                                    GhostInputField(
                                        value = (specs[key] ?: "") as String,
                                        focusedTextColor = colorScheme.onBackground,
                                        unfocusedTextColor = colorScheme.onBackground,
                                        onValueChange = { text ->
                                            val newSpecs = specs.toMutableMap().apply { this[key] = text }
                                            val newItem = item.copy(specs= newSpecs)

                                            viewModel.onEdit(newItem)
                                        },
                                        modifier = Modifier.padding(0.dp)
                                    )
                                    HorizontalDivider()
                                    AppText(
                                        key.uppercase(),
                                        maxLines = 1,
                                        variant = TextType.Small,
                                        textAlign = TextAlign.Center,
                                        color = colorScheme.onBackground.copy(.6f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = Spacing.XL)
                                    )
                                }
                            }
                        }
                    }
                }
                SelectField(
                    "${item.pricingScheme.name.capitalize()} Pricing",
                    PricingScheme.entries.map { entry -> SelectOption("${entry.name} Pricing".capitalize(), entry) },
                    {
                        val newItem = item.copy(pricingScheme = it.value)
                        viewModel.onEdit(newItem)
                    },
                    label = "PRICING SCHEME",
                )

                if(item.pricingScheme == PricingScheme.RANGE){
                    val determinants = item.specs?.filter { s ->
                        (s.value as String).split(",").isNotEmpty()
                    }
                    if(determinants.isNullOrEmpty()){
                        Box(
                            Modifier.fillMaxWidth().clip(CircleShape).padding(Spacing.MD).background(colorScheme.errorContainer)
                        ){
                            AppText(
                                "Can't find any specifications that the price can base on. Please add comma seperated specifications like SIZE: SMALL, MIDUIM etc.",
                                color = colorScheme.error
                            )
                        }
                    }else{
                        val dts = item.priceDeterminants?: emptyMap()
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            determinants.forEach { dt ->
                                val selected = dts.contains(dt.key)
                                TextButton(
                                    {
                                        val newDts = if(selected){
                                            dts - dt.key
                                        }else{
                                            dts + (dt.key to (dt.value as String).split(","))
                                        }
                                        val ety = item.copy(priceDeterminants = newDts)
                                        viewModel.onEdit(ety)
                                    },
                                    modifier = Modifier.widthIn(min = 100.dp).size(100.dp, 30.dp),
                                    contentPadding = PaddingValues(10.dp, 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(selected) colorScheme.primary else colorScheme.surface
                                    )
                                ) {
                                    AppText(dt.key.uppercase(), variant = TextType.LabelSmall, color = if(selected) colorScheme.onPrimary else colorScheme.onSurface)
                                }
                            }
                        }
                    }

                }

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                ) {
                    val images = item.images ?: emptyList()
                    AppText("ITEM IMAGES", variant = TextType.LabelSmall)
                    if (!error?.images.isNullOrEmpty()){
                        ErrorMessage(error?.images ?: "")
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(rect)
                            .clickable {
                                sortIndex = 0
                                imagePicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                            .background(colorScheme.surface.copy(.6f), rect),
                        //                                    .border(Dp.Hairline, colorScheme.onBackground, rect),
                        contentAlignment = Alignment.Center
                    ) {
                        val firstImage = images.firstOrNull()
                        if (firstImage == null) {
                            Column(
                                Modifier.wrapContentSize(),
                                verticalArrangement = Arrangement.spacedBy(Spacing.XL),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.camera),
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(IconSize.BIG)
                                )
                                AppText(
                                    "Add main cover Image",
                                    color = colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            ImageView(firstImage.url)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        listOf(1, 2, 3, 4).forEach { sortOrder ->
                            val image = images.find { it.sortOrder == sortOrder }
                            Box(
                                Modifier
                                    .weight(.5f)
                                    .height(80.dp)
                                    .clip(rect)
                                    .background(colorScheme.surface.copy(.6f), rect)
                                    .clickable {
                                        sortIndex = sortOrder
                                        imagePicker.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (image != null) {
                                    ImageView(image.url)
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.plus),
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!error?.general.isNullOrEmpty()){
                    Box(
                        Modifier.fillMaxWidth().clip(rect).background(colorScheme.errorContainer).padding(vertical = 5.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ){
                        ErrorMessage(error?.general ?: "")
                    }
                }

                AppButton(
                    if(itemId == null) "Add to stock" else "Save Changes",
                    { viewModel.handleSave(navController) },
                    type = ButtonType.Primary,
                    isLoading = loading
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@HiltViewModel
class EditItemScreenViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val itemRepository: ItemRepository
): ViewModel(){
    val itemId = savedState.get<Long>("itemId")
    val storeId = savedState.get<Long>("storeId")

    private val _uiState = MutableStateFlow<EditScreenUiState>(EditScreenUiState.Loading)
    private val _item = MutableStateFlow(ItemEntity(0, storeId ?: 0 , "", categorySlug = "", pricingScheme = PricingScheme.UNIT))
    private val _loading = MutableStateFlow(false)
    private val _error = MutableStateFlow<FieldsError?>(null)

    val uiState: StateFlow<EditScreenUiState> = _uiState
    val item: StateFlow<ItemEntity> = _item
    val loading: StateFlow<Boolean> = _loading
    val error: StateFlow<FieldsError?> = _error

    private suspend fun getItem(){
        if (itemId == null){
            _uiState.emit(EditScreenUiState.Idle)
            return
        }
        try {
            itemRepository.getItem(itemId).collect { item ->
                if(item == null){
                    _uiState.emit(EditScreenUiState.Error("Failed to fetch item with id: $itemId"))
                }else{
                    _uiState.emit(EditScreenUiState.Idle)
                    _item.emit(item)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    private fun validate(): FieldsError? {
        val itm = _item.value

        val nameError = if (itm.name.isBlank()) "The item title is required" else null
        val descriptionError = if (itm.description.isNullOrBlank()) "The item description is required" else null
        val skuError = if (itm.sku.isNullOrBlank()) "The item stock keeping unit is required" else null
        val categoryError = if (itm.categorySlug.isBlank()) "The item category is required" else null
        val imagesError = if (itm.images.isNullOrEmpty()) "Add at least one image" else null

        val error = FieldsError(
            name = nameError,
            description = descriptionError,
            sku = skuError,
            category = categoryError,
            images = imagesError
        )

        // Return FieldsError only if at least one field has error, otherwise null
        return if (error.hasAnyError()) error.copy(general = "Some fields require attention") else null
    }
    fun onEdit(itm: ItemEntity){
        _item.value = itm
    }

    fun handleSave(navController: NavController) {
        viewModelScope.launch {
            _loading.emit(true)
            val error = validate()
            if(error != null){
                _error.emit(error)
                _loading.emit(false)
                return@launch
            }
            _error.emit(null)
            val item = _item.value
            if(item.storeId < 1){
                _error.emit(FieldsError(general = "Invalid storeId."))
                return@launch
            }
            try {
                if(itemId == null){
                    itemRepository.insertItem(item)
                }else {
                    itemRepository.updateItem(item)
                }
                _loading.emit(false)
                navController.popBackStack()
            }catch (e: Exception){
                e.printStackTrace()
                _loading.emit(false)
            }
        }
    }

    init {
        viewModelScope.launch {
            if(storeId == null){
                _uiState.emit(EditScreenUiState.Error("Error: Failed to identify the target store. Store id is null."))
                return@launch
            }
            getItem()
        }
    }
}

sealed class EditScreenUiState {
    data object Loading: EditScreenUiState()
    data object Idle: EditScreenUiState()
    data class Error(val message: String): EditScreenUiState()
}

data class FieldsError(
    val name: String? = null,
    val description: String? = null,
    val sku: String? = null,
    val category: String? = null,
    val images: String? = null,
    val general: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpecForm(onAdd: (key: String, value: Any)-> Unit, itemSpecs: List<String>){
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    IconButton(onClick = { expanded = true }) {
        Icon(
            painter = painterResource(R.drawable.plus),
            contentDescription = "Add Specification",
            modifier = Modifier.size(IconSize.NORMAL),
            tint = colorScheme.primary
        )
    }

    if(expanded){
        ModalBottomSheet(
            onDismissRequest = { expanded = false },
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                AppText("Add item specification.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                AppText("Specify the key, e.g. Color and value(s) e.g. Blue, Red, etc", color = colorScheme.onBackground.copy(.6f))
                TextInputField(
                    key,
                    {
                        key = it
                        error = null
                    },
                    label = "KEY",
                    placeHolder = "e.g Color",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                TextInputField(
                    value,
                    {
                        value = it
                        error = null
                    },
                    label = "VALUE",
                    placeHolder = "e.g Blue, Red",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                if (!error.isNullOrEmpty()){
                    AppText(error!!, color = colorScheme.error)
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton({expanded = false}) {
                        AppText("Cancel")
                    }
                    Spacer(Modifier.width(Spacing.MD))
                    TextButton({
                        if(key.isEmpty()){
                            error = "The specification key can not be empty"
                        }else if (itemSpecs.contains(key)){
                            error = "The key $key already exists. Edit it from the main form"
                        }else{
                            onAdd(key, value)
                            key= ""
                            value = ""
                            expanded = false
                        }
                    }) {
                        AppText("Save", color = colorScheme.primary)
                    }
                }
            }
        }
    }
}