package com.example.doline.views.components

import android.graphics.drawable.shapes.RectShape
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun CameraScannerOverlay(
    isPaused: Boolean,
    onUpcScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    // Pass latest isPaused flag to analyzer
    val currentIsPaused by rememberUpdatedState(isPaused)

    LaunchedEffect(Unit) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        if (currentIsPaused){
            scanner.close()
            return@LaunchedEffect
        }
        val analyzer = MlKitAnalyzer(
            listOf(scanner),
            COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(context)
        ) { result ->
            val barcodes = result?.getValue(scanner)
            barcodes?.firstOrNull()?.rawValue?.let { upc ->
                onUpcScanned(upc)
            }
        }

        cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    AlertDialog(
        tonalElevation = 10.dp,
        onDismissRequest = {
            onClose()
        },
        shape = RoundedCornerShape(10.dp),
        confirmButton = {},
        text = {
            Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                AndroidView(
                    factory = { ctx -> PreviewView(ctx).apply { controller = cameraController } },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(10.dp))
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
                }
            }
        }
    )

}
