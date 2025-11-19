package com.example.ratandroidsdkexample

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rat_ads.RatAdsSdk
import com.example.ratandroidsdkexample.ui.theme.RatAndroidSDKExampleTheme
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class MainActivity : ComponentActivity() {

    // Permission launcher
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onPermissionGranted()
            } else {
                handlePermissionDenied()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RatAdsSdk.initialize(this, "YOUR_API_KEY");
        enableEdgeToEdge()
        setContent {
            RatAndroidSDKExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        checkAndRequestPermission()
    }

    // ---------------------------------------------
// 1. Check if permission already granted
// ---------------------------------------------
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ---------------------------------------------
// 2. Main function to check + request
// ---------------------------------------------
    private fun checkAndRequestPermission() {
        if (hasPermission()) {
            onPermissionGranted()
        } else {
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ---------------------------------------------
// 3. Permission GRANTED
// ---------------------------------------------
    private fun onPermissionGranted() {
        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
        // Continue your logic...
    }

    // ---------------------------------------------
// 4. Permission DENIED (w/ Don’t Ask Again detection)
// ---------------------------------------------
    private fun handlePermissionDenied() {
        val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (shouldShow) {
            // Normal denial (user just pressed "Deny")
            showPermissionExplanationDialog()
        } else {
            // User selected "Don't Ask Again" OR denied twice
            showGoToSettingsDialog()
        }
    }

    // ---------------------------------------------
// 5. Normal denial dialog
// ---------------------------------------------
    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("We need microphone access to proceed.")
            .setPositiveButton("Retry") { _, _ ->
                requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------------------------------------
// 6. "Don't Ask Again" dialog → Open app settings
// ---------------------------------------------
    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(
                "You have permanently denied the microphone permission. " +
                        "Please enable it manually in the app settings."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------------------------------------
// 7. Open the app's settings screen
// ---------------------------------------------
    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RatAndroidSDKExampleTheme {
        Greeting("Android")
    }
}

