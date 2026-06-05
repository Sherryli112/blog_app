package com.funtime.blog.ui.passport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.local.PassportStampEntity
import com.funtime.blog.data.repository.PassportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PassportViewModel @Inject constructor(
    passportRepository: PassportRepository
) : ViewModel() {
    val stamps: StateFlow<List<PassportStampEntity>> = passportRepository.stampsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
