import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.first.qrcodescanner.CaptureActivityPortrait
import com.first.qrcodescanner.R
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QrFragment : Fragment() {

    private lateinit var scannedTextView: TextView
    private lateinit var popupMessageTextView: TextView
    private lateinit var popupDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scannedTextView = view.findViewById(R.id.scannedText)

        // Start the scanner when the fragment view is created
        startScanner()
    }

    private fun startScanner() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val integrator = IntentIntegrator.forSupportFragment(this)
            integrator.setOrientationLocked(true)
            integrator.setBeepEnabled(true)
            integrator.setCaptureActivity(CaptureActivityPortrait::class.java)
            integrator.initiateScan()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator.forSupportFragment(this).initiateScan()
            } else {
                Toast.makeText(context, "Camera Permission Required", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                // Show the scanned message in the popup
                showPopup("Scanned: ${result.contents}")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showPopup(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.popup_layout, null)
        popupMessageTextView = dialogView.findViewById(R.id.popupMessage)

        // Set the scanned message in the TextView
        popupMessageTextView.text = message

        popupDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Optionally, you can reset the scannedTextView text here
                scannedTextView.text = "Scanned text will appear here"
            }
            .create()

        popupDialog.show()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }
}
