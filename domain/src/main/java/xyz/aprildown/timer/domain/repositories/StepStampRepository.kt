package xyz.aprildown.timer.domain.repositories

import xyz.aprildown.timer.domain.entities.StepStampEntity

interface StepStampRepository {

    /**
     * @return The added entity's id
     */
    suspend fun add(stamp: StepStampEntity): Int

    /**
     * Newest first, matching [query] against either the timer name or the step name.
     * A blank [query] returns every stamp.
     */
    suspend fun search(query: String): List<StepStampEntity>
}
