package ir.madreseyar.guardian

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.json.JSONObject

private val Purple = Color(0xFF7654E8)
private val PurpleDark = Color(0xFF5536C9)
private val Lilac = Color(0xFFF0ECFF)
private val Surface = Color(0xFFF8F7FC)
private val Text = Color(0xFF2D2840)
private val Muted = Color(0xFF817A95)
private val Green = Color(0xFF2F9E70)
private val Red = Color(0xFFD9535F)

class MainActivity : AppCompatActivity() {
    var onRequireUnlock: (() -> Unit)? = null
    private var stopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GuardianApp(this, remember { SecureTokenStore(this) }) }
    }

    override fun onStop() { super.onStop(); stopped = true }
    override fun onStart() { super.onStart(); if (stopped) { stopped = false; onRequireUnlock?.invoke() } }

    fun biometricAvailable(): Boolean = BiometricManager.from(this).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
    ) == BiometricManager.BIOMETRIC_SUCCESS

    fun authenticateBiometric(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { onError(errString.toString()) }
        })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("مدرسه‌یار اولیا")
                .setSubtitle("برای ورود به اطلاعات فرزند، اثر انگشت را تأیید کنید")
                .setNegativeButtonText("استفاده از رمز")
                .build()
        )
    }
}

enum class GScreen { Unlock, Login, ChangePassword, Home, Profile, Grades, ExamReport, Attendance, Homework, Schedule, Messages, Settings }

@Composable
fun GuardianApp(activity: MainActivity, tokenStore: SecureTokenStore) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val scope = rememberCoroutineScope()
        var screen by remember { mutableStateOf(if (tokenStore.isBiometricEnabled()) GScreen.Unlock else GScreen.Login) }
        var dashboard by remember { mutableStateOf<JSONObject?>(null) }
        var selectedChildId by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf("") }
        var offerBio by remember { mutableStateOf(false) }
        var unlockPrompted by remember { mutableStateOf(false) }
        var currentPassword by remember { mutableStateOf("") }

        suspend fun loadDashboard() {
            val d = ApiClient.get("/api/guardian/dashboard")
            dashboard = d
            val children = d.optJSONArray("children")
            if (selectedChildId.isBlank() && children != null && children.length() > 0) {
                selectedChildId = children.optJSONObject(0)?.optJSONObject("student")?.optString("id").orEmpty()
            } else if (children != null && selectedChildId.isNotBlank()) {
                var found = false
                for (i in 0 until children.length()) if (children.optJSONObject(i)?.optJSONObject("student")?.optString("id") == selectedChildId) found = true
                if (!found && children.length() > 0) selectedChildId = children.optJSONObject(0)?.optJSONObject("student")?.optString("id").orEmpty()
            }
        }

        fun fail(t: Throwable) {
            error = t.message ?: "خطای نامشخص"
            if (t is ApiException && t.status == 401) {
                ApiClient.token = ""; tokenStore.clear(); dashboard = null; screen = GScreen.Login
            }
        }

        fun completeLogin(data: JSONObject, password: String) {
            val token = data.optString("token")
            if (token.isBlank()) { error = "سرور توکن ورود برنگرداند."; return }
            ApiClient.token = token
            currentPassword = password
            if (tokenStore.isBiometricEnabled()) tokenStore.save(token)
            else if (activity.biometricAvailable()) offerBio = true
            if (data.optBoolean("mustChangePassword", false)) screen = GScreen.ChangePassword
            else {
                screen = GScreen.Home
                scope.launch { runCatching { loadDashboard() }.onFailure(::fail) }
            }
        }

        fun loginNew(nationalId: String, password: String) {
            scope.launch {
                busy = true; error = ""
                try {
                    val d = ApiClient.post("/api/guardian/login", JSONObject().put("guardianNationalId", nationalId.filter(Char::isDigit)).put("password", password), auth = false)
                    completeLogin(d, password)
                } catch (t: Throwable) { fail(t) } finally { busy = false }
            }
        }
        fun loginLegacy(studentNid: String, accessCode: String) {
            scope.launch {
                busy = true; error = ""
                try {
                    val d = ApiClient.post("/api/guardian/login", JSONObject().put("studentNationalId", studentNid.filter(Char::isDigit)).put("accessCode", accessCode), auth = false)
                    completeLogin(d, accessCode)
                } catch (t: Throwable) { fail(t) } finally { busy = false }
            }
        }
        fun biometricUnlock() {
            activity.authenticateBiometric(onSuccess = {
                scope.launch {
                    try {
                        val t = tokenStore.load() ?: throw IllegalStateException("نشست ذخیره‌شده پیدا نشد.")
                        ApiClient.token = t
                        screen = GScreen.Home
                        loadDashboard()
                    } catch (e: Throwable) { tokenStore.clear(); ApiClient.token = ""; screen = GScreen.Login; fail(e) }
                }
            }, onError = { error = it })
        }
        fun logout() {
            scope.launch {
                runCatching { if (ApiClient.token.isNotBlank()) ApiClient.post("/api/guardian/logout") }
                ApiClient.token = ""; tokenStore.clear(); dashboard = null; selectedChildId = ""; screen = GScreen.Login
            }
        }

        DisposableEffect(activity) {
            activity.onRequireUnlock = {
                if (tokenStore.isBiometricEnabled() && screen !in listOf(GScreen.Login, GScreen.ChangePassword, GScreen.Unlock)) {
                    screen = GScreen.Unlock; unlockPrompted = false
                }
            }
            onDispose { activity.onRequireUnlock = null }
        }
        LaunchedEffect(screen) {
            if (screen == GScreen.Unlock && !unlockPrompted) { unlockPrompted = true; biometricUnlock() }
            else if (screen != GScreen.Unlock) unlockPrompted = false
        }

        MaterialTheme(colorScheme = lightColorScheme(primary = Purple, secondary = Color(0xFF8F74F1), background = Surface, surface = Color.White, onPrimary = Color.White, onBackground = Text, onSurface = Text)) {
            AnimatedContent(screen, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "guardian-screen") { target ->
                when (target) {
                    GScreen.Unlock -> UnlockScreen(error, { biometricUnlock() }, { screen = GScreen.Login })
                    GScreen.Login -> LoginScreen(busy, error, ::loginNew, ::loginLegacy)
                    GScreen.ChangePassword -> ChangePasswordScreen(busy, error) { newPassword ->
                        scope.launch {
                            busy = true; error = ""
                            try {
                                val d = ApiClient.post("/api/guardian/change-password", JSONObject().put("currentPassword", currentPassword).put("newPassword", newPassword))
                                d.optString("token").takeIf { it.isNotBlank() }?.let { ApiClient.token = it; if (tokenStore.isBiometricEnabled()) tokenStore.save(it) }
                                screen = GScreen.Home; loadDashboard()
                            } catch (t: Throwable) { fail(t) } finally { busy = false }
                        }
                    }
                    else -> GuardianShell(
                        screen = target, dashboard = dashboard, selectedChildId = selectedChildId,
                        busy = busy, error = error,
                        onSelectChild = { selectedChildId = it; screen = GScreen.Home },
                        onNavigate = { screen = it },
                        onRefresh = { scope.launch { busy = true; error = ""; try { loadDashboard() } catch (t: Throwable) { fail(t) } finally { busy = false } } },
                        onLogout = ::logout,
                        onEnableBiometric = {
                            activity.authenticateBiometric(onSuccess = { tokenStore.enableBiometric(ApiClient.token); offerBio = false }, onError = { error = it })
                        },
                        biometricEnabled = tokenStore.isBiometricEnabled(),
                        onDisableBiometric = { tokenStore.disableBiometric(clearSavedToken = true) },
                        onTokenChanged = { newToken -> ApiClient.token = newToken; if (tokenStore.isBiometricEnabled()) tokenStore.save(newToken) },
                        onDashboardChanged = { dashboard = it }
                    )
                }
            }
            if (offerBio && screen !in listOf(GScreen.Login, GScreen.ChangePassword, GScreen.Unlock)) {
                AlertDialog(
                    onDismissRequest = { offerBio = false },
                    icon = { Icon(Icons.Default.Fingerprint, null, tint = Purple) },
                    title = { Text("فعال‌سازی ورود با اثر انگشت", fontWeight = FontWeight.Bold) },
                    text = { Text("بعد از یک‌بار فعال‌سازی، هر بار برنامه را باز کنید با اثر انگشت وارد می‌شوید.") },
                    confirmButton = { TextButton(onClick = {
                        activity.authenticateBiometric(onSuccess = { tokenStore.enableBiometric(ApiClient.token); offerBio = false }, onError = { error = it })
                    }) { Text("فعال کن") } },
                    dismissButton = { TextButton(onClick = { offerBio = false }) { Text("فعلاً نه") } }
                )
            }
        }
    }
}

@Composable
private fun UnlockScreen(error: String, onUnlock: () -> Unit, onPassword: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Purple, PurpleDark))), contentAlignment = Alignment.Center) {
        Card(Modifier.padding(24.dp).fillMaxWidth(), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FamilyRestroom, null, tint = Purple, modifier = Modifier.size(58.dp))
                Spacer(Modifier.height(12.dp)); Text("مدرسه‌یار اولیا", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("اطلاعات فرزند شما محافظت شده است", color = Muted)
                if (error.isNotBlank()) { Spacer(Modifier.height(10.dp)); Text(error, color = Red) }
                Spacer(Modifier.height(22.dp)); Button(onClick = onUnlock, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Fingerprint, null); Spacer(Modifier.width(8.dp)); Text("باز کردن با اثر انگشت") }
                TextButton(onClick = onPassword) { Text("ورود با رمز عبور") }
            }
        }
    }
}
