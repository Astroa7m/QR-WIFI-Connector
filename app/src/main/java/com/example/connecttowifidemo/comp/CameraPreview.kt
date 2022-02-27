package com.example.connecttowifidemo.comp

import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.connecttowifidemo.util.QRCodeWIFIAnalyzer

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onSuccess: (ssid: String, pw: String) -> Unit,
    onFailure: (e: Exception) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).also { view ->
                view.scaleType = scaleType
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val previewUseCase = Preview.Builder()
                .build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(previewView.width, previewView.height))
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QRCodeWIFIAnalyzer(onSuccess, onFailure)
                    )
                }

                try {
                    ProcessCameraProvider.getInstance(context).get().apply {
                        unbindAll()
                        bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageAnalysis
                        )
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
            }

            previewView
        }
    )
}