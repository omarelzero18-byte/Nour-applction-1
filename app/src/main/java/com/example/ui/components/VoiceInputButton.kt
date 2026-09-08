package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.speech.SpeechHelper
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.NourCyan
import com.example.ui.theme.NourViolet

@Composable
fun VoiceInputButton(
    speechHelper: SpeechHelper,
    onSpeechResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isListening by speechHelper.isListening.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            speechHelper.startListening(onSpeechResult)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isListening) 1.0f else 1.0f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isListening) DangerCoral else NourViolet.copy(alpha = 0.8f),
        label = "mic_bg"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(pulseScale)
                    .background(DangerCoral.copy(alpha = 0.3f), CircleShape)
            )
        }

        IconButton(
            onClick = {
                if (isListening) {
                    speechHelper.stopListening()
                } else {
                    if (hasPermission) {
                        speechHelper.startListening(onSpeechResult)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            modifier = Modifier
                .size(46.dp)
                .background(bgColor, CircleShape)
                .border(1.5.dp, if (isListening) DangerCoral else NourCyan.copy(alpha = 0.6f), CircleShape)
                .testTag("voice_input_button")
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "إيقاف التسجيل الصوتي" else "تسجيل صوتي",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
