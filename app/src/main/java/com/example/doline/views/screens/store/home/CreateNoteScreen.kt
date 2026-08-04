package com.example.doline.views.screens.store.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.NoteRepository
import com.example.doline.data.NotesEntity
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.FormScreen
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(navController: NavController, viewModel: CreateNoteScreenViewModel){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val uiState by viewModel.uiState.collectAsState()
    val title by viewModel.title.collectAsState()
    val body by viewModel.body.collectAsState()

    FormScreen(
        appBar = {
            TopAppBar(
                title = { AppText("Add new note", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.shadow(10.dp),
                scrollBehavior = scrollBehavior,
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    subtitleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(IconSize.NORMAL),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
            )
        },
    ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(Rounding.MD)
                    )
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD),
            ) {
                TextField(
                    value = title,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = FontSize.XL,
                        fontWeight = FontWeight.W800,

                    ),
                    placeholder = {
                        AppText(
                            "Notes title",
                            color = MaterialTheme.colorScheme.onBackground.copy(.4f),
                            variant = TextType.Heading
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Sentences
                    )

                )
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(.2f))
                TextField(
                    value = body,
                    onValueChange = { viewModel.onBodyChanged(it) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = FontSize.MD),
                    minLines = 5,
                    singleLine = false,
                    maxLines = 10,
                    placeholder = {
                        AppText(
                            "Start writing your note here...",
                            color = MaterialTheme.colorScheme.onBackground.copy(.4f),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 400.dp, max = 600.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Go,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }
            AppButton(
                "Save Notes",
                enabled = (title.isNotEmpty() && body.isNotEmpty()),
                isLoading = uiState is CreateNoteUiState.Loading,
                onClick = {
                    if(title.isNotEmpty() && body.isNotEmpty()){
                        viewModel.handleSubmit()
                        navController.popBackStack()
                    }
                },
                type = ButtonType.Primary, icon = R.drawable.save, iconTint = MaterialTheme.colorScheme.onPrimary, buttonHeight = 56.dp)

        }

}

@HiltViewModel
class CreateNoteScreenViewModel @Inject constructor(
    savedSate: SavedStateHandle,
    private val notesRepository: NoteRepository
): ViewModel(){
    val storeId = savedSate.get<Long>("storeId")

    private val _uiState = MutableStateFlow<CreateNoteUiState>(CreateNoteUiState.Idle)
    val uiState: StateFlow<CreateNoteUiState> = _uiState

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title
    private  val _body = MutableStateFlow("")
    val body: StateFlow<String> = _body

    fun onTitleChanged(t: String){
        _title.value = t
    }
    fun onBodyChanged(b: String){
        _body.value = b
    }

    fun handleSubmit(){
        _uiState.value = CreateNoteUiState.Loading
        val t = _title.value
        val b = _body.value

        if (t.isBlank() || b.isBlank() || storeId == null){
            _uiState.value = CreateNoteUiState.Error("Both the title and body are required.")
            return
        }

        viewModelScope.launch {
            try {
                val note = NotesEntity(
                    title = t,
                    body = b,
                    storeId = storeId
                )

                notesRepository.insert(note)
                _uiState.value = CreateNoteUiState.Success
            }catch (e: Exception){
                _uiState.value = CreateNoteUiState.Error(e.message ?: "Unknown database error.")
            }
        }
    }

}

sealed class CreateNoteUiState {
    data object Idle: CreateNoteUiState()
    data object Success: CreateNoteUiState()
    data object Loading: CreateNoteUiState()
    data class Error(val message: String): CreateNoteUiState()
}