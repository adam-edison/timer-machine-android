package xyz.aprildown.timer.domain.usecases.record

import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import xyz.aprildown.timer.domain.di.IoDispatcher
import xyz.aprildown.timer.domain.entities.StepStampEntity
import xyz.aprildown.timer.domain.repositories.StepStampRepository
import xyz.aprildown.timer.domain.usecases.CoroutinesUseCase
import javax.inject.Inject

@Reusable
class SearchStepStamps @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val repository: StepStampRepository
) : CoroutinesUseCase<String, List<StepStampEntity>>(dispatcher) {
    override suspend fun create(params: String): List<StepStampEntity> {
        return repository.search(params)
    }
}
