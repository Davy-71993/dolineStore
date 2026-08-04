package com.example.doline.views.screens.store.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.doline.data.NoteRepository
import com.example.doline.data.NotesEntity
import com.example.doline.timestampToDate
import com.example.doline.timestampToTime
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(navController: NavController, viewModel: NotesScreenViewModel){
    val storeId = viewModel.storeId
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val columns = DeviceConfiguration.getGridWideColumnCount(deviceConfig)

    Screen(
        topAppBar = {
            TopAppBar(
                title = { AppText("Notes", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.padding(vertical = 0.dp).shadow(10.dp),
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
                actions = {
                    IconButton(onClick = { navController.navigate("store/notes/create")}) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "New Expense",
                            tint = colorScheme.onBackground,
                            modifier = Modifier.size(IconSize.BIG)
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("store/${storeId}/notes/create")},
                containerColor = colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    8.dp,
                    6.dp
                ),
                shape = RoundedCornerShape(Rounding.FULL),
                modifier = Modifier
                    .padding(Spacing.SM)
                    .absoluteOffset(
                        y = (-80).dp
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = "Add new note",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(IconSize.BIG)
                )
            }
        }
    ) {
        when(val state = uiState){
            is NotesScreenUiState.Loading -> {
                LoadingScreen()
            }
            is NotesScreenUiState.Error -> {
                ErrorMessage(state.message)
            }
            is NotesScreenUiState.Success -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    TextInputField(
                        value = searchText,
                        onValueChange = { viewModel.onSearchTextChanged(it) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.search),
                                contentDescription = "Search",
                                tint = colorScheme.onBackground.copy(.6f)
                            )
                        },
                        placeHolder = "Search for notes...",
                    )
                    val notes = state.notes
                    if (notes.isEmpty()){
                        Box(Modifier.fillMaxWidth().heightIn(min=100.dp).padding(100.dp), contentAlignment = Alignment.Center){
                            AppText("There are no notes.", textAlign = TextAlign.Center)
                        }
                    }else{
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colorScheme.background)
                                .padding(bottom = 60.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(notes.size) { index ->
                                val note = notes[index]
                                NoteCard(note)
                            }
                        }

                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteCard(note: NotesEntity){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Rounding.SM))
            .border(
                BorderStroke(
                    1.dp, colorScheme.onBackground.copy(.5f)
                ),
                shape = RoundedCornerShape(Rounding.SM)
            )
            .background(colorScheme.primary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(
                    colorScheme.primary,
                    shape = RoundedCornerShape(
                        topStart = Rounding.SM,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp,
                        bottomStart = Rounding.SM
                    )
                )
        ){}
        Column(
            modifier = Modifier
                .weight(1f)
                .background(colorScheme.surface)
                .padding(Spacing.MD),
            verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
        ) {
            AppText(note.title, variant = TextType.Label)
            AppText(note.body, color = colorScheme.onBackground.copy(.7f))
            Spacer(modifier = Modifier.height(10.dp))
            AppText(
                "${timestampToDate(note.createdAt)} | ${timestampToTime(note.createdAt)}",
                color = colorScheme.onBackground.copy(.5f),
                variant = TextType.Small,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@HiltViewModel
class NotesScreenViewModel @Inject constructor(
    saveStateHandle: SavedStateHandle,
    private val notesRepository: NoteRepository
): ViewModel(){
    val storeId = saveStateHandle.get<Long>("storeId")

    private val _uiState = MutableStateFlow<NotesScreenUiState>(NotesScreenUiState.Loading)
    val uiState: StateFlow<NotesScreenUiState> = _uiState
    private val _notes = MutableStateFlow<List<NotesEntity>>(emptyList())

    init {
        fetchNotes()
    }
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    fun onSearchTextChanged(text: String){
        _searchText.value = text

        viewModelScope.launch {
            _searchText.collect { query ->
                if (query.isBlank()){
                    _uiState.value = NotesScreenUiState.Success(_notes.value)
                }else{
                    val filtered = _notes.value.filter {
                        it.title.contains(query, ignoreCase = true)
                                ||
                                it.body.contains(query, ignoreCase = true)
                    }
                    _uiState.value = NotesScreenUiState.Success(filtered)
                }
            }
        }
    }

    private fun fetchNotes(){
        viewModelScope.launch {
            if(storeId == null){
                _uiState.value = NotesScreenUiState.Error("The store Id can not be null")
                return@launch
            }
            try {
                notesRepository.getNotes(storeId).collect {
                    _notes.value = it
                    _uiState.value = NotesScreenUiState.Success(it)
                }
            }catch (e: Exception){
                _uiState.value = NotesScreenUiState.Error(e.message ?: "Unknown database error.")
            }

        }
    }
}

sealed class NotesScreenUiState {
    data object Loading: NotesScreenUiState()
    data class Success(val notes: List<NotesEntity>): NotesScreenUiState()
    data class Error(val message: String): NotesScreenUiState()
}