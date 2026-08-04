package com.example.doline.views.screens.stores


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.Store
import com.example.doline.data.StoreCategory
import com.example.doline.data.StoreRepository
import com.example.doline.getPersistentImageUrl
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.FormScreen
import com.example.doline.views.components.ImageView
import com.example.doline.views.components.TextInputField
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
fun CreateStoreScreen(navController: NavHostController, viewModel: ScreenViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val uiState = viewModel.uiState.collectAsState()
    val image = viewModel.image.collectAsState(null)
    val name = viewModel.name.collectAsState("")
    val description = viewModel.description.collectAsState("")
    val slug = viewModel.slug.collectAsState("")
    val address = viewModel.address.collectAsState("")
    val category = viewModel.category.collectAsState(StoreCategory.DOLINE_STORE)
    val isCatDropDownOpen = viewModel.isCatDropDownOpen.collectAsState(false)

    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val persistentUrl = if (uri != null) getPersistentImageUrl(context, uri) else null
        viewModel.onImageChanged(persistentUrl)
    }

    if (uiState.value is UiState.Success) {
        val storeId = (uiState.value as UiState.Success).storeId
        navController.navigate("stores/$storeId") {
            popUpTo("stores/create-store") { inclusive = true }
        }
    }

    FormScreen(
        appBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                ),
                title = {
                    AppText(
                        "Create a new store",
                        color = colorScheme.onBackground,
                        variant = TextType.Heading
                    )
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.shadow(10.dp),
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
        }
    ) {
        AppText("STORE FRONT IMAGE", variant = TextType.LabelSmall)
        Box(
            Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(Rounding.MD))
                .clickable {
                    imagePicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
                .background(colorScheme.surface.copy(.6f), RoundedCornerShape(Rounding.MD)),
            contentAlignment = Alignment.Center
        ) {
            if (image.value == null) {
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
                        "Add a store cover Image",
                        color = colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                ImageView(image.value)
            }
        }
        ExposedDropdownMenuBox(
            expanded = isCatDropDownOpen.value,
            onExpandedChange = { viewModel.openCloseCatDropDown(it) }
        ) {
            TextInputField(
                value = category.value.name.replace("_", " ").capitalize().capitalize(),
                onValueChange = {},
                placeHolder = "eg. Unit Pricing",
                label = "CATEGORY",
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isCatDropDownOpen.value) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
            )
            ExposedDropdownMenu(
                expanded = isCatDropDownOpen.value,
                containerColor = colorScheme.surface,
                onDismissRequest = { viewModel.openCloseCatDropDown(false) }
            ) {
                StoreCategory.getStoreCategories().forEach { entry ->
                    DropdownMenuItem(
                        text = { AppText(entry.name.replace("_", " ").capitalize()) },
                        onClick = {
                            viewModel.onCategoryChanged(entry)
                            viewModel.openCloseCatDropDown(false)
                        }
                    )
                }
            }
        }
        TextInputField(
            value = name.value,
            onValueChange = { viewModel.onNameChanged(it) },
            label = "NAME",
            placeHolder = "eg. Doline Electronics store",
            singleLine = true
        )
        TextInputField(
            value = slug.value,
            onValueChange = { viewModel.onSlugChanged(it) },
            label = "SLUG",
            placeHolder = "eg. Quality Electronics",
            singleLine = true
        )
        TextInputField(
            value = description.value,
            onValueChange = { viewModel.onDescriptionChanged(it) },
            label = "DESCRIPTION",
            placeHolder = "Describe what your store sells",
            singleLine = false,
            minLines = 2,
        )
        TextInputField(
            value = address.value,
            onValueChange = { viewModel.onAddressChanged(it) },
            label = "ADDRESS",
            placeHolder = "eg. Plot 18 Nahayima Road, Busia(U)",
            singleLine = false,
            minLines = 2,
        )

        if (uiState.value is UiState.Error) {
            AppText(
                (uiState.value as UiState.Error).error,
                color = colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(colorScheme.errorContainer)
                    .padding(Spacing.MD)
            )
            Spacer(modifier = Modifier.height(Spacing.MD))
        }
        AppButton(
                "Save",
                { viewModel.handleSubmit() },
                type = ButtonType.Primary,
                isLoading = uiState.value is UiState.Loading
            )
    }
}


@HiltViewModel
class ScreenViewModel @Inject constructor(private  val storeRepository: StoreRepository): ViewModel(){
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: Flow<String> = _name
    private val _description = MutableStateFlow("")
    val description: Flow<String> = _description
    private val _address = MutableStateFlow("")
    val address: Flow<String> = _address
    private val _slug = MutableStateFlow("")
    val slug: Flow<String> = _slug
    private val _category = MutableStateFlow(StoreCategory.DOLINE_STORE)
    val category: Flow<StoreCategory> = _category
    private  val _isCatDropDownOpen = MutableStateFlow(false)
    val isCatDropDownOpen: Flow<Boolean> = _isCatDropDownOpen

    private  val _image = MutableStateFlow<String?>(null)
    val image: Flow<String?> = _image

    fun onNameChanged(string: String) {
        _name.value = string
    }

    fun onDescriptionChanged(string: String) {
        _description.value = string
    }

    fun onSlugChanged(string: String) {
        _slug.value = string
    }

    fun onImageChanged(string: String?) {
        _image.value = string
    }

    fun onAddressChanged(string: String) {
        _address.value = string
    }

    fun onCategoryChanged(category: StoreCategory) {
        _category.value = category
    }

    fun openCloseCatDropDown(state: Boolean){
        _isCatDropDownOpen.value = state
    }

  fun handleSubmit(){
         _uiState.value = UiState.Loading
        if(_name.value.isEmpty() || _description.value.isEmpty()){
           _uiState.value = UiState.Error("Fill out all required fields")
            return
        }

        val store = Store(
            name = _name.value,
            category = _category.value,
            description = _description.value,
            image = _image.value,
            slug = _slug.value,
            address = _address.value
        )

      viewModelScope.launch {
          try {
              val storeId = storeRepository.insertStore(store)
              _uiState.value = UiState.Success(storeId)
          }catch (e: Exception){
              _uiState.value = UiState.Error(e.message ?: "Unknown error.")
          }
      }
    }
}


sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class  Success(val storeId: Long): UiState()
    data class  Error(val error: String): UiState()
}

