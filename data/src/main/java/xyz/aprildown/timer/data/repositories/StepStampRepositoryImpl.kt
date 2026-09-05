package xyz.aprildown.timer.data.repositories

import dagger.Reusable
import xyz.aprildown.timer.data.db.StepStampDao
import xyz.aprildown.timer.data.mappers.StepStampMapper
import xyz.aprildown.timer.domain.entities.StepStampEntity
import xyz.aprildown.timer.domain.repositories.StepStampRepository
import javax.inject.Inject

@Reusable
internal class StepStampRepositoryImpl @Inject constructor(
    private val dao: StepStampDao,
    private val mapper: StepStampMapper
) : StepStampRepository {

    override suspend fun add(stamp: StepStampEntity): Int {
        return dao.add(mapper.mapTo(stamp)).toInt()
    }

    override suspend fun search(query: String): List<StepStampEntity> {
        return mapper.mapFrom(dao.search(query))
    }
}
