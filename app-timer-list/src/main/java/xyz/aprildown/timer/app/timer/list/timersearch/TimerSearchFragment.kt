package xyz.aprildown.timer.app.timer.list.timersearch

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import xyz.aprildown.timer.app.base.ui.MainCallback
import xyz.aprildown.timer.app.base.utils.getDisplayName
import xyz.aprildown.timer.app.timer.list.R
import xyz.aprildown.timer.app.timer.list.databinding.FragmentTimerSearchBinding
import xyz.aprildown.timer.domain.entities.FolderEntity
import xyz.aprildown.timer.domain.entities.TimerInfo
import xyz.aprildown.timer.presentation.timersearch.TimerSearchViewModel

@AndroidEntryPoint
class TimerSearchFragment : Fragment(R.layout.fragment_timer_search) {

    private lateinit var mainCallback: MainCallback.ActivityCallback

    private val viewModel: TimerSearchViewModel by viewModels()

    private var latestFolders: List<FolderEntity> = emptyList()
    private var latestResults: List<TimerInfo> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainCallback = context as MainCallback.ActivityCallback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context
        val binding = FragmentTimerSearchBinding.bind(view)

        val searchAdapter = TimerSearchAdapter { result ->
            mainCallback.enterTimerScreen(view, result.timerId)
        }
        binding.listTimerSearch.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }

        fun refreshList() {
            val visibleResults = latestResults.map { timerInfo ->
                val folder = latestFolders.find { it.id == timerInfo.folderId }
                VisibleTimerSearchResult(
                    timerId = timerInfo.id,
                    timerName = timerInfo.name,
                    folderName = folder?.getDisplayName(context).orEmpty(),
                )
            }
            searchAdapter.set(visibleResults)
            binding.textTimerSearchEmpty.isVisible = visibleResults.isEmpty()
            binding.listTimerSearch.isVisible = visibleResults.isNotEmpty()
        }

        viewModel.folders.observe(viewLifecycleOwner) { folders ->
            latestFolders = folders
            refreshList()
        }
        viewModel.results.observe(viewLifecycleOwner) { results ->
            latestResults = results
            refreshList()
        }

        binding.editTimerSearch.addTextChangedListener {
            viewModel.search(it?.toString().orEmpty())
        }

        viewModel.search("")
    }
}
