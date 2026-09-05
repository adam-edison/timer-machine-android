package xyz.aprildown.timer.data.datas

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * No foreign key to [TimerData]: [timerName]/[stepName] are plain-text snapshots taken at
 * record time specifically so this log stays readable and searchable after the source timer
 * or step is renamed or deleted — unlike [TimerStampData], rows here are never cleaned up
 * when their timer is deleted.
 */
@Keep
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "StepStamp",
    indices = [Index("timerId")]
)
internal data class StepStampData(
    @Json(name = "id")
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @Json(name = "timerId")
    @ColumnInfo(name = "timerId")
    val timerId: Int,

    @Json(name = "timerName")
    @ColumnInfo(name = "timerName")
    val timerName: String,

    @Json(name = "stepName")
    @ColumnInfo(name = "stepName")
    val stepName: String,

    @Json(name = "timestamp")
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @Json(name = "confirmMethod")
    @ColumnInfo(name = "confirmMethod")
    val confirmMethod: String
)
