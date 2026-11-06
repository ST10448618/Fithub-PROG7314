@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.example.fithub.ui.screens.food.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.components.AppHeader
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.theme.CardWhite
import com.example.fithub.ui.theme.ErrorRed
import com.example.fithub.ui.theme.TextSecondary
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    onBack: () -> Unit,
    onFoodResolved: (foodId: String) -> Unit,
    viewModel: BarcodeScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // When a food is resolved, navigate.
    LaunchedEffect(state.resolvedFood) {
        state.resolvedFood?.let { food ->
            onFoodResolved(food.id)
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionDenied = !granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permLauncher.launch(Manifest.permission.CAMERA)
    }

    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }

    val previewView = remember { PreviewView(context) }
    var scanned by remember { mutableStateOf(false) }

    // Reset `scanned` when the user taps "Try again"
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage == null && state.resolvedFood == null) {
            scanned = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppHeader(
            title = "Scan Barcode",
            onBack = onBack,
            titleColor = Color.White
        )

        // ---- Permission gate ----
        if (!hasPermission) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (permissionDenied) {
                        "Camera permission denied.\nEnable it in Settings to scan barcodes."
                    } else {
                        "Requesting camera permission…"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
            return@Column
        }

        // ---- Error state (product not found / network error) ----
        if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardWhite)
                        .padding(24.dp)
                ) {
                    Text(
                        "❌",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Couldn't find this product",
                        style = MaterialTheme.typography.titleLarge,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        text = "Try again",
                        onClick = {
                            viewModel.consumeError()
                            scanned = false
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "or search the product manually",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }
            return@Column
        }

        // ---- Normal scanning state ----
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { previewView.apply { scaleType = PreviewView.ScaleType.FILL_CENTER } },
                modifier = Modifier.fillMaxSize()
            )

            // Scan frame
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(280.dp, 170.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Text(
                text = if (state.isLookingUp) {
                    "Looking up product…"
                } else {
                    "Align the barcode inside the frame"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            )

            // Loading overlay
            if (state.isLookingUp) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // ---- Camera binding ----
        LaunchedEffect(hasPermission) {
            if (!hasPermission) return@LaunchedEffect
            previewView.post {
                try {
                    val future = ProcessCameraProvider.getInstance(context)
                    future.addListener({
                        try {
                            val provider = future.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { ia ->
                                    ia.setAnalyzer(executor) { imageProxy ->
                                        if (scanned || imageProxy.image == null) {
                                            imageProxy.close()
                                            return@setAnalyzer
                                        }
                                        val input = InputImage.fromMediaImage(
                                            imageProxy.image!!,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        BarcodeScanning.getClient()
                                            .process(input)
                                            .addOnSuccessListener { barcodes ->
                                                val value = barcodes
                                                    .firstOrNull { it.format != Barcode.FORMAT_UNKNOWN }
                                                    ?.rawValue
                                                if (!value.isNullOrBlank() && !scanned) {
                                                    scanned = true
                                                    viewModel.lookup(value)
                                                }
                                            }
                                            .addOnCompleteListener { imageProxy.close() }
                                    }
                                }

                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        } catch (t: Throwable) {
                            android.util.Log.e("BarcodeScanner", "Camera bind failed", t)
                        }
                    }, ContextCompat.getMainExecutor(context))
                } catch (t: Throwable) {
                    android.util.Log.e("BarcodeScanner", "Camera provider failed", t)
                }
            }
        }
    }
}