package com.example.memotrip_kroniq.ui.locationsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach


class LocationSearchViewModel(
    private val repository: LocationSearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _suggestions =
        MutableStateFlow<List<LocationSuggestion>>(emptyList())
    val suggestions: StateFlow<List<LocationSuggestion>> = _suggestions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun onQueryChange(text: String) {
        _query.value = text
    }

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .onEach { query ->
                    if (query.length < 4) {
                        _suggestions.value = emptyList()
                        _isLoading.value = false
                    }
                }
                .filter { it.length >= 4 }
                .collectLatest { query ->
                    _isLoading.value = true
                    _suggestions.value = repository.search(query)
                    _isLoading.value = false
                }
        }
    }
}