package xyz.aprildown.timer.domain.usecases.record

import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import xyz.aprildown.timer.domain.di.IoDispatcher
import xyz.aprildown.timer.domain.entities.StepStampEntity
import xyz.aprildown.timer.domain.repositories.AppDataRepository
import xyz.aprildown.timer.domain.repositories.StepStampRepository
import xyz.aprildown.timer.domain.usecases.CoroutinesUseCase
import javax.inject.Inject

@Reusable
class AddStepStamp @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val repository: StepStampRepository,
    private val appDataRepository: AppDataRepository
) : CoroutinesUseCase<StepStampEntity, Int>(dispatcher) {
    override suspend fun create(params: StepStampEntity): Int {
        return repository.add(params).also {
            appDataRepository.notifyDataChanged()
        }
    }
}
