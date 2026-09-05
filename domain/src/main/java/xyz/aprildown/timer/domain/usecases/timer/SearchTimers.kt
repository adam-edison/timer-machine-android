package xyz.aprildown.timer.domain.usecases.timer

import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import xyz.aprildown.timer.domain.di.IoDispatcher
import xyz.aprildown.timer.domain.entities.TimerInfo
import xyz.aprildown.timer.domain.repositories.TimerRepository
import xyz.aprildown.timer.domain.usecases.CoroutinesUseCase
import javax.inject.Inject

@Reusable
class SearchTimers @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val repository: TimerRepository
) : CoroutinesUseCase<String, List<TimerInfo>>(dispatcher) {
    override suspend fun create(params: String): List<TimerInfo> {
        return repository.searchTimerInfo(params.trim())
    }
}
