package xyz.aprildown.timer.domain.repositories

import kotlinx.coroutines.flow.Flow
import xyz.aprildown.timer.domain.entities.TimerEntity
import xyz.aprildown.timer.domain.entities.TimerInfo

interface TimerRepository {
    suspend fun items(): List<TimerEntity>
    suspend fun item(id: Int): TimerEntity?
    suspend fun add(item: TimerEntity): Int
    suspend fun save(item: TimerEntity): Boolean
    suspend fun delete(id: Int)
    suspend fun getTimerInfoByTimerId(timerId: Int): TimerInfo?
    fun getTimerInfoFlow(folderId: Long): Flow<List<TimerInfo>>
    fun getTimersFlow(folderId: Long): Flow<List<TimerEntity>>
    suspend fun getTimerInfo(folderId: Long): List<TimerInfo>

    /**
     * Newest-name-first is not guaranteed; matches [query] against the timer name across
     * every folder except the trash. A blank [query] returns every non-trashed timer.
     */
    suspend fun searchTimerInfo(query: String): List<TimerInfo>

    suspend fun changeTimerFolder(timerId: Int, folderId: Long)
    suspend fun moveFolderTimersToAnother(originalFolderId: Long, targetFolderId: Long)
}
