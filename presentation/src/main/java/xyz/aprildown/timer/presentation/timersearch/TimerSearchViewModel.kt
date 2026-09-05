package xyz.aprildown.timer.presentation.timersearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import xyz.aprildown.timer.domain.di.MainDispatcher
import xyz.aprildown.timer.domain.entities.FolderEntity
import xyz.aprildown.timer.domain.entities.TimerInfo
import xyz.aprildown.timer.domain.usecases.folder.GetFolders
import xyz.aprildown.timer.domain.usecases.invoke
import xyz.aprildown.timer.domain.usecases.timer.SearchTimers
import xyz.aprildown.timer.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class TimerSearchViewModel @Inject constructor(
    @MainDispatcher mainDispatcher: CoroutineDispatcher,
    private val searchTimers: SearchTimers,
    private val getFolders: GetFolders,
) : BaseViewModel(mainDispatcher) {

    private val _folders = MutableLiveData<List<FolderEntity>>()
    val folders: LiveData<List<FolderEntity>> = _folders

    private val _results = MutableLiveData<List<TimerInfo>>()
    val results: LiveData<List<TimerInfo>> = _results

    private var searchJob: Job? = null

    init {
        launch {
            _folders.value = getFolders()
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = launch {
            _results.value = searchTimers(query)
        }
    }
}
