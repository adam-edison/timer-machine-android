package xyz.aprildown.timer.presentation.steplog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import xyz.aprildown.timer.domain.di.MainDispatcher
import xyz.aprildown.timer.domain.entities.StepStampEntity
import xyz.aprildown.timer.domain.usecases.record.SearchStepStamps
import xyz.aprildown.timer.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class StepLogViewModel @Inject constructor(
    @MainDispatcher mainDispatcher: CoroutineDispatcher,
    private val searchStepStamps: SearchStepStamps
) : BaseViewModel(mainDispatcher) {

    private val _stamps = MutableLiveData<List<StepStampEntity>>()
    val stamps: LiveData<List<StepStampEntity>> = _stamps

    private var searchJob: Job? = null

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = launch {
            _stamps.value = searchStepStamps(query.trim())
        }
    }
}
