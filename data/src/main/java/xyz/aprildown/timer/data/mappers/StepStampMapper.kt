package xyz.aprildown.timer.data.mappers

import dagger.Reusable
import xyz.aprildown.timer.data.datas.StepStampData
import xyz.aprildown.timer.domain.Mapper
import xyz.aprildown.timer.domain.entities.StepStampEntity
import javax.inject.Inject

@Reusable
internal class StepStampMapper @Inject constructor() : Mapper<StepStampData, StepStampEntity>() {
    override fun mapFrom(from: StepStampData): StepStampEntity {
        return StepStampEntity(
            id = from.id,
            timerId = from.timerId,
            timerName = from.timerName,
            stepName = from.stepName,
            timestamp = from.timestamp,
            confirmMethod = StepStampEntity.ConfirmMethod.valueOf(from.confirmMethod)
        )
    }

    override fun mapTo(from: StepStampEntity): StepStampData {
        return StepStampData(
            id = from.id,
            timerId = from.timerId,
            timerName = from.timerName,
            stepName = from.stepName,
            timestamp = from.timestamp,
            confirmMethod = from.confirmMethod.name
        )
    }
}
