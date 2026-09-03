package xyz.aprildown.timer.domain.usecases.timer

import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import xyz.aprildown.timer.domain.di.IoDispatcher
import xyz.aprildown.timer.domain.entities.TimerEntity
import xyz.aprildown.timer.domain.repositories.TimerRepository
import javax.inject.Inject

@Reusable
class GetTimersFlow @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val repository: TimerRepository,
) {
    fun get(folderId: Long): Flow<List<TimerEntity>> {
        return repository.getTimersFlow(folderId).flowOn(dispatcher)
    }
}
