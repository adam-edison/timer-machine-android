package xyz.aprildown.timer.data.repositories

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.aprildown.timer.data.db.MachineDatabase
import xyz.aprildown.timer.data.mappers.BehaviourMapper
import xyz.aprildown.timer.data.mappers.StepMapper
import xyz.aprildown.timer.data.mappers.StepOnlyMapper
import xyz.aprildown.timer.data.mappers.TimerInfoMapper
import xyz.aprildown.timer.data.mappers.TimerMapper
import xyz.aprildown.timer.data.mappers.TimerMoreMapper
import xyz.aprildown.timer.domain.TestData
import xyz.aprildown.timer.domain.entities.FolderEntity
import xyz.aprildown.timer.domain.entities.TimerEntity
import xyz.aprildown.timer.domain.entities.toTimerInfo
import xyz.aprildown.timer.domain.repositories.TimerRepository

class TimerRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = MachineDatabase.createInMemoryDatabase(context)
    private val timerRepository: TimerRepository = TimerRepositoryImpl(
        timerDao = database.timerDao(),
        timerMapper = TimerMapper(StepMapper(StepOnlyMapper(BehaviourMapper())), TimerMoreMapper()),
        timerInfoMapper = TimerInfoMapper(),
    )

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun items_item_add_idAndNames() = runTest {
        val list = listOf(
            TestData.fakeTimerSimpleA,
            TestData.fakeTimerSimpleB,
            TestData.fakeTimerAdvanced
        )
        val ids = Array(list.size) { TimerEntity.NULL_ID }
        // At some at first
        list.forEachIndexed { index, timerEntity ->
            ids[index] = timerRepository.add(timerEntity)
        }

        // items and add
        val result: List<TimerEntity> = timerRepository.items()
        assertEquals(3, result.size)
        result.forEachIndexed { index, timerEntity ->
            assertEquals(list[index].copy(id = ids[index]), timerEntity)
        }

        // item
        val item = timerRepository.item(ids[0])
        assertEquals(list[0], item)

        assertEquals(list, timerRepository.items())
    }

    @Test
    fun save() = runTest {
        val id = timerRepository.add(TestData.fakeTimerSimpleA)

        val item = timerRepository.item(id)
        assertEquals(TestData.fakeTimerSimpleA.copy(id = id), item)

        assertTrue(timerRepository.save(TestData.fakeTimerAdvanced.copy(id = id)))
        val new = timerRepository.item(id)
        assertEquals(TestData.fakeTimerAdvanced.copy(id = id), new)
    }

    @Test
    fun delete() = runTest {
        val id = timerRepository.add(TestData.fakeTimerSimpleA)

        val item = timerRepository.item(id)
        assertEquals(TestData.fakeTimerSimpleA.copy(id = id), item)

        timerRepository.delete(id)

        assertNull(timerRepository.item(id))
    }

    @Test
    fun getTimersFlow_returnsFullEntitiesForTheFolder() = runTest {
        val defaultFolderId = TestData.fakeTimerSimpleA.folderId
        val otherFolderId = defaultFolderId + 1

        val defaultFolderTimerId = timerRepository.add(TestData.fakeTimerSimpleA)
        timerRepository.add(TestData.fakeTimerAdvanced.copy(folderId = otherFolderId))

        val result = timerRepository.getTimersFlow(defaultFolderId).first()

        assertEquals(
            listOf(TestData.fakeTimerSimpleA.copy(id = defaultFolderTimerId)),
            result
        )
    }

    @Test
    fun searchTimerInfo_matchesNameAcrossFolders_excludingTrash() = runTest {
        // Distinct from both FolderEntity.FOLDER_DEFAULT and FolderEntity.FOLDER_TRASH.
        val otherFolderId = TestData.fakeTimerSimpleA.folderId + 100

        val alphaId = timerRepository.add(TestData.fakeTimerSimpleA)
        val bravoId = timerRepository.add(
            TestData.fakeTimerSimpleB.copy(folderId = otherFolderId)
        )
        val trashedId = timerRepository.add(
            TestData.fakeTimerAdvanced.copy(folderId = FolderEntity.FOLDER_TRASH)
        )

        assertEquals(
            listOf(
                TestData.fakeTimerSimpleA.copy(id = alphaId).toTimerInfo(),
                TestData.fakeTimerSimpleB.copy(id = bravoId, folderId = otherFolderId).toTimerInfo(),
            ),
            timerRepository.searchTimerInfo("Timer")
        )

        assertEquals(
            listOf(TestData.fakeTimerSimpleA.copy(id = alphaId).toTimerInfo()),
            timerRepository.searchTimerInfo("Alpha")
        )

        assertTrue(
            timerRepository.searchTimerInfo("Advanced").none { it.id == trashedId }
        )
    }
}
