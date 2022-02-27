package com.example.connecttowifidemo.comp

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequest(
    permission: String = Manifest.permission.CAMERA,
    onPermissionPermanentlyDenied: @Composable () -> Unit,
    onPermissionDenied: @Composable () -> Unit,
    content: @Composable () -> Unit
) {

    val permissionState = rememberPermissionState(permission)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionState.launchPermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    when (permissionState.status) {
        is PermissionStatus.Granted -> {
            content()
        }
        is PermissionStatus.Denied -> {
            if (permissionState.status.shouldShowRationale) {
                onPermissionDenied()
            } else {
                onPermissionPermanentlyDenied()
            }
        }
    }
}


@Composable
fun PermissionDeniedDialog(isPermanently: Boolean = false) {

    var shouldShowDialog by remember {
        mutableStateOf(true)
    }

    AnimatedVisibility(visible = shouldShowDialog) {
        Dialog(
            onDismissRequest = { shouldShowDialog = shouldShowDialog.not() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colors.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.End
            ) {

                val title =
                    if (isPermanently) "Permission Denied Permanently" else "Permission is needed"
                val description =
                    if (isPermanently) "Permission has been permanently denied to access scanner you should go to settings and enable it"
                    else "Permission is required in order to access camera features"

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
            }
        }
    }
}