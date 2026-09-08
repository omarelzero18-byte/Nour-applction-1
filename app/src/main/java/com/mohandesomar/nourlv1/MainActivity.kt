package com.mohandesomar.nourlv1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mohandesomar.nourlv1.sound.SoundManager
import com.mohandesomar.nourlv1.speech.SpeechHelper
import com.mohandesomar.nourlv1.ui.components.VariableRewardDialog
import com.mohandesomar.nourlv1.ui.screens.*
import com.mohandesomar.nourlv1.ui.theme.*
import com.mohandesomar.nourlv1.viewmodel.NourViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NourViewModel by viewModels()
    private lateinit var speechHelper: SpeechHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechHelper = SpeechHelper(this)
        enableEdgeToEdge()

        setContent {
            val appSettings by viewModel.appSettings.collectAsState()
            val layoutDir = if (appSettings.language == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
            val appStrings = if (appSettings.language == "en") EnglishStrings else ArabicStrings

            MyApplicationTheme(
                darkTheme = appSettings.darkTheme,
                fontScale = appSettings.fontScale
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDir,
                    LocalAppStrings provides appStrings
                ) {
                    NourApp(
                        viewModel = viewModel,
                        speechHelper = speechHelper
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHelper.destroy()
    }
}

enum class NourNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("الرئيسية", Icons.Default.Home, Icons.Outlined.Home),
    TASKS("المهام", Icons.Default.Checklist, Icons.Outlined.Checklist),
    SUCCESS_PATH("مسار النجاح", Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp),
    SMART_TOOLS("الأدوات", Icons.Default.Psychology, Icons.Outlined.Psychology),
    HABITS("العادات", Icons.Default.LocalFireDepartment, Icons.Outlined.LocalFireDepartment),
    NOUR("نور 💜", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
    PROJECTS("المشاريع", Icons.Default.Code, Icons.Outlined.Code),
    SETTINGS("الإعدادات", Icons.Default.Settings, Icons.Outlined.Settings)
}

@Composable
fun NourApp(
    viewModel: NourViewModel,
    speechHelper: SpeechHelper
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val activeReward by viewModel.activeReward.collectAsState()

    // Screen navigation flow: "INTRO" -> "ONBOARDING" -> "MAIN"
    var currentFlow by remember { mutableStateOf("INTRO") }
    var currentTab by remember { mutableStateOf(NourNavDestination.HOME) }

    LaunchedEffect(userProfile) {
        if (userProfile != null && userProfile?.onboardingCompleted == true) {
            currentFlow = "MAIN"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (currentFlow) {
            "INTRO" -> {
                IntroScreen(
                    onIntroFinished = {
                        if (userProfile?.onboardingCompleted == true) {
                            currentFlow = "MAIN"
                        } else {
                            currentFlow = "ONBOARDING"
                        }
                    }
                )
            }

            "ONBOARDING" -> {
                OnboardingScreen(
                    onOnboardingFinished = { name, age, stage, grade, interests, hasTech, projects ->
                        viewModel.completeOnboarding(
                            name = name,
                            age = age,
                            stage = stage,
                            grade = grade,
                            secondaryInterests = interests,
                            hasTechField = hasTech,
                            initialProjects = projects
                        )
                        currentFlow = "MAIN"
                    }
                )
            }

            else -> {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NourBottomNavigationBar(
                            currentTab = currentTab,
                            onTabSelected = {
                                SoundManager.playButtonTap()
                                currentTab = it
                            }
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (currentTab) {
                            NourNavDestination.HOME -> HomeScreen(
                                viewModel = viewModel,
                                speechHelper = speechHelper,
                                onNavigateToTasks = { currentTab = NourNavDestination.TASKS },
                                onNavigateToHabits = { currentTab = NourNavDestination.HABITS },
                                onNavigateToChat = { currentTab = NourNavDestination.NOUR },
                                onNavigateToProjects = { currentTab = NourNavDestination.PROJECTS },
                                onNavigateToEmergency = { currentTab = NourNavDestination.TASKS },
                                onNavigateToSuccessPath = { currentTab = NourNavDestination.SUCCESS_PATH },
                                onNavigateToSmartTools = { currentTab = NourNavDestination.SMART_TOOLS }
                            )
                            NourNavDestination.TASKS -> TasksScreen(
                                viewModel = viewModel,
                                speechHelper = speechHelper
                            )
                            NourNavDestination.SUCCESS_PATH -> SuccessPathScreen(
                                viewModel = viewModel
                            )
                            NourNavDestination.SMART_TOOLS -> SmartToolsScreen(
                                viewModel = viewModel,
                                speechHelper = speechHelper
                            )
                            NourNavDestination.HABITS -> HabitsScreen(
                                viewModel = viewModel,
                                speechHelper = speechHelper
                            )
                            NourNavDestination.PROJECTS -> ProjectsScreen(
                                viewModel = viewModel
                            )
                            NourNavDestination.NOUR -> NourChatScreen(
                                viewModel = viewModel,
                                speechHelper = speechHelper
                            )
                            NourNavDestination.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onResetToOnboarding = {
                                    currentFlow = "INTRO"
                                }
                            )
                        }
                    }
                }
            }
        }

        // Dopamine Variable Reward Dialog
        activeReward?.let { reward ->
            VariableRewardDialog(
                reward = reward,
                onDismiss = { viewModel.dismissReward() }
            )
        }
    }
}

@Composable
fun NourBottomNavigationBar(
    currentTab: NourNavDestination,
    onTabSelected: (NourNavDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(NourViolet.copy(alpha = 0.35f), MaterialTheme.colorScheme.outline)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .testTag("bottom_nav_bar")
    ) {
        NourNavDestination.values().forEach { destination ->
            val isSelected = currentTab == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(destination) },
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.title,
                        tint = if (isSelected) NourCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) NourCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedIconColor = NourCyan,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
