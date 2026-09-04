package xyz.aprildown.timer.app.base.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeConverterTest {

    @Test
    fun `produceCompactTime omits zero units`() {
        assertEquals("0s", 0L.produceCompactTime())
        assertEquals("54s", 54_000L.produceCompactTime())
        assertEquals("1h", 3_600_000L.produceCompactTime())
        assertEquals("1h4m", (3_600_000L + 4 * 60_000L).produceCompactTime())
        assertEquals("4h10s", (4 * 3_600_000L + 10_000L).produceCompactTime())
        assertEquals(
            "3h10m4s",
            (3 * 3_600_000L + 10 * 60_000L + 4_000L).produceCompactTime()
        )
    }
}
