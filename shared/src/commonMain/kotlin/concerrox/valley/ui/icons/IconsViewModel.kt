package concerrox.valley.ui.icons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import concerrox.valley.data.model.IconCategory
import concerrox.valley.data.repository.IconRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IconsViewModel(private val repository: IconRepository) : ViewModel() {

    val uiState: StateFlow<IconsUIState>
        field = MutableStateFlow<IconsUIState>(IconsUIState.Loading)

    init {
        viewModelScope.launch {
            uiState.value = IconsUIState.Success(repository.getCategories(), repository.canEdit)
        }
    }

}

sealed interface IconsUIState {
    object Loading : IconsUIState
    class Success(val categories: List<IconCategory>, val canEdit: Boolean) : IconsUIState
}