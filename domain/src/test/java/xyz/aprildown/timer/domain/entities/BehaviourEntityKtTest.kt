package xyz.aprildown.timer.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviourEntityKtTest {

    @Test
    fun `QrScanAction round-trips through BehaviourEntity`() {
        val action = QrScanAction(savedCode = "my-secret-code")

        assertEquals(action, action.toBehaviourEntity().toQrScanAction())
    }

    @Test
    fun `QrScanAction with a blank saved code round-trips too`() {
        val action = QrScanAction(savedCode = "")

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
