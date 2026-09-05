package xyz.aprildown.timer.app.timer.list.steplog

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import xyz.aprildown.timer.app.timer.list.R
import xyz.aprildown.timer.app.timer.list.databinding.FragmentStepLogBinding
import xyz.aprildown.timer.presentation.steplog.StepLogViewModel

@AndroidEntryPoint
class StepLogFragment : Fragment(R.layout.fragment_step_log) {

    private val viewModel: StepLogViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context
        val binding = FragmentStepLogBinding.bind(view)

        val stepLogAdapter = StepLogAdapter()
        binding.listStepLog.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = stepLogAdapter
        }

        viewModel.stamps.observe(viewLifecycleOwner) { stamps ->
            stepLogAdapter.set(
                stamps.map { VisibleStepStamp.fromStepStampEntity(it, context) }
            )
            binding.textStepLogEmpty.isVisible = stamps.isEmpty()
            binding.listStepLog.isVisible = stamps.isNotEmpty()
        }

        binding.editStepLogSearch.addTextChangedListener {
            viewModel.search(it?.toString().orEmpty())
        }

        viewModel.search("")
    }
}
