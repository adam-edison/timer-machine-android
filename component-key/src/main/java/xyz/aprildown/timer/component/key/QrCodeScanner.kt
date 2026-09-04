package xyz.aprildown.timer.component.key

import android.app.Activity
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Wraps Google Play services' Code Scanner API: a standalone system-provided scanning UI
 * (no CameraX pipeline, no CAMERA permission of our own to request — the scanner module
 * handles its own camera access) that hands back the raw QR content on a successful scan.
 */
object QrCodeScanner {
    fun scan(
        activity: Activity,
        onSuccess: (rawValue: String) -> Unit,
        onCancel: () -> Unit = {},
        onFailure: (Exception) -> Unit = {},
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        GmsBarcodeScanning.getClient(activity, options)
            .startScan()
            .addOnSuccessListener { barcode ->
                val value = barcode.rawValue
                if (value != null) {
                    onSuccess(value)
                } else {
                    onFailure(IllegalStateException("Scanned QR code has no content"))
                }
            }
            .addOnCanceledListener(onCancel)
            .addOnFailureListener(onFailure)
    }
}
