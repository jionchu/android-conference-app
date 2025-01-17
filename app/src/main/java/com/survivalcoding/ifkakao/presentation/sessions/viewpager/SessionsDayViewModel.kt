package com.survivalcoding.ifkakao.presentation.sessions.viewpager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.ifkakao.domain.model.DayType
import com.survivalcoding.ifkakao.domain.model.Session
import com.survivalcoding.ifkakao.domain.usecase.SearchSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionsDayViewModel @Inject constructor(
    private val searchSessionsUseCase: SearchSessionsUseCase
) : ViewModel() {
    private val _sessions = MutableLiveData<List<Session>>()
    val sessions: LiveData<List<Session>> get() = _sessions

    fun getSessionsByDay(
        day: DayType,
        fields: MutableList<String>,
        keywords: MutableList<String>,
        companies: MutableList<String>
    ) = viewModelScope.launch {
        _sessions.value = searchSessionsUseCase.invoke(day, fields, keywords, companies)
    }
}