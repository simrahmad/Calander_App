package com.example.calander_app


import android.app.Service
import android.os.IBinder
import android.os.VibratorManager
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import android.media.ToneGenerator
import android.media.AudioManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.LocalTextStyle
import android.Manifest
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Verified
import android.os.PowerManager
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.Canvas
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.compose.foundation.lazy.items
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import android.view.WindowManager
import android.os.VibrationEffect
import android.os.Vibrator

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.app.Notification
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.Year
import java.time.ZoneId
import java.time.Duration
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// Color Palette
val CyanBlue = Color(0xFF00D4FF)
val DarkNavy = Color(0xFF0A1929)
val PinkAccent = Color(0xFFFF4081)
val PurpleAccent = Color(0xFFAB47BC)
val LightCyan = Color(0xFF66E3FF)
val DarkCyan = Color(0xFF0099CC)
val NeonGreen = Color(0xFF39FF14)
val OrangeAccent = Color(0xFFFFA726)

// Data Classes
data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val dateTime: LocalDateTime,
    val category: EventCategory,
    val duration: Int = 60,
    var isAttended: Boolean = false
)
data class TimeData(
    val hours: String,
    val minutes: String,
    val seconds: String,
    val milliseconds: String,
    val date: String,
    val location: String,
    val amPm: String
)
object EventNotificationTracker {
    private val notifiedEvents = mutableSetOf<String>()

    fun shouldNotify(eventId: String, minutesBefore: Int): Boolean {
        val key = "${eventId}_${minutesBefore}"
        return if (!notifiedEvents.contains(key)) {
            notifiedEvents.add(key)
            true
        } else {
            false
        }
    }

    fun clearOldNotifications() {
        // Clear notifications for events that have passed
        notifiedEvents.clear()
    }
}
enum class EventCategory(val color: Color, val icon: ImageVector) {
    WORK(Color(0xFF3B82F6), Icons.Default.Work),
    PERSONAL(PinkAccent, Icons.Default.Person),
    MEETING(CyanBlue, Icons.Default.VideoCall),
    HEALTH(Color(0xFF10B981), Icons.Default.FavoriteBorder),
    ALARM(PurpleAccent, Icons.Default.Alarm)
}

data class AIFeature(val title: String, val description: String, val icon: ImageVector, val color: Color)
data class AlarmTone(val name: String, val voiceType: String, val uri: Uri? = null)
// Daily Challenge System
data class DailyChallenge(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val type: ChallengeType,
    val targetValue: Int = 1,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val startDate: LocalDate = LocalDate.now(),
    val streakCount: Int = 0,
    val color: Color = CyanBlue
)

enum class ChallengeType {
    NO_SNOOZE_ALARM,      // Wake up without snooze
    PERFECT_ATTENDANCE,    // Attend all events
    EARLY_BIRD,           // Wake up with first alarm
    DAILY_PLANNER,        // Open app before 9 AM
    ON_TIME_CHAMPION      // Arrive on time to events
}

data class ChallengeProgress(
    val challengeId: String,
    val date: LocalDate,
    val completed: Boolean,
    val value: Int = 0
)
// Achievement & Rewards System
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val pointsAwarded: Int,
    val unlockRequirement: String,
    val isUnlocked: Boolean = false,
    val category: AchievementCategory,
    val badgeColor: Color
)

enum class AchievementCategory {
    STREAK,      // Consecutive day achievements
    CHALLENGE,   // Challenge completion
    MASTER,      // Elite achievements
    LEGENDARY    // Rare achievements
}

data class AppTheme(
    val id: String,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val primaryGradient: List<Color>,
    val accentColor: Color,
    val secondaryColor: Color,
    val cardColor: Color,
    val isUnlocked: Boolean = false,
    val previewIcon: ImageVector = Icons.Default.Palette
)

data class UserProfile(
    val totalPoints: Int = 0,
    val level: Int = 1,
    val unlockedBadges: List<String> = emptyList(),
    val unlockedThemes: List<String> = listOf("default"), // Default theme always unlocked
    val currentTheme: String = "default",
    val longestStreak: Int = 0,
    val totalChallengesCompleted: Int = 0
)
// Available Themes
fun getAvailableThemes() = listOf(
    AppTheme(
        id = "default",
        name = "Ocean AI (Default)",
        description = "The original ChronoAI experience",
        pointsRequired = 0,
        primaryGradient = listOf(DarkNavy, Color(0xFF0D2137), Color(0xFF1A2332)),
        accentColor = CyanBlue,
        secondaryColor = LightCyan,
        cardColor = Color(0xFF1A2332),
        isUnlocked = true
    ),
    AppTheme(
        id = "ocean_breeze",
        name = "Ocean Breeze",
        description = "Deep blues and turquoise calm",
        pointsRequired = 100,
        primaryGradient = listOf(Color(0xFF006994), Color(0xFF1E3A8A), Color(0xFF0EA5E9)),
        accentColor = Color(0xFF00CED1),
        secondaryColor = Color(0xFF87CEEB),
        cardColor = Color(0xFF1A3A52),
        previewIcon = Icons.Default.Waves
    ),
    AppTheme(
        id = "sunset_glow",
        name = "Sunset Glow",
        description = "Warm oranges and pinks",
        pointsRequired = 200,
        primaryGradient = listOf(Color(0xFFFF6B6B), Color(0xFFFFA500), Color(0xFFFF1493)),
        accentColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFFFA726),
        cardColor = Color(0xFF3D2817),
        previewIcon = Icons.Default.WbSunny
    ),
    AppTheme(
        id = "forest_night",
        name = "Forest Night",
        description = "Dark greens and emerald",
        pointsRequired = 300,
        primaryGradient = listOf(Color(0xFF0B3D2C), Color(0xFF1E5128), Color(0xFF2D6A4F)),
        accentColor = Color(0xFF39FF14),
        secondaryColor = Color(0xFF52B788),
        cardColor = Color(0xFF1B4332),
        previewIcon = Icons.Default.Park
    ),
    AppTheme(
        id = "midnight_oil",
        name = "Midnight Oil",
        description = "Professional charcoal and blue",
        pointsRequired = 350,
        primaryGradient = listOf(Color(0xFF0D0D0D), Color(0xFF1A1A2E), Color(0xFF16213E)),
        accentColor = Color(0xFF00D4FF),
        secondaryColor = Color(0xFF4A90E2),
        cardColor = Color(0xFF2A2A3E),
        previewIcon = Icons.Default.DarkMode
    ),
    AppTheme(
        id = "cherry_blossom",
        name = "Cherry Blossom",
        description = "Soft pinks and lavender",
        pointsRequired = 400,
        primaryGradient = listOf(Color(0xFFFFC1CC), Color(0xFFFFB6C1), Color(0xFFE6E6FA)),
        accentColor = Color(0xFFFF69B4),
        secondaryColor = Color(0xFFDDA0DD),
        cardColor = Color(0xFFFFF0F5),
        previewIcon = Icons.Default.LocalFlorist
    ),
    AppTheme(
        id = "tropical_paradise",
        name = "Tropical Paradise",
        description = "Bright turquoise and coral",
        pointsRequired = 450,
        primaryGradient = listOf(Color(0xFF00CED1), Color(0xFFFFD700), Color(0xFFFF7F50)),
        accentColor = Color(0xFFFF1493),
        secondaryColor = Color(0xFF00BFFF),
        cardColor = Color(0xFF2A5555),
        previewIcon = Icons.Default.BeachAccess
    ),
    AppTheme(
        id = "galaxy_explorer",
        name = "Galaxy Explorer",
        description = "Cosmic purple and starlight",
        pointsRequired = 500,
        primaryGradient = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E)),
        accentColor = Color(0xFF9B59B6),
        secondaryColor = Color(0xFF8E44AD),
        cardColor = Color(0xFF1E1E3F),
        previewIcon = Icons.Default.Rocket
    ),
    AppTheme(
        id = "arctic_ice",
        name = "Arctic Ice",
        description = "Cool ice blue and silver",
        pointsRequired = 600,
        primaryGradient = listOf(Color(0xFFB0E0E6), Color(0xFFE0F7FF), Color(0xFF87CEEB)),
        accentColor = Color(0xFF00CED1),
        secondaryColor = Color(0xFF4682B4),
        cardColor = Color(0xFFD0E8F2),
        previewIcon = Icons.Default.AcUnit
    ),
    AppTheme(
        id = "crimson_fire",
        name = "Crimson Fire",
        description = "Bold red and orange flames",
        pointsRequired = 700,
        primaryGradient = listOf(Color(0xFF8B0000), Color(0xFFDC143C), Color(0xFFFF4500)),
        accentColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFFF6347),
        cardColor = Color(0xFF4A0E0E),
        previewIcon = Icons.Default.Whatshot
    ),
    AppTheme(
        id = "royal_gold",
        name = "Royal Gold",
        description = "Luxurious gold and purple",
        pointsRequired = 1000,
        primaryGradient = listOf(Color(0xFFFFD700), Color(0xFFDAA520), Color(0xFFB8860B)),
        accentColor = Color(0xFFFFFFFF),
        secondaryColor = Color(0xFF9370DB),
        cardColor = Color(0xFF4A3C2A),
        previewIcon = Icons.Default.EmojiEvents
    )
)

// Available Achievements
fun getAvailableAchievements() = listOf(
    Achievement(
        id = "week_warrior",
        name = "Week Warrior",
        description = "Complete 7-day streak",
        icon = Icons.Default.Whatshot,
        pointsAwarded = 100,
        unlockRequirement = "7_day_streak",
        category = AchievementCategory.STREAK,
        badgeColor = OrangeAccent
    ),
    Achievement(
        id = "challenge_master",
        name = "Challenge Master",
        description = "Complete all 5 challenges in one day",
        icon = Icons.Default.EmojiEvents,
        pointsAwarded = 150,
        unlockRequirement = "5_challenges_one_day",
        category = AchievementCategory.CHALLENGE,
        badgeColor = NeonGreen
    ),
    Achievement(
        id = "early_bird_15",
        name = "Dawn Keeper",
        description = "Complete Early Bird 15 times",
        icon = Icons.Default.WbSunny,
        pointsAwarded = 200,
        unlockRequirement = "early_bird_15",
        category = AchievementCategory.CHALLENGE,
        badgeColor = OrangeAccent
    ),
    Achievement(
        id = "no_snooze_30",
        name = "Snooze Slayer",
        description = "No snooze for 30 alarms",
        icon = Icons.Default.Alarm,
        pointsAwarded = 300,
        unlockRequirement = "no_snooze_30",
        category = AchievementCategory.MASTER,
        badgeColor = PurpleAccent
    ),
    Achievement(
        id = "attendance_10",
        name = "Attendance King",
        description = "Perfect attendance 10 days",
        icon = Icons.Default.CheckCircle,
        pointsAwarded = 200,
        unlockRequirement = "perfect_attendance_10",
        category = AchievementCategory.CHALLENGE,
        badgeColor = NeonGreen
    ),
    Achievement(
        id = "month_champion",
        name = "Month Champion",
        description = "30-day streak",
        icon = Icons.Default.Star,
        pointsAwarded = 500,
        unlockRequirement = "30_day_streak",
        category = AchievementCategory.MASTER,
        badgeColor = Color(0xFFFFD700)
    ),
    Achievement(
        id = "legend_90",
        name = "Legend Status",
        description = "90-day streak",
        icon = Icons.Default.MilitaryTech,
        pointsAwarded = 1000,
        unlockRequirement = "90_day_streak",
        category = AchievementCategory.LEGENDARY,
        badgeColor = Color(0xFFFFD700)
    ),
    Achievement(
        id = "centurion",
        name = "Centurion",
        description = "Complete 100 challenges total",
        icon = Icons.Default.Verified,
        pointsAwarded = 800,
        unlockRequirement = "100_challenges",
        category = AchievementCategory.LEGENDARY,
        badgeColor = CyanBlue
    )
)

// Persistent Storage
object EventStorage {
    private const val PREFS_NAME = "chrono_ai_prefs"
    private const val EVENTS_KEY = "saved_events"

    fun saveEvents(context: Context, events: List<CalendarEvent>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val eventStrings = events.map { event ->
                buildString {
                    append(event.id)
                    append("|||") // Use unique separator
                    append(event.title)
                    append("|||")
                    append(event.description)
                    append("|||")
                    append(event.dateTime.toString())
                    append("|||")
                    append(event.category.name)
                    append("|||")
                    append(event.duration)
                    append("|||")
                    append(event.isAttended)
                }
            }

            prefs.edit()
                .putStringSet(EVENTS_KEY, eventStrings.toSet())
                .apply()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadEvents(context: Context): List<CalendarEvent> {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val eventStrings = prefs.getStringSet(EVENTS_KEY, emptySet()) ?: emptySet()

            return eventStrings.mapNotNull { str ->
                try {
                    val parts = str.split("|||")
                    if (parts.size >= 7) {
                        CalendarEvent(
                            id = parts[0],
                            title = parts[1],
                            description = parts[2],
                            dateTime = LocalDateTime.parse(parts[3]),
                            category = EventCategory.valueOf(parts[4]),
                            duration = parts[5].toInt(),
                            isAttended = parts[6].toBoolean()
                        )
                    } else null
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }.sortedBy { it.dateTime }

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun saveAttendance(context: Context, eventId: String, attended: Boolean) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("attendance_$eventId", attended)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearOldEvents(context: Context) {
        try {
            // Remove events older than 30 days
            val cutoffDate = LocalDateTime.now().minusDays(30)
            val events = loadEvents(context)
            val filteredEvents = events.filter { it.dateTime.isAfter(cutoffDate) }
            saveEvents(context, filteredEvents)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
object ChallengeStorage {
    private const val PREFS_NAME = "chrono_ai_prefs"
    private const val CHALLENGES_KEY = "daily_challenges"
    private const val PROGRESS_KEY = "challenge_progress"
    private const val LAST_CHECK_KEY = "last_challenge_check"

    fun initializeChallenges(context: Context): List<DailyChallenge> {
        val saved = loadChallenges(context)
        if (saved.isNotEmpty()) return saved

        val defaultChallenges = listOf(
            DailyChallenge(
                id = "no_snooze",
                name = "No Snooze Challenge",
                description = "Wake up with first alarm - no snooze for 7 days",
                icon = Icons.Default.Alarm,
                type = ChallengeType.NO_SNOOZE_ALARM,
                targetValue = 7,
                color = PurpleAccent
            ),
            DailyChallenge(
                id = "perfect_attendance",
                name = "Perfect Attendance",
                description = "Attend all scheduled events today",
                icon = Icons.Default.CheckCircle,
                type = ChallengeType.PERFECT_ATTENDANCE,
                targetValue = 1,
                color = NeonGreen
            ),
            DailyChallenge(
                id = "early_bird",
                name = "Early Bird",
                description = "Dismiss alarm within 1 minute for 7 days",
                icon = Icons.Default.WbSunny,
                type = ChallengeType.EARLY_BIRD,
                targetValue = 7,
                color = OrangeAccent
            ),
            DailyChallenge(
                id = "daily_planner",
                name = "Daily Planner",
                description = "Check your schedule before 9 AM",
                icon = Icons.Default.CalendarToday,
                type = ChallengeType.DAILY_PLANNER,
                targetValue = 1,
                color = CyanBlue
            ),
            DailyChallenge(
                id = "on_time",
                name = "On-Time Champion",
                description = "Mark all events as attended today",
                icon = Icons.Default.Schedule,
                type = ChallengeType.ON_TIME_CHAMPION,
                targetValue = 1,
                color = PinkAccent
            )
        )
        saveChallenges(context, defaultChallenges)
        return defaultChallenges
    }

    fun saveChallenges(context: Context, challenges: List<DailyChallenge>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val challengeStrings = challenges.map { c ->
                "${c.id}|||${c.name}|||${c.description}|||${c.type.name}|||${c.targetValue}|||${c.currentProgress}|||${c.isCompleted}|||${c.startDate}|||${c.streakCount}"
            }
            prefs.edit().putStringSet(CHALLENGES_KEY, challengeStrings.toSet()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadChallenges(context: Context): List<DailyChallenge> {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val challengeStrings = prefs.getStringSet(CHALLENGES_KEY, emptySet()) ?: emptySet()

            return challengeStrings.mapNotNull { str ->
                try {
                    val parts = str.split("|||")
                    if (parts.size >= 9) {
                        val id = parts[0]
                        val icon = when(id) {
                            "no_snooze" -> Icons.Default.Alarm
                            "perfect_attendance" -> Icons.Default.CheckCircle
                            "early_bird" -> Icons.Default.WbSunny
                            "daily_planner" -> Icons.Default.CalendarToday
                            "on_time" -> Icons.Default.Schedule
                            else -> Icons.Default.Star
                        }
                        val color = when(id) {
                            "no_snooze" -> PurpleAccent
                            "perfect_attendance" -> NeonGreen
                            "early_bird" -> OrangeAccent
                            "daily_planner" -> CyanBlue
                            "on_time" -> PinkAccent
                            else -> CyanBlue
                        }
                        DailyChallenge(
                            id = id,
                            name = parts[1],
                            description = parts[2],
                            icon = icon,
                            type = ChallengeType.valueOf(parts[3]),
                            targetValue = parts[4].toInt(),
                            currentProgress = parts[5].toInt(),
                            isCompleted = parts[6].toBoolean(),
                            startDate = LocalDate.parse(parts[7]),
                            streakCount = parts[8].toInt(),
                            color = color
                        )
                    } else null
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun updateChallengeProgress(context: Context, challengeId: String, increment: Int = 1) {
        val challenges = loadChallenges(context).toMutableList()
        val index = challenges.indexOfFirst { it.id == challengeId }

        if (index != -1) {
            val challenge = challenges[index]
            val newProgress = challenge.currentProgress + increment
            val completed = newProgress >= challenge.targetValue

            challenges[index] = challenge.copy(
                currentProgress = newProgress,
                isCompleted = completed,
                streakCount = if (completed) challenge.streakCount + 1 else challenge.streakCount
            )

            saveChallenges(context, challenges)

            if (completed && !challenge.isCompleted) {
                // Award points for challenge completion (NEW!)
                val pointsAwarded = when (challenge.type) {
                    ChallengeType.NO_SNOOZE_ALARM -> 50
                    ChallengeType.PERFECT_ATTENDANCE -> 30
                    ChallengeType.EARLY_BIRD -> 40
                    ChallengeType.DAILY_PLANNER -> 20
                    ChallengeType.ON_TIME_CHAMPION -> 30
                }
                UserProfileStorage.addPoints(context, pointsAwarded)
                UserProfileStorage.incrementChallengesCompleted(context)

                // Check for streak achievements
                checkAchievements(context, challenge.streakCount)

                // Check for theme unlocks
                checkThemeUnlocks(context)

                showChallengeCompletedNotification(context, challenge.name, pointsAwarded)
            }
        }
    }

    private fun checkAchievements(context: Context, currentStreak: Int) {
        val profile = UserProfileStorage.loadProfile(context)

        // Check streak achievements
        when (currentStreak) {
            7 -> UserProfileStorage.unlockAchievement(context, "week_warrior")
            30 -> UserProfileStorage.unlockAchievement(context, "month_champion")
            90 -> UserProfileStorage.unlockAchievement(context, "legend_90")
        }

        // Check total challenges achievement
        if (profile.totalChallengesCompleted >= 100) {
            UserProfileStorage.unlockAchievement(context, "centurion")
        }

        // Update longest streak
        UserProfileStorage.updateStreak(context, currentStreak)
    }

    private fun checkThemeUnlocks(context: Context) {
        val profile = UserProfileStorage.loadProfile(context)
        val themes = getAvailableThemes()

        themes.forEach { theme ->
            if (!profile.unlockedThemes.contains(theme.id) &&
                profile.totalPoints >= theme.pointsRequired) {
                UserProfileStorage.unlockTheme(context, theme.id)
                showThemeUnlockedNotification(context, theme.name)
            }
        }
    }

    private fun showChallengeCompletedNotification(context: Context, challengeName: String, points: Int) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                NotificationCompat.Builder(context, "chrono_ai_events")
                    .setSmallIcon(android.R.drawable.star_on)
                    .setContentTitle("🎉 Challenge Completed!")
                    .setContentText("$challengeName - Earned $points points!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            )
        }
    }

    private fun showThemeUnlockedNotification(context: Context, themeName: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                NotificationCompat.Builder(context, "chrono_ai_events")
                    .setSmallIcon(android.R.drawable.ic_menu_gallery)
                    .setContentTitle("🎨 Theme Unlocked!")
                    .setContentText(themeName)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            )
        }
    }

    fun resetDailyChallenges(context: Context) {
        val challenges = loadChallenges(context).map { challenge ->
            when (challenge.type) {
                ChallengeType.PERFECT_ATTENDANCE,
                ChallengeType.DAILY_PLANNER,
                ChallengeType.ON_TIME_CHAMPION -> {
                    // Daily challenges reset every day
                    challenge.copy(
                        currentProgress = 0,
                        isCompleted = false,
                        startDate = LocalDate.now()
                    )
                }
                else -> challenge // Streak challenges don't reset
            }
        }
        saveChallenges(context, challenges)
    }

    fun checkAndResetIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getString(LAST_CHECK_KEY, LocalDate.now().toString())
        val lastCheckDate = LocalDate.parse(lastCheck ?: LocalDate.now().toString())

        if (lastCheckDate.isBefore(LocalDate.now())) {
            resetDailyChallenges(context)
            prefs.edit().putString(LAST_CHECK_KEY, LocalDate.now().toString()).apply()
        }
    }

    private fun showChallengeCompletedNotification(context: Context, challengeName: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                NotificationCompat.Builder(context, "chrono_ai_events")
                    .setSmallIcon(android.R.drawable.star_on)
                    .setContentTitle("🎉 Challenge Completed!")
                    .setContentText(challengeName)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            )
        }
    }
}
object UserProfileStorage {
    private const val PREFS_NAME = "chrono_ai_prefs"
    private const val PROFILE_KEY = "user_profile"
    private const val ACHIEVEMENTS_KEY = "achievements"
    private const val THEMES_KEY = "themes"

    fun loadProfile(context: Context): UserProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val profileString = prefs.getString(PROFILE_KEY, null) ?: return UserProfile()

        return try {
            val parts = profileString.split("|||")
            UserProfile(
                totalPoints = parts[0].toInt(),
                level = parts[1].toInt(),
                unlockedBadges = parts[2].split(",").filter { it.isNotEmpty() },
                unlockedThemes = parts[3].split(",").filter { it.isNotEmpty() }.let {
                    if (it.isEmpty()) listOf("default") else it
                },
                currentTheme = parts.getOrNull(4) ?: "default",
                longestStreak = parts.getOrNull(5)?.toIntOrNull() ?: 0,
                totalChallengesCompleted = parts.getOrNull(6)?.toIntOrNull() ?: 0
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    fun saveProfile(context: Context, profile: UserProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val profileString = "${profile.totalPoints}|||${profile.level}|||" +
                "${profile.unlockedBadges.joinToString(",")}|||" +
                "${profile.unlockedThemes.joinToString(",")}|||" +
                "${profile.currentTheme}|||${profile.longestStreak}|||" +
                "${profile.totalChallengesCompleted}"
        prefs.edit().putString(PROFILE_KEY, profileString).apply()
    }

    fun addPoints(context: Context, points: Int): UserProfile {
        val profile = loadProfile(context)
        val newPoints = profile.totalPoints + points
        val newLevel = calculateLevel(newPoints)
        val updated = profile.copy(
            totalPoints = newPoints,
            level = newLevel
        )
        saveProfile(context, updated)
        return updated
    }

    fun unlockAchievement(context: Context, achievementId: String): UserProfile {
        val profile = loadProfile(context)
        if (!profile.unlockedBadges.contains(achievementId)) {
            val achievement = getAvailableAchievements().find { it.id == achievementId }
            val updated = profile.copy(
                unlockedBadges = profile.unlockedBadges + achievementId,
                totalPoints = profile.totalPoints + (achievement?.pointsAwarded ?: 0)
            )
            saveProfile(context, updated)

            // Show achievement notification
            showAchievementNotification(context, achievement?.name ?: "Achievement")
            return updated
        }
        return profile
    }

    fun unlockTheme(context: Context, themeId: String): UserProfile {
        val profile = loadProfile(context)
        if (!profile.unlockedThemes.contains(themeId)) {
            val updated = profile.copy(unlockedThemes = profile.unlockedThemes + themeId)
            saveProfile(context, updated)
            return updated
        }
        return profile
    }

    fun setCurrentTheme(context: Context, themeId: String): UserProfile {
        val profile = loadProfile(context)
        val updated = profile.copy(currentTheme = themeId)
        saveProfile(context, updated)
        return updated
    }

    fun updateStreak(context: Context, currentStreak: Int): UserProfile {
        val profile = loadProfile(context)
        if (currentStreak > profile.longestStreak) {
            val updated = profile.copy(longestStreak = currentStreak)
            saveProfile(context, updated)
            return updated
        }
        return profile
    }

    fun incrementChallengesCompleted(context: Context): UserProfile {
        val profile = loadProfile(context)
        val updated = profile.copy(totalChallengesCompleted = profile.totalChallengesCompleted + 1)
        saveProfile(context, updated)
        return updated
    }

    private fun calculateLevel(points: Int): Int = when {
        points < 500 -> 1      // Bronze
        points < 1500 -> 2     // Silver
        points < 5000 -> 3     // Gold
        else -> 4              // Diamond
    }

    private fun showAchievementNotification(context: Context, achievementName: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                NotificationCompat.Builder(context, "chrono_ai_events")
                    .setSmallIcon(android.R.drawable.star_big_on)
                    .setContentTitle("🏆 Achievement Unlocked!")
                    .setContentText(achievementName)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            )
        }
    }
}

fun getAlarmTones(context: Context) = listOf(
    AlarmTone("Bird Song", "Natural wake-up", Uri.parse("android.resource://${context.packageName}/${R.raw.birdsong}")),
    AlarmTone("Christmas", "Festive bells", Uri.parse("android.resource://${context.packageName}/${R.raw.christmas}")),
    AlarmTone("Clock", "Classic ticking", Uri.parse("android.resource://${context.packageName}/${R.raw.clock}")),
    AlarmTone("Fairy Tail", "Magical morning", Uri.parse("android.resource://${context.packageName}/${R.raw.fairytail}")),
    AlarmTone("iPhone", "Apple style", Uri.parse("android.resource://${context.packageName}/${R.raw.iphone}")),
    AlarmTone("Samsung", "Galaxy style", Uri.parse("android.resource://${context.packageName}/${R.raw.samsung}")),
    AlarmTone("Morning Flower", "Gentle bloom", Uri.parse("android.resource://${context.packageName}/${R.raw.morningflower}")),
    AlarmTone("Morning", "Bright start", Uri.parse("android.resource://${context.packageName}/${R.raw.morning}")),
    AlarmTone("Sparkle", "Light chime", Uri.parse("android.resource://${context.packageName}/${R.raw.sparkle}")),
    AlarmTone("Tick", "Simple tick", Uri.parse("android.resource://${context.packageName}/${R.raw.tick}")),
    AlarmTone("Default", "Standard alarm", Uri.parse("android.resource://${context.packageName}/${R.raw.default_alarm}")),
    AlarmTone("Rainfall", "Calm rain", Uri.parse("android.resource://${context.packageName}/${R.raw.rainfall}"))
)
fun loadSampleEvents(): List<CalendarEvent> {
    val now = LocalDateTime.now()
    return listOf(
        CalendarEvent(
            title = "Team Meeting",
            description = "Weekly sync",
            dateTime = now.plusHours(2),
            category = EventCategory.MEETING,
            duration = 60
        ),
        CalendarEvent(
            title = "Gym Workout",
            description = "Cardio training",
            dateTime = now.plusDays(1).withHour(18).withMinute(0),
            category = EventCategory.HEALTH,
            duration = 90
        ),
        CalendarEvent(
            title = "Project Review",
            description = "Q1 presentation",
            dateTime = now.plusDays(2).withHour(14).withMinute(30),
            category = EventCategory.WORK,
            duration = 120
        )
    )
}

fun scheduleEventNotifications(event: CalendarEvent, context: Context) {
    scheduleNotification(context, event, 10, "Starting in 10 minutes")
    scheduleNotification(context, event, 5, "Starting in 5 minutes - Voice alert enabled")
}

fun scheduleNotification(
    context: Context,
    event: CalendarEvent,
    minutesBefore: Int,
    message: String
) {
    val notificationTime = event.dateTime.minusMinutes(minutesBefore.toLong())
    if (notificationTime.isAfter(LocalDateTime.now())) {
        val triggerTime =
            notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("event_title", event.title); putExtra(
            "event_description",
            event.description
        ); putExtra("message", message); putExtra(
            "minutes_before",
            minutesBefore
        ); putExtra("event_id", event.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode() + minutesBefore,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        else alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }
}

fun scheduleAlarm(time: LocalTime, voiceName: String, context: Context) {
    try {
        val now = LocalDateTime.now()
        var alarmDateTime = LocalDateTime.of(LocalDate.now(), time)

        if (alarmDateTime.isBefore(now) || alarmDateTime.isEqual(now)) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }

        val triggerTime = alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val soundUri = getAlarmTones(context).find { it.name == voiceName }?.uri?.toString()
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()

        Log.d("ALARM_DEBUG", "==========================================")
        Log.d("ALARM_DEBUG", "Setting alarm for: $alarmDateTime")
        Log.d("ALARM_DEBUG", "Trigger time: $triggerTime")
        Log.d("ALARM_DEBUG", "Current time: ${System.currentTimeMillis()}")
        Log.d("ALARM_DEBUG", "Time until alarm: ${(triggerTime - System.currentTimeMillis()) / 1000} seconds")
        Log.d("ALARM_DEBUG", "Sound: $voiceName")
        Log.d("ALARM_DEBUG", "Sound URI: $soundUri")
        Log.d("ALARM_DEBUG", "==========================================")

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.calander_app.ALARM_ACTION"
            putExtra("voice_name", voiceName)
            putExtra("sound_uri", soundUri)
            putExtra("trigger_time", triggerTime)
        }

        val requestCode = (time.hour * 100 + time.minute)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // CRITICAL: Check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("ALARM_DEBUG", "CANNOT SCHEDULE EXACT ALARMS - PERMISSION DENIED!")
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(it)
                }
                Toast.makeText(context, "Please allow exact alarms for reliable wake-up", Toast.LENGTH_LONG).show()
                return
            } else {
                Log.d("ALARM_DEBUG", "Exact alarm permission: GRANTED")
            }
        }

        // Cancel any existing alarm with same request code
        alarmManager.cancel(pendingIntent)
        Log.d("ALARM_DEBUG", "Cancelled any existing alarm with request code: $requestCode")

        // Use setAlarmClock for guaranteed delivery (shows alarm icon in status bar)
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
            pendingIntent
        )

        Log.d("ALARM_DEBUG", "✅ Alarm scheduled successfully using setAlarmClock")
        Log.d("ALARM_DEBUG", "Request code: $requestCode")

        // Verify battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                Log.w("ALARM_DEBUG", "⚠️ App is subject to battery optimization - alarms may not work reliably")
                Toast.makeText(
                    context,
                    "Please disable battery optimization for reliable alarms",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        val hour12 = if (time.hour == 0) 12 else if (time.hour > 12) time.hour - 12 else time.hour
        val amPm = if (time.hour < 12) "AM" else "PM"
        Toast.makeText(
            context,
            "✅ Alarm set for ${String.format("%02d:%02d %s", hour12, time.minute, amPm)}",
            Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {
        Log.e("ALARM_DEBUG", "❌ ALARM FAILED: ${e.message}", e)
        e.printStackTrace()
        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun checkUpcomingEvents(
    events: List<CalendarEvent>,
    context: Context,
    textToSpeech: TextToSpeech
) {
    val now = LocalDateTime.now()

    events.forEach { event ->
        val minsUntil = Duration.between(now, event.dateTime).toMinutes()

        // Only notify once at exactly 5 minutes
        if (minsUntil == 5L && EventNotificationTracker.shouldNotify(event.id, 5)) {
            // Speak announcement
            if (textToSpeech.isSpeaking.not()) {
                textToSpeech.speak(
                    "Attention: ${event.title} is about to start in 5 minutes. Please prepare.",
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "event_${event.id}_5"
                )
            }

            // Show notification with sound
            showNotificationWithSound(
                context,
                event.title,
                "Starting in 5 minutes - Get ready!"
            )
        }

        // 10-minute warning (notification only, no voice)
        if (minsUntil == 10L && EventNotificationTracker.shouldNotify(event.id, 10)) {
            showNotificationWithSound(context, event.title, "Starting in 10 minutes")
        }
    }

    // Clean up old notifications every hour
    if (now.minute == 0) {
        EventNotificationTracker.clearOldNotifications()
    }
}
fun getCurrentTimeWithLocation(context: Context): TimeData {
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val amPm = if (hour >= 12) "PM" else "AM"
    val hour12 = when {
        hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour
    }
    return TimeData(
        hour12.toString().padStart(2, '0'),
        cal.get(Calendar.MINUTE).toString().padStart(2, '0'),
        cal.get(Calendar.SECOND).toString().padStart(2, '0'),
        cal.get(Calendar.MILLISECOND).toString().take(2).padStart(2, '0'),
        SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(cal.time),
        "Your Location",
        amPm
    )
}

fun showNotificationWithSound(context: Context, title: String, message: String) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(context).notify(
            System.currentTimeMillis().toInt(),
            NotificationCompat.Builder(context, "chrono_ai_events")
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar).setContentTitle(title).setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true).setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setLights(android.graphics.Color.CYAN, 1000, 500)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build()
        )
    }
}
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("event_title") ?: ""
        val description = intent.getStringExtra("event_description") ?: ""
        val message = intent.getStringExtra("message") ?: ""
        val minsBefore = intent.getIntExtra("minutes_before", 0)

        // Show notification
        showNotification(context, title, description, message)

        // Play notification sound
        try {
            val notificationSound =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(context, notificationSound)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Voice announcement for 5-minute warning
        if (minsBefore == 5) {
            val tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val speaker = TextToSpeech(context, null)
                    speaker.language = Locale.US
                    speaker.setSpeechRate(0.9f)
                    speaker.setPitch(1.0f)

                    val voiceMessage =
                        "Attention! Your meeting, $title, is starting in 5 minutes. Please get ready."
                    speaker.speak(
                        voiceMessage,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "event_tts_${System.currentTimeMillis()}"
                    )

                    // Clean up after 10 seconds
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        speaker.stop()
                        speaker.shutdown()
                    }, 10000)
                }
            }
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        description: String,
        message: String
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(context, "chrono_ai_events")
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle("📅 $title")
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$message\n\n$description")
                        .setBigContentTitle(title)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setLights(android.graphics.Color.CYAN, 1000, 300)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build()

            NotificationManagerCompat.from(context).notify(
                title.hashCode(),
                notification
            )
        }
    }
}
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_DEBUG", "==========================================")
        Log.d("ALARM_DEBUG", "ALARM RECEIVER TRIGGERED!!!")
        Log.d("ALARM_DEBUG", "==========================================")

        val voiceName = intent.getStringExtra("voice_name") ?: "Alarm"
        val soundUriString = intent.getStringExtra("sound_uri")
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()

        // Acquire wake lock
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "ChronoAI::AlarmWakeLock"
        )
        wakeLock.acquire(60 * 1000L)

        // Start foreground service
        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            putExtra("sound_uri", soundUriString)
            putExtra("alarm_name", voiceName)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d("ALARM_DEBUG", "Foreground service started successfully")
        } catch (e: Exception) {
            Log.e("ALARM_DEBUG", "Failed to start service: ${e.message}")
        }

        // Also try to start the activity
        val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("sound_uri", soundUriString)
            putExtra("alarm_name", voiceName)
            putExtra("from_receiver", true)
        }

        try {
            context.startActivity(activityIntent)
            Log.d("ALARM_DEBUG", "AlarmRingingActivity started from receiver")
        } catch (e: Exception) {
            Log.e("ALARM_DEBUG", "Failed to start activity: ${e.message}")
        }

        // Release wake lock after delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (wakeLock.isHeld) wakeLock.release()
        }, 5000)
    }
}
class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ALARM_DEBUG", "Stopping alarm via AlarmStopReceiver")

        // Stop the foreground service (this stops sound + notification)
        val serviceIntent = Intent(context, AlarmForegroundService::class.java)
        context.stopService(serviceIntent)

        // Cancel all notifications
        NotificationManagerCompat.from(context).cancel(8888)
        NotificationManagerCompat.from(context).cancel(9999)

        Toast.makeText(context, "Alarm stopped", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AnimatedBackgroundParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(20) { index ->
            val randomX = remember { kotlin.random.Random.nextFloat() }
            val randomY = remember { kotlin.random.Random.nextFloat() }
            val randomX2 = remember { kotlin.random.Random.nextFloat() }
            val randomY2 = remember { kotlin.random.Random.nextFloat() }

            val offsetX by infiniteTransition.animateFloat(
                initialValue = randomX * 1000f,
                targetValue = randomX2 * 1000f,
                animationSpec = infiniteRepeatable(
                    tween(
                        5000 + index * 500,
                        easing = LinearEasing
                    ), RepeatMode.Reverse
                ),
                label = "x$index"
            )
            val offsetY by infiniteTransition.animateFloat(
                initialValue = randomY * 2000f,
                targetValue = randomY2 * 2000f,
                animationSpec = infiniteRepeatable(
                    tween(
                        6000 + index * 400,
                        easing = LinearEasing
                    ), RepeatMode.Reverse
                ),
                label = "y$index"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.1f, targetValue = 0.4f,
                animationSpec = infiniteRepeatable(
                    tween(2000, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse
                ), label = "a$index"
            )
            Box(
                modifier = Modifier
                    .offset(offsetX.dp, offsetY.dp)
                    .size((4 + index % 4).dp)
                    .background(
                        when (index % 4) {
                            0 -> CyanBlue
                            1 -> PurpleAccent
                            2 -> PinkAccent
                            else -> LightCyan
                        }.copy(alpha = alpha), CircleShape
                    )
            )
        }
    }
}
class ChronoAIApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("ChronoAI", "Application started")

        // Request battery optimization exemption (CRITICAL FOR BACKGROUND ALARMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("ChronoAI", "Failed to request battery optimization exemption", e)
                }
            }
        }

        // Create notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Alarm Channel (CRITICAL IMPORTANCE)
            val alarmChannel = NotificationChannel(
                "chrono_ai_alarms",
                "AI Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "AI Voice Alarms"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                enableLights(true)
                lightColor = android.graphics.Color.MAGENTA
                setBypassDnd(true) // Allow sound even in Do Not Disturb
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // Alarm Service Channel
            val serviceChannel = NotificationChannel(
                "alarm_service_channel",
                "Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Keeps alarm running in background"
                enableVibration(true)
                enableLights(true)
                lightColor = android.graphics.Color.MAGENTA
                setSound(null, null)
            }

            // Event Channel
            val eventChannel = NotificationChannel(
                "chrono_ai_events",
                "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming meetings"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(eventChannel)
        }
    }
}
@Composable
fun FloatingAISymbols() {
    val symbols = listOf(
        Icons.Default.SmartToy to CyanBlue,
        Icons.Default.Memory to PurpleAccent,
        Icons.Default.NetworkCheck to PinkAccent,
        Icons.Default.AutoAwesome to LightCyan
    )
    symbols.forEachIndexed { index, (icon, color) ->
        val infiniteTransition = rememberInfiniteTransition(label = "sym$index")
        val offset by infiniteTransition.animateFloat(
            initialValue = -10f, targetValue = 10f,
            animationSpec = infiniteRepeatable(
                tween(
                    2000 + index * 300,
                    easing = FastOutSlowInEasing
                ), RepeatMode.Reverse
            ), label = "off$index"
        )
        val angle = (index * 90 + 45) * Math.PI / 180
        val radius = 90.dp
        Box(
            modifier = Modifier
                .offset(
                    (cos(angle) * radius.value + offset).dp,
                    (sin(angle) * radius.value + offset).dp
                )
                .size(28.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
    }
}
@Composable
fun DailyChallengesSection(challenges: List<DailyChallenge>, onChallengeClick: (DailyChallenge) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Daily Challenges",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBlue
            )
            val completedCount = challenges.count { it.isCompleted }
            Text(
                "$completedCount/${challenges.size}",
                fontSize = 16.sp,
                color = NeonGreen,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(challenges.size) { index ->
                ChallengeCard(challenges[index]) { onChallengeClick(challenges[index]) }
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: DailyChallenge, onClick: () -> Unit) {
    val progress = (challenge.currentProgress.toFloat() / challenge.targetValue.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.isCompleted)
                challenge.color.copy(alpha = 0.3f)
            else
                Color(0xFF1A2332)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon and completion badge
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        challenge.color.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            challenge.icon,
                            null,
                            tint = challenge.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    if (challenge.isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Challenge name
                Text(
                    challenge.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                // Progress
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (challenge.streakCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Whatshot,
                                null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${challenge.streakCount} day streak",
                                fontSize = 11.sp,
                                color = OrangeAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2A3A4A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(challenge.color, challenge.color.copy(alpha = 0.7f))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "${challenge.currentProgress}/${challenge.targetValue}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeDetailDialog(challenge: DailyChallenge, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    challenge.color.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        challenge.icon,
                        null,
                        tint = challenge.color,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    challenge.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    challenge.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (challenge.streakCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                OrangeAccent.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Whatshot,
                            null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "${challenge.streakCount} Days",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangeAccent
                            )
                            Text(
                                "Current Streak",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${challenge.currentProgress}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = challenge.color
                        )
                        Text(
                            "Progress",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${challenge.targetValue}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightCyan
                        )
                        Text(
                            "Goal",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val percentage = ((challenge.currentProgress.toFloat() / challenge.targetValue.toFloat()) * 100).toInt()
                        Text(
                            "$percentage%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                        Text(
                            "Complete",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = challenge.color)
                ) {
                    Text("Got it!")
                }
            }
        }
    }
}
@Composable
fun UserProfileHeader(profile: UserProfile, onThemeClick: () -> Unit, onBadgesClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Your Progress",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Level ${profile.level} ${getLevelName(profile.level)}",
                        fontSize = 14.sp,
                        color = getLevelColor(profile.level)
                    )
                }

                // Points display
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(OrangeAccent, Color(0xFFFFD700))
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${profile.totalPoints}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar to next level
            val nextLevelPoints = getNextLevelPoints(profile.level)
            val currentLevelPoints = getCurrentLevelMinPoints(profile.level)
            val progress = ((profile.totalPoints - currentLevelPoints).toFloat() /
                    (nextLevelPoints - currentLevelPoints).toFloat()).coerceIn(0f, 1f)

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2A3A4A))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(CyanBlue, PurpleAccent)
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${profile.totalPoints} / $nextLevelPoints points to Level ${profile.level + 1}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    Icons.Default.EmojiEvents,
                    profile.unlockedBadges.size.toString(),
                    "Badges",
                    OrangeAccent
                ) { onBadgesClick() }

                ProfileStat(
                    Icons.Default.Palette,
                    profile.unlockedThemes.size.toString(),
                    "Themes",
                    PinkAccent
                ) { onThemeClick() }

                ProfileStat(
                    Icons.Default.Whatshot,
                    profile.longestStreak.toString(),
                    "Best Streak",
                    NeonGreen
                ) {}
            }
        }
    }
}

@Composable
fun ProfileStat(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Brush.radialGradient(
                        listOf(color.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

fun getLevelName(level: Int): String = when (level) {
    1 -> "Bronze"
    2 -> "Silver"
    3 -> "Gold"
    4 -> "Diamond"
    else -> "Legend"
}

fun getLevelColor(level: Int): Color = when (level) {
    1 -> Color(0xFFCD7F32) // Bronze
    2 -> Color(0xFFC0C0C0) // Silver
    3 -> Color(0xFFFFD700) // Gold
    4 -> Color(0xFF87CEEB) // Diamond
    else -> Color(0xFFFF1493) // Legend pink
}

fun getNextLevelPoints(currentLevel: Int): Int = when (currentLevel) {
    1 -> 500
    2 -> 1500
    3 -> 5000
    else -> 10000
}

fun getCurrentLevelMinPoints(currentLevel: Int): Int = when (currentLevel) {
    1 -> 0
    2 -> 500
    3 -> 1500
    4 -> 5000
    else -> 10000
}

@Composable
fun ThemeSelectionDialog(
    profile: UserProfile,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = getAvailableThemes()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Choose Your Theme",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBlue
                )
                Text(
                    "All themes unlocked for testing",
                    fontSize = 12.sp,
                    color = NeonGreen,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn {
                    items(themes.size) { index ->
                        ThemeOption(
                            theme = themes[index],
                            isSelected = profile.currentTheme == themes[index].id,
                            isUnlocked = true, // ← ALWAYS UNLOCKED
                            userPoints = profile.totalPoints,
                            onClick = {
                                UserProfileStorage.setCurrentTheme(context, themes[index].id)
                                onThemeSelected(themes[index].id)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun ThemeOption(
    theme: AppTheme,
    isSelected: Boolean,
    isUnlocked: Boolean,
    userPoints: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                theme.accentColor.copy(alpha = 0.3f)
            else
                Color(0xFF2A3A4A)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme preview
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(theme.primaryGradient)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    theme.previewIcon,
                    null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    theme.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
                )
                Text(
                    theme.description,
                    fontSize = 13.sp,
                    color = if (isUnlocked)
                        Color.White.copy(alpha = 0.7f)
                    else
                        Color.White.copy(alpha = 0.4f)
                )

                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${theme.pointsRequired} points (${theme.pointsRequired - userPoints} more needed)",
                            fontSize = 11.sp,
                            color = OrangeAccent
                        )
                    }
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(28.dp)
                )
            } else if (isUnlocked) {
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = Color.White.copy(alpha = 0.5f)
                )
            } else {
                Icon(
                    Icons.Default.Lock,
                    null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun BadgesDialog(profile: UserProfile, onDismiss: () -> Unit) {
    val achievements = getAvailableAchievements()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Your Badges",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBlue
                )
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn {
                    items(achievements.size) { index ->
                        BadgeItem(
                            achievement = achievements[index],
                            isUnlocked = profile.unlockedBadges.contains(achievements[index].id)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(achievement: Achievement, isUnlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                achievement.badgeColor.copy(alpha = 0.2f)
            else
                Color(0xFF2A3A4A)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = if (isUnlocked)
                            Brush.radialGradient(
                                listOf(
                                    achievement.badgeColor.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        else
                            Brush.radialGradient(  // ← FIXED: Return Brush in both cases
                                listOf(
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    achievement.icon,
                    null,
                    tint = if (isUnlocked) achievement.badgeColor else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
                )
                Text(
                    achievement.description,
                    fontSize = 13.sp,
                    color = if (isUnlocked)
                        Color.White.copy(alpha = 0.7f)
                    else
                        Color.White.copy(alpha = 0.4f)
                )

                if (isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "+${achievement.pointsAwarded} points",
                            fontSize = 11.sp,
                            color = OrangeAccent
                        )
                    }
                }
            }

            if (isUnlocked) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = achievement.badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Default.Lock,
                    null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    events: List<CalendarEvent>,
    onMarkAttendance: (CalendarEvent, Boolean) -> Unit
) {
    val context = LocalContext.current
    val currentTime by produceState(initialValue = getCurrentTimeWithLocation(context)) {
        while (true) {
            value = getCurrentTimeWithLocation(context); delay(100)
        }
    }

    // Load profile and challenges
    var profile by remember { mutableStateOf(UserProfileStorage.loadProfile(context)) }
    var challenges by remember { mutableStateOf(ChallengeStorage.initializeChallenges(context)) }
    var selectedChallenge by remember { mutableStateOf<DailyChallenge?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showBadgesDialog by remember { mutableStateOf(false) }

    // Get current theme
    val currentTheme = remember(profile.currentTheme) {
        getAvailableThemes().find { it.id == profile.currentTheme }
            ?: getAvailableThemes().first()
    }

    LaunchedEffect(Unit) {
        ChallengeStorage.checkAndResetIfNeeded(context)
        challenges = ChallengeStorage.loadChallenges(context)
        profile = UserProfileStorage.loadProfile(context)

        // Check if it's before 9 AM and update daily planner challenge
        val now = LocalTime.now()
        if (now.hour < 9) {
            ChallengeStorage.updateChallengeProgress(context, "daily_planner", 1)
            challenges = ChallengeStorage.loadChallenges(context)
            profile = UserProfileStorage.loadProfile(context)
        }
    }

    val pastEvents = events.filter { it.dateTime.isBefore(LocalDateTime.now()) }
    val attendedCount = pastEvents.count { it.isAttended }
    val attendanceRate =
        if (pastEvents.isNotEmpty()) (attendedCount * 100 / pastEvents.size) else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(currentTheme.primaryGradient) // USE THEME COLORS
            )
            .padding(16.dp)
    ) {
        item { RealTimeClockHeader(currentTime); Spacer(modifier = Modifier.height(24.dp)) }

        // User Profile Section (NEW!)
        item {
            UserProfileHeader(
                profile = profile,
                onThemeClick = { showThemeDialog = true },
                onBadgesClick = { showBadgesDialog = true }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Daily Challenges Section
        item {
            DailyChallengesSection(challenges) { challenge ->
                selectedChallenge = challenge
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (pastEvents.isNotEmpty()) {
            item {
                AttendanceChart(attendedCount, pastEvents.size, attendanceRate)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            Text(
                "AI-Powered Features",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = currentTheme.accentColor, // USE THEME COLORS
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        item { AIFeaturesSection(); Spacer(modifier = Modifier.height(32.dp)) }
        item {
            Text(
                "Quick Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = currentTheme.secondaryColor, // USE THEME COLORS
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        item {
            MainActionButton(
                "ChronoAI Calendar",
                "Manage meetings & events",
                Icons.Default.CalendarMonth,
                currentTheme.accentColor, // USE THEME COLORS
                onNavigateToCalendar
            ); Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            MainActionButton(
                "AI Smart Alarm",
                "Wake with intelligent voice alerts",
                Icons.Default.Alarm,
                PurpleAccent,
                onNavigateToAlarm
            ); Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Floating Sound Toggle Button
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        FloatingActionButton(
            onClick = {
                SoundManager.toggleSound()
                if (SoundManager.isSoundEnabled) {
                    SoundManager.playBackgroundMusic(context)
                }
            },
            containerColor = if (SoundManager.isSoundEnabled) PurpleAccent else Color.Gray,
            contentColor = Color.White,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 16.dp)
                .size(56.dp)
        ) {
            Icon(
                if (SoundManager.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = if (SoundManager.isSoundEnabled) "Mute" else "Unmute",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Challenge Detail Dialog
    selectedChallenge?.let { challenge ->
        ChallengeDetailDialog(
            challenge = challenge,
            onDismiss = {
                selectedChallenge = null
                // Reload profile in case points changed
                profile = UserProfileStorage.loadProfile(context)
            }
        )
    }

    // Theme Selection Dialog (NEW!)
    if (showThemeDialog) {
        ThemeSelectionDialog(
            profile = profile,
            onThemeSelected = { themeId ->
                profile = UserProfileStorage.loadProfile(context)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Badges Dialog (NEW!)
    if (showBadgesDialog) {
        BadgesDialog(
            profile = profile,
            onDismiss = { showBadgesDialog = false }
        )
    }
}
@Composable
fun AttendanceChart(attendedCount: Int, totalCount: Int, attendanceRate: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Meeting Attendance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightCyan
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EmojiEvents, null, tint = when {
                            attendanceRate >= 80 -> NeonGreen; attendanceRate >= 50 -> OrangeAccent; else -> PinkAccent
                        }, modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$attendanceRate%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            attendanceRate >= 80 -> NeonGreen; attendanceRate >= 50 -> OrangeAccent; else -> PinkAccent
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A3A4A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(attendanceRate / 100f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    when {
                                        attendanceRate >= 80 -> NeonGreen; attendanceRate >= 50 -> OrangeAccent; else -> PinkAccent
                                    },
                                    when {
                                        attendanceRate >= 80 -> Color(0xFF00CC00); attendanceRate >= 50 -> Color(
                                        0xFFFF8F00
                                    ); else -> Color(0xFFFF1744)
                                    }
                                )
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttendanceStat(Icons.Default.CheckCircle, attendedCount, "Attended", NeonGreen)
                AttendanceStat(
                    Icons.Default.Cancel,
                    totalCount - attendedCount,
                    "Missed",
                    PinkAccent
                )
                AttendanceStat(Icons.Default.Event, totalCount, "Total", CyanBlue)
            }
        }
    }
}

@Composable
fun AttendanceStat(icon: ImageVector, count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun RealTimeClockHeader(timeData: TimeData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "ChronoAI Logo",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    timeData.location,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    timeData.hours,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBlue
                )
                Text(
                    ":",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBlue,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    timeData.minutes,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBlue
                )
                Text(
                    ":",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBlue,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        timeData.seconds,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightCyan
                    )
                    Text(
                        ".${timeData.milliseconds}",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    timeData.amPm,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PinkAccent,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(timeData.date, fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun AIFeaturesSection() {
    val features = listOf(
        AIFeature(
            "Smart Scheduling",
            "AI suggests optimal meeting times",
            Icons.Default.SmartToy,
            CyanBlue
        ),
        AIFeature(
            "Voice Alerts",
            "AI voice announces your events",
            Icons.Default.RecordVoiceOver,
            PurpleAccent
        ),
        AIFeature(
            "Auto-Reminders",
            "10 & 5 min notifications before events",
            Icons.Default.NotificationsActive,
            PinkAccent
        ),
        AIFeature(
            "Time Intelligence",
            "Location-based time tracking",
            Icons.Default.Public,
            Color(0xFF10B981)
        )
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(features.size) { index ->
            AIFeatureCard(features[index])
        }
    }
}

@Composable
fun AIFeatureCard(feature: AIFeature) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "glow"
    )

    Card(
        modifier = Modifier
            .widthIn(min = 140.dp, max = 180.dp) // Flexible width
            .heightIn(min = 160.dp, max = 200.dp) // Flexible height
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                feature.color.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ), CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(2.dp, feature.color.copy(alpha = 0.5f), CircleShape)
                )
                Icon(feature.icon, null, tint = feature.color, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                feature.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                feature.description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun MainActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp, max = 120.dp) // Flexible height
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable {
                pressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                color.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    color.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ), CircleShape
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                }
                Icon(
                    Icons.Default.ArrowForward,
                    null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(150); pressed = false
        }
    }
}

@Composable
fun UpcomingEventCard(event: CalendarEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                event.category.color.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ), CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                Icon(
                    event.category.icon,
                    null,
                    tint = event.category.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    event.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a")),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    events: List<CalendarEvent>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onAddEvent: () -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onBack: () -> Unit,
    onMarkAttendance: (CalendarEvent, Boolean) -> Unit
) {
    val context = LocalContext.current
    val currentTime by produceState(initialValue = getCurrentTimeWithLocation(context)) {
        while (true) {
            value = getCurrentTimeWithLocation(context); delay(100)
        }
    }
    val eventsForDate =
        events.filter { it.dateTime.toLocalDate() == selectedDate }.sortedBy { it.dateTime }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkNavy, Color(0xFF0D2137))))
    ) {
        TopAppBar(
            title = { Text("ChronoAI Calendar", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "Back",
                        tint = CyanBlue
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A2332),
                titleContentColor = CyanBlue
            )
        )

        LazyColumn(modifier = Modifier
            .weight(1f)
            .padding(16.dp)) {
            item { RealTimeClockHeader(currentTime); Spacer(modifier = Modifier.height(16.dp)) }
            item {
                CalendarGrid(
                    selectedDate.month,
                    selectedDate.year,
                    selectedDate,
                    events,
                    onDateSelected
                ); Spacer(modifier = Modifier.height(24.dp))
            }
            if (eventsForDate.isNotEmpty()) {
                item {
                    Text(
                        "Today's Schedule",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightCyan,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ); AIClockView(eventsForDate); Spacer(modifier = Modifier.height(24.dp))
                }
            }
            item {
                Text(
                    selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            if (eventsForDate.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EventBusy,
                                null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            ); Spacer(modifier = Modifier.height(12.dp)); Text(
                            "No events scheduled",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        }
                    }
                }
            } else {
                items(eventsForDate.size) { index ->
                    EventCard(
                        eventsForDate[index],
                        { onDeleteEvent(eventsForDate[index]) },
                        { attended -> onMarkAttendance(eventsForDate[index], attended) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = onAddEvent,
                containerColor = CyanBlue,
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) { Icon(Icons.Default.Add, "Add Event", modifier = Modifier.size(32.dp)) }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Month,
    currentYear: Int,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDay = LocalDate.of(currentYear, currentMonth, 1)
    val daysInMonth = currentMonth.length(Year.of(currentYear).isLeap)
    val startDayOfWeek = firstDay.dayOfWeek.value % 7

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Flexible height based on content
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${currentMonth.name} $currentYear",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBlue,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightCyan
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            var dayCounter = 1
            for (week in 0..5) {
                if (dayCounter > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in 0..6) {
                        val cellIndex = week * 7 + day
                        if (cellIndex >= startDayOfWeek && dayCounter <= daysInMonth) {
                            val date = LocalDate.of(currentYear, currentMonth, dayCounter)
                            CalendarDay(
                                dayCounter,
                                date == selectedDate,
                                date == LocalDate.now(),
                                events.any { it.dateTime.toLocalDate() == date }) {
                                onDateSelected(
                                    date
                                )
                            }
                            dayCounter++
                        } else Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun RowScope.CalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> CyanBlue; isToday -> CyanBlue.copy(alpha = 0.3f); else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .background(bgColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f)
            )
            if (hasEvents) Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(PinkAccent, CircleShape)
            )
        }
    }
}

@Composable
fun AIClockView(events: List<CalendarEvent>) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                CyanBlue.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent
                            )
                        ), CircleShape
                    )
            )
            Canvas(modifier = Modifier.size(250.dp)) {
                val radius = size.minDimension / 2
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(DarkNavy, radius, center)
                drawCircle(
                    CyanBlue.copy(alpha = 0.3f),
                    radius,
                    center,
                    style = Stroke(width = 3f)
                )
                for (hour in 0..11) {
                    val angle = (hour * 30 - 90) * Math.PI / 180
                    drawLine(
                        LightCyan.copy(alpha = 0.7f),
                        Offset(
                            center.x + ((radius - 20) * cos(angle)).toFloat(),
                            center.y + ((radius - 20) * sin(angle)).toFloat()
                        ),
                        Offset(
                            center.x + ((radius - 10) * cos(angle)).toFloat(),
                            center.y + ((radius - 10) * sin(angle)).toFloat()
                        ),
                        3f,
                        StrokeCap.Round
                    )
                }
                events.forEach { event ->
                    val hour = event.dateTime.hour % 12
                    val minute = event.dateTime.minute
                    val totalMinutes = hour * 60 + minute
                    val angle = (totalMinutes * 0.5 - 90) * Math.PI / 180
                    val eventRadius = radius - 35f
                    drawCircle(
                        event.category.color.copy(alpha = 0.3f),
                        12f,
                        Offset(
                            center.x + (eventRadius * cos(angle)).toFloat(),
                            center.y + (eventRadius * sin(angle)).toFloat()
                        )
                    )
                    drawCircle(
                        event.category.color,
                        8f,
                        Offset(
                            center.x + (eventRadius * cos(angle)).toFloat(),
                            center.y + (eventRadius * sin(angle)).toFloat()
                        )
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${events.size}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBlue
                )
                Text(
                    if (events.size == 1) "Event" else "Events",
                    fontSize = 16.sp,
                    color = LightCyan
                )
            }
        }
    }
}

@Composable
fun EventCard(event: CalendarEvent, onDelete: () -> Unit, onMarkAttendance: (Boolean) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAttendanceDialog by remember { mutableStateOf(false) }
    val isPastEvent = event.dateTime.isBefore(LocalDateTime.now())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(70.dp)
            ) {
                Text(
                    event.dateTime.format(DateTimeFormatter.ofPattern("hh:mm")),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = event.category.color
                )
                Text(
                    event.dateTime.format(DateTimeFormatter.ofPattern("a")),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = PinkAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        event.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp)); Text(
                        event.description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${event.duration} min - ${event.category.name}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            if (isPastEvent) {
                IconButton(onClick = { showAttendanceDialog = true }) {
                    Icon(
                        if (event.isAttended) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        "Mark Attendance",
                        tint = if (event.isAttended) NeonGreen else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    "Delete",
                    tint = PinkAccent
                )
            }
        }
    }
    if (showDeleteDialog) AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = { Text("Delete Event") },
        text = { Text("Are you sure?") },
        confirmButton = {
            TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                Text(
                    "Delete",
                    color = PinkAccent
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteDialog = false }) {
                Text(
                    "Cancel",
                    color = LightCyan
                )
            }
        },
        containerColor = Color(0xFF1A2332)
    )

    if (showAttendanceDialog) AlertDialog(
        onDismissRequest = { showAttendanceDialog = false },
        title = { Text("Mark Attendance") },
        text = { Text("Did you attend this meeting?") },
        confirmButton = {
            Button(
                onClick = {
                    onMarkAttendance(true); showAttendanceDialog = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) { Text("Yes, Attended") }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onMarkAttendance(false); showAttendanceDialog = false
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkAccent)
            ) { Text("No, Missed") }
        },
        containerColor = Color(0xFF1A2332)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onEventAdded: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(EventCategory.MEETING) }
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }
    var duration by remember { mutableStateOf(60) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
        ) {
            LazyColumn(modifier = Modifier.padding(24.dp)) {
                item {
                    Text(
                        "Add New Event",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBlue
                    ); Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanBlue,
                            unfocusedBorderColor = LightCyan.copy(alpha = 0.5f),
                            focusedLabelColor = CyanBlue,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanBlue,
                            unfocusedBorderColor = LightCyan.copy(alpha = 0.5f),
                            focusedLabelColor = CyanBlue,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Category",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    ); Spacer(modifier = Modifier.height(8.dp))
                }
                val categories = EventCategory.entries.filter { it != EventCategory.ALARM }
                items(categories.size) { index ->
                    CategoryChip(
                        categories[index],
                        selectedCategory == categories[index]
                    ) {
                        selectedCategory = categories[index]
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp)); Text(
                    "Time",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                ); Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeSelector(
                            "Hour",
                            selectedHour,
                            1..12,
                            { selectedHour = it },
                            Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp)); Text(
                        ":",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBlue
                    ); Spacer(modifier = Modifier.width(8.dp))
                        TimeSelector(
                            "Minute",
                            selectedMinute,
                            0..59,
                            { selectedMinute = it },
                            Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Period",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            ); Spacer(modifier = Modifier.height(4.dp)); Row {
                            FilterChip(
                                selected = isAm,
                                onClick = { isAm = true },
                                label = { Text("AM") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2A3A4A),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = !isAm,
                                onClick = { isAm = false },
                                label = { Text("PM") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2A3A4A),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)); Text(
                    "Duration (minutes)",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                ); Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60, 90, 120).forEach {
                            FilterChip(
                                selected = duration == it,
                                onClick = { duration = it },
                                label = { Text("$it min") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2A3A4A),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                "Cancel",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }; Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotEmpty()) {
                                    val hour24 = when {
                                        isAm && selectedHour == 12 -> 0; isAm -> selectedHour; !isAm && selectedHour == 12 -> 12; else -> selectedHour + 12
                                    }
                                    onEventAdded(
                                        CalendarEvent(
                                            title = title,
                                            description = description,
                                            dateTime = LocalDateTime.of(
                                                selectedDate,
                                                LocalTime.of(hour24, selectedMinute)
                                            ),
                                            category = selectedCategory,
                                            duration = duration
                                        )
                                    )
                                }
                            },
                            enabled = title.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanBlue,
                                contentColor = Color.White
                            )
                        ) { Text("Add Event") }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronoAIApp(textToSpeech: TextToSpeech, context: Context) {
    var showSplash by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf("home") }
    var events by remember { mutableStateOf(emptyList<CalendarEvent>()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showEventDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val saved = EventStorage.loadEvents(context)
        events = if (saved.isNotEmpty()) saved else loadSampleEvents()
        EventStorage.saveEvents(context, events) // Save immediately
    }
    LaunchedEffect(Unit) {
        val saved = EventStorage.loadEvents(context)
        events = if (saved.isNotEmpty()) saved else loadSampleEvents()
        EventStorage.saveEvents(context, events)
    }

    LaunchedEffect(events) { EventStorage.saveEvents(context, events) }

    // Refresh events when returning to home screen
    LaunchedEffect(currentScreen) {
        if (currentScreen == "home") {
            // Force reload from storage
            val loadedEvents = EventStorage.loadEvents(context)
            events = emptyList() // Clear first
            delay(50) // Small delay to force recomposition
            events = loadedEvents
            Log.d("ChronoAI", "Refreshed ${events.size} events on home screen")
        }
    }

    LaunchedEffect(events) { EventStorage.saveEvents(context, events) }

    LaunchedEffect(events) {
        while (true) {
            checkUpcomingEvents(events, context, textToSpeech)
            delay(30000)
        }
    }

    BackHandler { showExitDialog = true }

    if (showSplash) {
        EnhancedTwoStageSplash { showSplash = false }
    }else {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkNavy) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    onNavigateToCalendar = { currentScreen = "calendar" },
                    onNavigateToAlarm = { currentScreen = "alarm" },
                    events = events,
                    onMarkAttendance = { event, attended ->
                        events =
                            events.map { if (it.id == event.id) it.copy(isAttended = attended) else it }
                        EventStorage.saveAttendance(context, event.id, attended)
                    }
                )

                "calendar" -> CalendarScreen(
                    events = events, selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    onAddEvent = { showEventDialog = true },
                    onDeleteEvent = { event -> events = events.filter { it.id != event.id } },
                    onBack = { currentScreen = "home" },
                    onMarkAttendance = { event, attended ->
                        events = events.map { if (it.id == event.id) it.copy(isAttended = attended) else it }
                        EventStorage.saveAttendance(context, event.id, attended)

                        // Track attendance challenge (NEW!)
                        if (attended) {
                            ChallengeStorage.updateChallengeProgress(context, "perfect_attendance", 1)
                            ChallengeStorage.updateChallengeProgress(context, "on_time", 1)
                        }
                    }
                )

                "alarm" -> AlarmScreen(
                    onBack = { currentScreen = "home" },
                    onSetAlarm = { time, voiceName -> scheduleAlarm(time, voiceName, context) },
                    textToSpeech = textToSpeech
                )
            }

            if (showEventDialog) {
                AddEventDialog(
                    selectedDate = selectedDate,
                    onDismiss = { showEventDialog = false },
                    onEventAdded = { event ->
                        events = events + event
                        EventStorage.saveEvents(context, events) // SAVE IMMEDIATELY
                        showEventDialog = false
                        scheduleEventNotifications(event, context)
                        Toast.makeText(context, "Event scheduled!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showExitDialog) {
                ExitConfirmationDialog(
                    onConfirm = { (context as? ComponentActivity)?.finish() },
                    onDismiss = { showExitDialog = false }
                )
            }
        }
    }
}

@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ExitToApp,
                    null,
                    tint = PinkAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Exit ChronoAI?", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                "Are you sure? Alarms will continue in background.",
                color = Color.White.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PinkAccent)
            ) { Text("Yes, Exit") }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightCyan)
            ) { Text("Stay") }
        },
        containerColor = Color(0xFF1A2332), shape = RoundedCornerShape(20.dp)
    )
}
@Composable
fun EnhancedTwoStageSplash(onComplete: () -> Unit) {
    var stage by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        delay(2000) // Stage 1: Logo only for 2 seconds
        stage = 2
        delay(3000) // Stage 2: Welcome screen for 3 seconds
        onComplete()
    }

    when (stage) {
        1 -> LogoSplashStage()
        2 -> WelcomeSplashStage()
    }
}

@Composable
fun LogoSplashStage() {
    val context = LocalContext.current

    // Play splash sound once
    LaunchedEffect(Unit) {
        SoundManager.playSplashSound(context)
    }

    val scale by rememberInfiniteTransition(label = "logo_scale").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A1F3D), // Match logo's dark navy blue
                            Color(0xFF1A3555), // Medium blue from logo center
                            Color(0xFF0F2847), // Deeper blue
                            Color(0xFF05121F)  // Almost black at bottom
                        )
                    )
                ),
        contentAlignment = Alignment.Center
    ) {
        FloatingParticles()

        Box(
            modifier = Modifier.scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CyanBlue.copy(alpha = 0.4f),
                                PurpleAccent.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "ChronoAI Logo",
                modifier = Modifier.size(250.dp)
            )
        }
    }
}

@Composable
fun WelcomeSplashStage() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1929), // Navy from logo
                        Color(0xFF1A3A52), // Blue from logo
                        Color(0xFF2A4A62)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        FloatingParticles()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .alpha(alpha)
        ) {
            // Animated brain/AI icon
            val infiniteTransition = rememberInfiniteTransition(label = "brain")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    tween(20000, easing = LinearEasing)
                ),
                label = "rotation"
            )

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .rotate(rotation)
                        .border(
                            3.dp,
                            Brush.sweepGradient(
                                colors = listOf(
                                    CyanBlue,
                                    PurpleAccent,
                                    PinkAccent,
                                    CyanBlue
                                )
                            ),
                            CircleShape
                        )
                )
                Icon(
                    Icons.Default.Psychology,
                    null,
                    tint = CyanBlue,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Main text
            Text(
                text = "Welcome to the World of AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ChronoAI Calendar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = LightCyan
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Animated tagline
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = PinkAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Powered by Intelligent Automation",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = PinkAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Progress indicator
            CircularProgressIndicator(
                color = CyanBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun FloatingParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(30) { index ->
            val randomX = remember { kotlin.random.Random.nextFloat() }
            val randomY = remember { kotlin.random.Random.nextFloat() }
            val randomX2 = remember { kotlin.random.Random.nextFloat() }
            val randomY2 = remember { kotlin.random.Random.nextFloat() }

            val offsetX by infiniteTransition.animateFloat(
                initialValue = randomX * 1000f,
                targetValue = randomX2 * 1000f,
                animationSpec = infiniteRepeatable(
                    tween(8000 + index * 200, easing = LinearEasing),
                    RepeatMode.Reverse
                ),
                label = "x$index"
            )

            val offsetY by infiniteTransition.animateFloat(
                initialValue = randomY * 2000f,
                targetValue = randomY2 * 2000f,
                animationSpec = infiniteRepeatable(
                    tween(10000 + index * 150, easing = LinearEasing),
                    RepeatMode.Reverse
                ),
                label = "y$index"
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    tween(3000, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse
                ),
                label = "a$index"
            )

            val particleColor = when (index % 3) {
                0 -> CyanBlue
                1 -> PurpleAccent
                else -> Color(0xFF4FC3F7) // Light blue from logo
            }

            Box(
                modifier = Modifier
                    .offset(offsetX.dp, offsetY.dp)
                    .size((3 + index % 5).dp)
                    .background(particleColor.copy(alpha = alpha), CircleShape)
            )
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(3000)
        onTimeout()
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "alpha"
    )
    val rotation by animateFloatAsState(
        targetValue = if (visible) 360f else 0f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "rot"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DarkNavy,
                        Color(0xFF0D2137),
                        Color(0xFF1A1A2E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedBackgroundParticles()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    CyanBlue.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ), CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .rotate(rotation)
                        .border(
                            3.dp,
                            Brush.sweepGradient(
                                listOf(
                                    CyanBlue,
                                    PurpleAccent,
                                    PinkAccent,
                                    CyanBlue
                                )
                            ),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF1A2332), DarkNavy)),
                            CircleShape
                        )
                        .shadow(20.dp, CircleShape, spotColor = CyanBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "ChronoAI Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
                FloatingAISymbols()
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "Welcome to the World of AI",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = CyanBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "ChronoAI Calendar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = LightCyan,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = CyanBlue,
                trackColor = Color(0xFF2A3A4A)
            )
        }
    }
}


@Composable
fun CategoryChip(category: EventCategory, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) category.color.copy(alpha = 0.3f) else Color(
                0xFF2A3A4A
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                category.icon,
                null,
                tint = category.color,
                modifier = Modifier.size(24.dp)
            ); Spacer(modifier = Modifier.width(12.dp)); Text(
            category.name,
            color = Color.White,
            fontSize = 16.sp
        )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelector(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(value.toString().padStart(2, '0')) }

    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                // Allow typing
                if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                    textValue = newValue

                    // Only update parent when valid
                    val n = newValue.toIntOrNull()
                    if (n != null && n in range) {
                        onValueChange(n)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanBlue,
                unfocusedBorderColor = LightCyan.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1A2332),
                unfocusedContainerColor = Color(0xFF1A2332)
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingAlarmCard(alarm: UpcomingAlarm, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    alarm.time.format(DateTimeFormatter.ofPattern("hh:mm")),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent
                )
                Text(
                    alarm.time.format(DateTimeFormatter.ofPattern("a")),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, null, tint = PinkAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(alarm.soundName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, "Delete", tint = PinkAccent)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Alarm", color = Color.White) },
            text = { Text("Are you sure you want to delete this alarm?", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = PinkAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = LightCyan)
                }
            },
            containerColor = Color(0xFF1A2332)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    onBack: () -> Unit,
    onSetAlarm: (LocalTime, String) -> Unit,
    textToSpeech: TextToSpeech
) {
    val context = LocalContext.current
    var selectedHour by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }
    var selectedVoice by remember { mutableStateOf(getAlarmTones(context)[0]) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var upcomingAlarms by remember { mutableStateOf(loadUpcomingAlarms(context)) }

    val currentTime by produceState(initialValue = getCurrentTimeWithLocation(context)) {
        while (true) {
            value = getCurrentTimeWithLocation(context)
            delay(100)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkNavy, Color(0xFF0D2137))))
    ) {
        TopAppBar(
            title = { Text("AI Smart Alarm", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = PurpleAccent)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A2332),
                titleContentColor = PurpleAccent
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item { RealTimeClockHeader(currentTime); Spacer(modifier = Modifier.height(24.dp)) }

            item {
                AIAlarmClockDisplay(selectedHour, selectedMinute, isAm)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Set Alarm Time",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeSelector(
                                "Hour",
                                selectedHour,
                                1..12,
                                { selectedHour = it },
                                Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            TimeSelector(
                                "Minute",
                                selectedMinute,
                                0..59,
                                { selectedMinute = it },
                                Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text("Period", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Column {
                                    FilterChip(
                                        selected = isAm,
                                        onClick = { isAm = true },
                                        label = { Text("AM") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PurpleAccent,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF2A3A4A),
                                            labelColor = Color.White.copy(alpha = 0.7f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FilterChip(
                                        selected = !isAm,
                                        onClick = { isAm = false },
                                        label = { Text("PM") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PurpleAccent,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF2A3A4A),
                                            labelColor = Color.White.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp))
                        .clickable { showVoiceDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, null, tint = PinkAccent, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alarm Sound", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(selectedVoice.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(selectedVoice.voiceType, fontSize = 13.sp, color = PinkAccent)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = PurpleAccent)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedButton(
                        onClick = {
                            // PAUSE background music during test
                            SoundManager.pauseBackgroundMusic()

                            try {
                                val testPlayer = MediaPlayer().apply {
                                    setDataSource(context, selectedVoice.uri!!)
                                    setAudioAttributes(
                                        AudioAttributes.Builder()
                                            .setUsage(AudioAttributes.USAGE_ALARM)
                                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                            .build()
                                    )
                                    prepare()
                                    start()
                                    setOnCompletionListener {
                                        it.release()
                                        // RESUME background music after test
                                        SoundManager.resumeBackgroundMusic()
                                    }
                                }
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    if (testPlayer.isPlaying) {
                                        testPlayer.stop()
                                        testPlayer.release()
                                        // RESUME background music if stopped early
                                        SoundManager.resumeBackgroundMusic()
                                    }
                                }, 3000)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Could not play alarm sound", Toast.LENGTH_SHORT).show()
                                // Resume even on error
                                SoundManager.resumeBackgroundMusic()
                            }
                        },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkAccent)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Alarm Sound")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // SET ALARM BUTTON
            item {
                Button(
                    onClick = {
                        val hour24 = when {
                            isAm && selectedHour == 12 -> 0
                            isAm -> selectedHour
                            !isAm && selectedHour == 12 -> 12
                            else -> selectedHour + 12
                        }
                        val time = LocalTime.of(hour24, selectedMinute)
                        onSetAlarm(time, selectedVoice.name)

                        // Save to upcoming alarms
                        val alarm = UpcomingAlarm(
                            id = UUID.randomUUID().toString(),
                            time = time,
                            soundName = selectedVoice.name,
                            soundUri = selectedVoice.uri.toString()
                        )
                        upcomingAlarms = upcomingAlarms + alarm
                        saveUpcomingAlarms(context, upcomingAlarms)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Alarm, null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Set Alarm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // UPCOMING ALARMS SECTION
            if (upcomingAlarms.isNotEmpty()) {
                item {
                    Text(
                        "Upcoming Alarms",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightCyan,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(upcomingAlarms.size) { index ->
                    UpcomingAlarmCard(
                        alarm = upcomingAlarms[index],
                        onDelete = {
                            // FIX: Store the alarm to delete BEFORE filtering
                            val alarmToDelete = upcomingAlarms[index]

                            // Update the list
                            upcomingAlarms = upcomingAlarms.filter { it.id != alarmToDelete.id }

                            // Save the updated list
                            saveUpcomingAlarms(context, upcomingAlarms)

                            // Cancel the alarm
                            cancelAlarm(context, alarmToDelete.id.hashCode())

                            // Show confirmation
                            Toast.makeText(context, "Alarm deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (showVoiceDialog) {
        VoiceSelectionDialog(
            selectedVoice,
            context,
            { selectedVoice = it; showVoiceDialog = false }
        ) { showVoiceDialog = false }
    }
}
@Composable
    fun AIAlarmClockDisplay(hour: Int, minute: Int, isAm: Boolean) {
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
            label = "glow"
        )
        val blinkAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
            label = "blink"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp, max = 320.dp) // Flexible height
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    PurpleAccent.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ), CircleShape
                        )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            hour.toString().padStart(2, '0'),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent
                        )
                        Text(
                            ":",
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent.copy(alpha = blinkAlpha),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            minute.toString().padStart(2, '0'),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isAm) "AM" else "PM",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkAccent,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Alarm,
                            null,
                            tint = PinkAccent,
                            modifier = Modifier.size(20.dp)
                        ); Spacer(modifier = Modifier.width(8.dp)); Text(
                        "AI Voice Alarm",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    }
                }
            }
        }
    }

    @Composable
    fun VoiceSelectionDialog(
        currentVoice: AlarmTone,
        context: Context,
        onVoiceSelected: (AlarmTone) -> Unit,
        onDismiss: () -> Unit
    ) {
        val voices = getAlarmTones(context)
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Choose Alarm Sound",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAccent
                    ); Spacer(modifier = Modifier.height(20.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(voices.size) { index ->
                            VoiceOption(
                                voices[index],
                                voices[index].name == currentVoice.name
                            ) { onVoiceSelected(voices[index]) }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun VoiceOption(voice: AlarmTone, isSelected: Boolean, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) PurpleAccent.copy(alpha = 0.3f) else Color(
                    0xFF2A3A4A
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.RecordVoiceOver,
                    null,
                    tint = if (isSelected) PurpleAccent else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                ); Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        voice.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ); Text(
                    voice.voiceType,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                }
                if (isSelected) Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = PurpleAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

data class UpcomingAlarm(
    val id: String,
    val time: LocalTime,
    val soundName: String,
    val soundUri: String
)

fun saveUpcomingAlarms(context: Context, alarms: List<UpcomingAlarm>) {
    val prefs = context.getSharedPreferences("chrono_ai_prefs", Context.MODE_PRIVATE)
    val alarmStrings = alarms.map { "${it.id}|||${it.time}|||${it.soundName}|||${it.soundUri}" }
    prefs.edit().putStringSet("upcoming_alarms", alarmStrings.toSet()).apply()
}

fun loadUpcomingAlarms(context: Context): List<UpcomingAlarm> {
    val prefs = context.getSharedPreferences("chrono_ai_prefs", Context.MODE_PRIVATE)
    val alarmStrings = prefs.getStringSet("upcoming_alarms", emptySet()) ?: emptySet()
    return alarmStrings.mapNotNull {
        try {
            val parts = it.split("|||")
            UpcomingAlarm(parts[0], LocalTime.parse(parts[1]), parts[2], parts[3])
        } catch (e: Exception) { null }
    }.sortedBy { it.time }
}

fun cancelAlarm(context: Context, alarmId: Int) {
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, alarmId, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}

object SoundManager {
    private var backgroundPlayer: MediaPlayer? = null
    private var splashPlayer: MediaPlayer? = null
    var isSoundEnabled by mutableStateOf(true)
    var isPlaying = false

    fun playSplashSound(context: Context) {
        if (!isSoundEnabled) return
        try {
            splashPlayer?.release()
            splashPlayer = MediaPlayer.create(context, R.raw.splash_intro).apply {
                isLooping = false
                setVolume(0.6f, 0.6f)
                start()
                setOnCompletionListener {
                    it.release()
                    splashPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playBackgroundMusic(context: Context) {

        if (!isSoundEnabled || isPlaying) return
        try {
            backgroundPlayer?.release()
            backgroundPlayer = MediaPlayer.create(context, R.raw.background_music).apply {
                isLooping = true
                setVolume(0.15f, 0.15f) // Very light background music
                start()
            }
            isPlaying = true  // ← MOVE THIS OUTSIDE the apply block
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseBackgroundMusic() {
        backgroundPlayer?.apply {
            if (isPlaying()) {  // ← ADD PARENTHESES HERE
                pause()
            }
        }
        isPlaying = false  // ← Update our tracking variable
    }

    fun resumeBackgroundMusic() {
        if (!isSoundEnabled) return
        backgroundPlayer?.apply {
            if (!isPlaying()) {  // ← ADD PARENTHESES HERE
                start()
            }
        }
        isPlaying = true  // ← Update our tracking variable
    }

    fun stopBackgroundMusic() {
        backgroundPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        backgroundPlayer = null
        isPlaying = false
    }

    fun stopSplashSound() {
        splashPlayer?.apply {
            if (isPlaying()) {  // ← ADD PARENTHESES HERE
                stop()
            }
            release()
        }
        splashPlayer = null
    }

    fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
        if (!isSoundEnabled) {
            stopBackgroundMusic()
        }
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private var ttsInitialized = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (!it) Toast.makeText(this, "Notification permission required", Toast.LENGTH_LONG)
            .show()
    }

    private val alarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInitialized = true
                textToSpeech.language = Locale.US
                textToSpeech.setSpeechRate(0.9f)
                textToSpeech.setPitch(1.0f)
            }
        }
        // Check and request battery optimization disable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                alarmPermissionLauncher.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            } else {
                Log.d("ChronoAI", "Exact alarm permission granted")
            }
        }

        // Verify battery optimization
        val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Log.d("ChronoAI", "App is subject to battery optimization - alarms may not work reliably")
                Toast.makeText(
                    this,
                    "Please disable battery optimization for reliable alarms",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContent {
            ChronoAIApp(textToSpeech = textToSpeech, context = this)
        }

        // Start background music after splash screen
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            SoundManager.playBackgroundMusic(this)
        }, 5000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val eventChannel = NotificationChannel(
                "chrono_ai_events",
                "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming meetings"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                )
            }

            val alarmChannel = NotificationChannel(
                "chrono_ai_alarms",
                "AI Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "AI Voice Alarms"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                enableLights(true)
                lightColor = android.graphics.Color.MAGENTA
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                )
            }

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(eventChannel)
                createNotificationChannel(alarmChannel)
            }
        }
    }

    override fun onDestroy() {
        if (ttsInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        SoundManager.stopBackgroundMusic()
        SoundManager.stopSplashSound()
        super.onDestroy()
    }
}
class AlarmForegroundService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val CHANNEL_ID = "alarm_service_channel"
        const val NOTIFICATION_ID = 8888
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val soundUri = intent?.getStringExtra("sound_uri")
        val alarmName = intent?.getStringExtra("alarm_name") ?: "Alarm"

        // Start foreground service
        val notification = createNotification(alarmName, soundUri ?: "")
        startForeground(NOTIFICATION_ID, notification)

        // Play alarm sound
        playAlarmSound(soundUri)
        startVibration()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Keeps alarm running in background"
                enableVibration(true)
                enableLights(true)
                lightColor = android.graphics.Color.MAGENTA
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(alarmName: String, soundUri: String): Notification {
        val stopIntent = Intent(this, AlarmStopReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activityIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("alarm_name", alarmName)
            putExtra("sound_uri", soundUri)
        }
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ ALARM RINGING")
            .setContentText(alarmName)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(activityPendingIntent, true)
            .addAction(
                android.R.drawable.ic_delete,
                "STOP",
                stopPendingIntent
            )
            .setSound(null)
            .build()
    }

    private fun playAlarmSound(soundUriString: String?) {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            val soundUri = if (!soundUriString.isNullOrEmpty()) {
                Uri.parse(soundUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            Log.d("AlarmService", "Playing alarm sound: $soundUri")

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmForegroundService, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Error playing sound: ${e.message}", e)
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(
                        this@AlarmForegroundService,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    )
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e2: Exception) {
                Log.e("AlarmService", "Failed to play fallback sound", e2)
            }
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Vibration error: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        Log.d("AlarmService", "Service destroyed - stopping alarm")
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d("ChronoAI", "Device booted, restoring alarms...")

                // Reload and reschedule all alarms
                val alarms = loadUpcomingAlarms(context)
                alarms.forEach { alarm ->
                    try {
                        scheduleAlarm(alarm.time, alarm.soundName, context)
                        Log.d("ChronoAI", "Restored alarm: ${alarm.soundName} at ${alarm.time}")
                    } catch (e: Exception) {
                        Log.e("ChronoAI", "Failed to restore alarm: ${e.message}")
                    }
                }

                if (alarms.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        "Restored ${alarms.size} alarm(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
class AlarmRingingActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: android.os.PowerManager.WakeLock? = null
    private var isFromReceiver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("ChronoAI", "AlarmRingingActivity started!")

        isFromReceiver = intent.getBooleanExtra("from_receiver", false)

        // Critical: Show on lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(this, object : android.app.KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissError() {}
                override fun onDismissSucceeded() {}
                override fun onDismissCancelled() {}
            })
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Acquire full wake lock
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.FULL_WAKE_LOCK or
                    android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    android.os.PowerManager.ON_AFTER_RELEASE,
            "ChronoAI::AlarmActivityWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes

        val soundUriString = intent.getStringExtra("sound_uri")
        val alarmName = intent.getStringExtra("alarm_name") ?: "Alarm"

        // Play sound at MAX VOLUME
        playAlarmSound(soundUriString)

        // Start vibration
        startVibration()

        // Cancel any fallback notification
        NotificationManagerCompat.from(this).cancel(9999)

        setContent {
            AlarmRingingScreen(
                alarmName = alarmName,
                onDismiss = {
                    // Stop the foreground service
                    val serviceIntent = Intent(this, AlarmForegroundService::class.java)
                    stopService(serviceIntent)

                    // Track no-snooze challenge (NEW!)
                    ChallengeStorage.updateChallengeProgress(this, "no_snooze", 1)
                    ChallengeStorage.updateChallengeProgress(this, "early_bird", 1)

                    stopAlarm()
                    finish()
                },
                onSnooze = {
                    // Stop the foreground service
                    val serviceIntent = Intent(this, AlarmForegroundService::class.java)
                    stopService(serviceIntent)

                    stopAlarm()
                    scheduleSnooze(soundUriString ?: "", alarmName)
                    Toast.makeText(this, "Alarm snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        }
    }

    private fun playAlarmSound(soundUriString: String?) {
        try {
            // Max volume
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            val soundUri = if (!soundUriString.isNullOrEmpty()) {
                Uri.parse(soundUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            Log.d("ChronoAI", "Playing sound: $soundUri")

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmRingingActivity, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("ChronoAI", "Error playing sound: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e("ChronoAI", "Vibration error: ${e.message}")
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        // Stop vibration
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
        } catch (e: Exception) {}
    }

    private fun scheduleSnooze(soundUri: String, alarmName: String) {
        val snoozeTime = LocalTime.now().plusMinutes(5)
        val triggerTime = LocalDateTime.of(LocalDate.now(), snoozeTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("voice_name", alarmName)
            putExtra("sound_uri", soundUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    override fun onDestroy() {
        stopAlarm()
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }
}

@Composable
fun AlarmRingingScreen(alarmName: String, onDismiss: () -> Unit, onSnooze: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0033), Color(0xFF330066), Color(0xFF4D0099))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "alarm")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                label = "scale"
            )

            Icon(
                Icons.Default.Alarm,
                null,
                tint = PinkAccent,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "ALARM",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PinkAccent
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                alarmName,
                fontSize = 24.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("STOP", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightCyan),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(2.dp, LightCyan)
            ) {
                Icon(Icons.Default.Snooze, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("SNOOZE 5 MIN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

