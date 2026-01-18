package com.example.ratandroidsdkexample

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rat_ads.RatAdsSdk
import com.example.rat_ads.domain.entities.AdEntity
import com.example.rat_ads.domain.value_objects.AdPlacement
import com.example.rat_ads.domain.value_objects.SessionState
import com.example.ratandroidsdkexample.ui.theme.RatAndroidSDKExampleTheme
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

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
        
        // Initialize the Rat Ads SDK - just pass context and API key!
        RatAdsSdk.initialize(this, "YOUR_API_KEY")
        
        enableEdgeToEdge()
        setContent {
            RatAndroidSDKExampleTheme {
                AdDemoScreen()
            }
        }

        checkAndRequestPermission()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Disconnect from the gateway when activity is destroyed
        RatAdsSdk.disconnect()
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
    // 4. Permission DENIED (w/ Don't Ask Again detection)
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
            .setMessage("We need location access for targeted ads.")
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
                "You have permanently denied the location permission. " +
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
fun AdDemoScreen() {
    val connectionState by RatAdsSdk.connectionState.collectAsState()
    var loadedAd by remember { mutableStateOf<AdEntity?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Rat Ads SDK Demo",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Connection Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (connectionState) {
                        is SessionState.Connected -> MaterialTheme.colorScheme.primaryContainer
                        is SessionState.Connecting -> MaterialTheme.colorScheme.secondaryContainer
                        is SessionState.Error -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Connection Status",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = when (val state = connectionState) {
                            is SessionState.Connected -> "✅ Connected (Session: ${state.session.sessionId.take(8)}...)"
                            is SessionState.Connecting -> "🔄 Connecting..."
                            is SessionState.Error -> "❌ Error: ${state.message}"
                            is SessionState.Disconnected -> "⚪ Disconnected: ${state.reason}"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Load Ad Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = RatAdsSdk.loadAd(AdPlacement.BANNER)
                            result.onSuccess { ad ->
                                loadedAd = ad
                            }
                            result.onFailure { error ->
                                errorMessage = error.message
                            }
                            isLoading = false
                        }
                    },
                    enabled = connectionState is SessionState.Connected && !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Load Banner")
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = RatAdsSdk.loadAd(AdPlacement.INTERSTITIAL)
                            result.onSuccess { ad ->
                                loadedAd = ad
                            }
                            result.onFailure { error ->
                                errorMessage = error.message
                            }
                            isLoading = false
                        }
                    },
                    enabled = connectionState is SessionState.Connected && !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Load Interstitial")
                }
            }
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator()
            }
            
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: $error",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Loaded Ad Display
            loadedAd?.let { ad ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Loaded Ad",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ID: ${ad.id}")
                        Text("Title: ${ad.title}")
                        Text("Description: ${ad.description}")
                        Text("Placement: ${ad.placement}")
                        Text("TTL: ${ad.ttlSeconds}s")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        RatAdsSdk.reportImpression(ad.id, 1000)
                                    }
                                }
                            ) {
                                Text("Report Impression")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        RatAdsSdk.reportClick(ad.id, 100f, 100f)
                                    }
                                }
                            ) {
                                Text("Report Click")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Reconnect button
            if (connectionState is SessionState.Error || connectionState is SessionState.Disconnected) {
                Button(
                    onClick = {
                        RatAdsSdk.reconnect()
                    }
                ) {
                    Text("Reconnect")
                }
            }
        }
    }
}

