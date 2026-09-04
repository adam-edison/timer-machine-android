package xyz.aprildown.timer.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviourEntityKtTest {

    @Test
    fun `QrScanAction round-trips through BehaviourEntity`() {
        val action = QrScanAction(
            nagIntervalSeconds = 30,
            content = "Scan to continue",
            savedCode = "my-secret-code",
        )

        assertEquals(action, action.toBehaviourEntity().toQrScanAction())
    }

    @Test
    fun `QrScanAction with no nag interval round-trips to zero`() {
        val action = QrScanAction(nagIntervalSeconds = 0)

        assertEquals(action, action.toBehaviourEntity().toQrScanAction())
    }

    @Test
    fun `blank saved code accepts any scanned code`() {
        val action = QrScanAction(savedCode = "")

        assertTrue(action.matches("anything"))
        assertTrue(action.matches(""))
    }

    @Test
    fun `set saved code only matches the exact scanned code`() {
        val action = QrScanAction(savedCode = "my-secret-code")

        assertTrue(action.matches("my-secret-code"))
        assertFalse(action.matches("some-other-code"))
    }
}
