package com.example.connecttowifidemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.connecttowifidemo.comp.CameraPreview
import com.example.connecttowifidemo.comp.PermissionDeniedDialog
import com.example.connecttowifidemo.comp.PermissionRequest
import com.example.connecttowifidemo.ui.theme.ConnectToWifiDemoTheme
import com.example.connecttowifidemo.util.is29AndAbove

class MainActivity : ComponentActivity() {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiManager: WifiManager
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private var hasScanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            ConnectToWifiDemoTheme {
                connectivityManager = remember {
                    getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                }
                wifiManager = remember {
                    getSystemService(Context.WIFI_SERVICE) as WifiManager
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PermissionRequest(
                        onPermissionPermanentlyDenied = {
                            PermissionDeniedDialog(isPermanently = true)
                        },
                        onPermissionDenied = {
                            PermissionDeniedDialog(isPermanently = false)
                        }
                    ) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onSuccess = { ssid, pw ->
                                if (!hasScanned) {
                                    hasScanned = true
                                    is29AndAbove {
                                        connect29AndAbove(
                                            ssid = ssid,
                                            passPhrase = pw
                                        )
                                    } ?: connectBelow29(ssid = ssid, pw)
                                }
                            },
                            onFailure = {
                                if (!hasScanned) {
                                    hasScanned = true
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Error's occurred ${it.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectBelow29(ssid: String, passPhrase: String) {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Enabling wifi...", Toast.LENGTH_SHORT).show()
            wifiManager.isWifiEnabled = true
        }

        val conf = WifiConfiguration()
        conf.SSID = String.format("\"%s\"", ssid)
        conf.preSharedKey = String.format("\"%s\"", passPhrase)
        wifiManager.addNetwork(conf)
        val list = wifiManager.configuredNetworks
        for (i in list) {
            if (i.SSID != null && i.SSID == "\"" + ssid + "\"") {
                wifiManager.disconnect()
                wifiManager.enableNetwork(i.networkId, true)
                wifiManager.reconnect()
                Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show()
                break
            }
        }
        hasScanned = false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun connect29AndAbove(ssid: String, passPhrase: String) {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Wifi is disabled please enable it to proceed", Toast.LENGTH_SHORT)
                .show()
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
            launcher.launch(panelIntent)
        }
        val networkSpecifier: NetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(passPhrase)
            .setIsHiddenSsid(true)
            .build()
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        connectivityManager.requestNetwork(networkRequest, mNetworkCallback)
    }

    private val mNetworkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onUnavailable() {
                super.onUnavailable()
                Toast.makeText(this@MainActivity, "Couldn't connect", Toast.LENGTH_SHORT).show()
                hasScanned = false
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // bind the wifi network so we don't lose connection after leaving the app
                connectivityManager.bindProcessToNetwork(network)
                Toast.makeText(this@MainActivity, "Connected", Toast.LENGTH_SHORT).show()
            }
        }
}


