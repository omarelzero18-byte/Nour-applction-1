package com.mohandesomar.nourlv1.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohandesomar.nourlv1.sound.SoundManager
import com.mohandesomar.nourlv1.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1-A: شاشة الإنترو (Intro/Splash) — مشهد سينمائي متصل من 4 مراحل:
 * 1. ولادة النور: شاشة سودة -> نقطة ضوء متوهجة تكبر بنعومة.
 * 2. الكرة المتحولة: بلوب عضوي نابض يتدرج من بنفسجي لأزرق لذهبي.
 * 3. الجسيمات المتجمعة: تناثر الجسيمات وتجمعها مغناطيسياً لتكوين لوجو Nour-lv1.
 * 4. استيقاظ نور: الضوء يتحول لأفاتار نور النابض وتبدأ التحدث فوراً بدون قطع.
 */
@Composable
fun IntroScreen(
    onIntroFinished: () -> Unit
) {
    // Current stage: 1, 2, 3, 4
    var stage by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        SoundManager.playIntroBirthOfLight()
        delay(1200) // Phase 1: Birth of Light (0 - 1.2s)
        stage = 2
        SoundManager.playIntroMorphingSphere()
        delay(1400) // Phase 2: Morphing Orb (1.2 - 2.6s)
        stage = 3
        SoundManager.playIntroParticleCluster()
        delay(1300) // Phase 3: Particles Magnetize to Logo (2.6 - 3.9s)
        stage = 4
        SoundManager.playIntroAwakening()
        delay(1800) // Phase 4: Nour awakens and starts speaking (3.9 - 5.7s)
        onIntroFinished()
    }

    // Dynamic animations for each phase
    val infiniteTransition = rememberInfiniteTransition(label = "intro_infinite")

    // Pulsing & Glow animations
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    val colorCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "color_cycle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("intro_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient deep glow in background
        Box(
            modifier = Modifier
                .size(350.dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NourViolet.copy(alpha = if (stage >= 2) 0.35f else 0.1f),
                            NourCyan.copy(alpha = if (stage >= 2) 0.2f else 0.05f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        when (stage) {
            1 -> {
                // Phase 1: ولادة النور
                val lightBirthScale by animateFloatAsState(
                    targetValue = 1.2f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                    label = "light_birth"
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.scale(lightBirthScale)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .blur(12.dp)
                            .background(Color.White, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }

            2 -> {
                // Phase 2: الكرة المتحولة (Morphing Organic Orb)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(orbPulse)
                        .size(160.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NourGoldLight,
                                    NourCyan,
                                    NourViolet,
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(NourViolet, NourCyan, NourGold, NourViolet)
                            ),
                            shape = CircleShape
                        )
                ) {
                    // Inner luminous spark
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .blur(4.dp)
                    )
                }
            }

            3 -> {
                // Phase 3: الجسيمات المتجمعة وتكوين اللوجو
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    // Particle canvas drawing magnetizing particles
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val numParticles = 24
                        for (i in 0 until numParticles) {
                            val angle = (i * (360f / numParticles) + colorCycle) * (Math.PI / 180f).toFloat()
                            val radius = (45f + 25f * cos(angle * 2))
                            val x = center.x + radius * cos(angle)
                            val y = center.y + radius * sin(angle)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(
                                        if (i % 3 == 0) NourGold else if (i % 3 == 1) NourCyan else NourViolet,
                                        Color.Transparent
                                    ),
                                    center = Offset(x, y),
                                    radius = 16f
                                ),
                                radius = 6f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    // Logo Emblem Center
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Nour-lv1",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "نور • تنظيم حياة الطالب",
                            style = MaterialTheme.typography.labelMedium,
                            color = NourGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            4 -> {
                // Phase 4: استيقاظ نور وبداية التحدث مباشرة
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    // Nour's Living Pulsing Avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .scale(orbPulse)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(NourGoldLight, NourCyan, NourViolet, Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                            .border(2.5.dp, NourCyan, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "أفاتار نور",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "نور استيقظت! ✨",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NourGold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "أهلاً بيك يا بطل! حابة أعرفك بنفسي الأول...",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                }
            }
        }

        // Quick Skip affordance
        TextButton(
            onClick = onIntroFinished,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("skip_intro_button")
        ) {
            Text(
                text = "تخطي للتعارف",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondaryDark
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextSecondaryDark,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
