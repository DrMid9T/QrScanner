import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import android.util.Base64
import android.util.Log
import com.first.qrcodescanner.R
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class QrFragment : Fragment() {

    private lateinit var scannedTextView: TextView
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
            IntentIntegrator.forSupportFragment(this).initiateScan()
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
                val encryptedData = result.contents
                val keyBase64 = "+MyRGJ6KeOm6Qnkaz0H7cA=="
                val key: Key = SecretKeySpec(Base64.decode(keyBase64, Base64.DEFAULT), "AES")

                try {
                    val decryptedData = decrypt(encryptedData, key)
                    scannedTextView.text = "Decrypted: $decryptedData"

                    // Show the green popup if decryption is successful
                    showPopup("Authenticated!", "Your Product Has Been Authenticated!")
                } catch (e: Exception) {
                    Log.e("QrFragment", "Error decrypting data: ${e.message}")
                    scannedTextView.text = "Failed to decrypt data"

                    // Show the red popup if decryption fails
                    showRedPopup("QR Code Not Recognized!", "Please Scan The Right Qr Code!")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun decrypt(data: String, key: Key): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))
        return String(decryptedBytes)
    }

    private fun showPopup(title: String, message: String) {
        val dialogView = layoutInflater.inflate(R.layout.popup_layout, null)
        val popupMessageTextView = dialogView.findViewById<TextView>(R.id.popupMessage)

        popupMessageTextView.text = message

        val okButton = dialogView.findViewById<Button>(R.id.okButton)

        popupDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        okButton.setOnClickListener {
            popupDialog.dismiss()
            scannedTextView.text = ""
        }

        popupDialog.show()
    }

    private fun showRedPopup(title: String, message: String) {
        val dialogView = layoutInflater.inflate(R.layout.popup_layout_red, null)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        popupDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            popupDialog.dismiss()
            scannedTextView.text = ""
        }

        popupDialog.show()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }
}