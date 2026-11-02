package ir.madreseyar.guardian

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val GPurple = Color(0xFF7654E8)
private val GDark = Color(0xFF2D2840)
private val GMuted = Color(0xFF817A95)
private val GLilac = Color(0xFFF0ECFF)
private val GBlue = Color(0xFF4B8DF8)
private val GGreen = Color(0xFF2F9E70)
private val GOrange = Color(0xFFF0A04B)
private val GRed = Color(0xFFD9535F)
private val GSurface = Color(0xFFF8F7FC)

private fun JSONArray.objects(): List<JSONObject> = buildList { for (i in 0 until length()) optJSONObject(i)?.let(::add) }
private fun JSONObject.arr(name: String): List<JSONObject> = optJSONArray(name)?.objects().orEmpty()
private fun JSONObject.obj(name: String): JSONObject? = optJSONObject(name)
private fun JSONObject.s(name: String, fallback: String = ""): String = optString(name).takeIf { it.isNotBlank() } ?: fallback
private fun numText(v: Any?): String = when (v) { null, JSONObject.NULL -> "—"; is Number -> if (v.toDouble() % 1.0 == 0.0) v.toInt().toString() else String.format("%.1f", v.toDouble()); else -> v.toString() }
private fun childName(st: JSONObject?) = listOf(st?.s("firstName"), st?.s("lastName")).filter { !it.isNullOrBlank() }.joinToString(" ").ifBlank { "دانش‌آموز" }
private fun urlEnc(v: String): String = URLEncoder.encode(v, StandardCharsets.UTF_8.toString())

@Composable
fun LoginScreen(
    busy: Boolean,
    error: String,
    onLogin: (String, String) -> Unit,
    onLegacy: (String, String) -> Unit,
) {
    var legacy by remember { mutableStateOf(false) }
    var nationalId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var studentNationalId by remember { mutableStateOf("") }
    var accessCode by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(GSurface)) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = GLilac, modifier = Modifier.size(86.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.FamilyRestroom, null, tint = GPurple, modifier = Modifier.size(48.dp)) } }
            Spacer(Modifier.height(14.dp)); Text("مدرسه‌یار اولیا", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = GDark)
            Text("همراه هوشمند خانواده در مسیر آموزش", color = GMuted)
            Spacer(Modifier.height(26.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(22.dp)) {
                    Text(if (legacy) "ورود حساب قدیمی" else "ورود والدین", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(14.dp))
                    if (!legacy) {
                        OutlinedTextField(nationalId, { nationalId = it.filter(Char::isDigit).take(10) }, label = { Text("کد ملی ولی") }, leadingIcon = { Icon(Icons.Default.Badge, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(password, { password = it }, label = { Text("رمز عبور") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    } else {
                        OutlinedTextField(studentNationalId, { studentNationalId = it.filter(Char::isDigit).take(10) }, label = { Text("کد ملی دانش‌آموز") }, leadingIcon = { Icon(Icons.Default.School, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(accessCode, { accessCode = it }, label = { Text("کد دسترسی خانواده") }, leadingIcon = { Icon(Icons.Default.Key, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    if (error.isNotBlank()) { Spacer(Modifier.height(10.dp)); Text(error, color = GRed, style = MaterialTheme.typography.bodySmall) }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { if (legacy) onLegacy(studentNationalId, accessCode) else onLogin(nationalId, password) }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else { Icon(Icons.Default.Login, null); Spacer(Modifier.width(8.dp)); Text("ورود به پنل اولیا") }
                    }
                    TextButton(onClick = { legacy = !legacy }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(if (legacy) "بازگشت به ورود جدید" else "حساب قدیمی دارم") }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(busy: Boolean, error: String, onSave: (String) -> Unit) {
    var a by remember { mutableStateOf("") }; var b by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(GSurface), contentAlignment = Alignment.Center) {
        Card(Modifier.padding(24.dp).fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.padding(24.dp)) {
                Icon(Icons.Default.Password, null, tint = GPurple, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(10.dp))
                Text("تغییر رمز اولیه", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("برای امنیت حساب، رمز شخصی جدید تعیین کنید.", color = GMuted)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(a, { a = it }, label = { Text("رمز جدید") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp)); OutlinedTextField(b, { b = it }, label = { Text("تکرار رمز") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                if (error.isNotBlank()) Text(error, color = GRed, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(16.dp)); Button(onClick = { if (a.length >= 8 && a == b) onSave(a) }, enabled = !busy && a.length >= 8 && a == b, modifier = Modifier.fillMaxWidth()) { Text("ذخیره رمز و ادامه") }
            }
        }
    }
}

data class GuardianFeature(val title: String, val subtitle: String, val icon: ImageVector, val screen: GScreen, val color: Color)

private val features = listOf(
    GuardianFeature("مشخصات فرزند", "پرونده و عکس", Icons.Default.AccountCircle, GScreen.Profile, GPurple),
    GuardianFeature("کارنامه و نمرات", "نمرات منتشرشده", Icons.Default.Assessment, GScreen.Grades, GBlue),
    GuardianFeature("گزارش آزمون‌ها", "روند و جزئیات", Icons.Default.Quiz, GScreen.ExamReport, Color(0xFF8A63D2)),
    GuardianFeature("حضور و غیاب", "حاضر، غایب و تأخیر", Icons.Default.HowToReg, GScreen.Attendance, GGreen),
    GuardianFeature("تکالیف", "مهلت و وضعیت تحویل", Icons.Default.Assignment, GScreen.Homework, GOrange),
    GuardianFeature("برنامه هفتگی", "کلاس‌ها و رویدادها", Icons.Default.CalendarMonth, GScreen.Schedule, Color(0xFF2E8B9E)),
    GuardianFeature("ارتباط با مدرسه", "پیام، ملاقات، رضایت", Icons.Default.Forum, GScreen.Messages, Color(0xFFDD6B9E)),
    GuardianFeature("حساب و امنیت", "دستگاه‌ها و رمز", Icons.Default.Security, GScreen.Settings, Color(0xFF687386)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianShell(
    screen: GScreen,
    dashboard: JSONObject?,
    selectedChildId: String,
    busy: Boolean,
    error: String,
    onSelectChild: (String) -> Unit,
    onNavigate: (GScreen) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onEnableBiometric: () -> Unit,
    biometricEnabled: Boolean,
    onDisableBiometric: () -> Unit,
    onTokenChanged: (String) -> Unit,
    onDashboardChanged: (JSONObject) -> Unit,
) {
    val children = dashboard?.optJSONArray("children")?.objects().orEmpty()
    val child = children.firstOrNull { it.obj("student")?.s("id") == selectedChildId } ?: children.firstOrNull()
    Scaffold(
        containerColor = GSurface,
        topBar = {
            TopAppBar(
                title = { Column { Text(screenTitle(screen), fontWeight = FontWeight.Black); Text(dashboard?.s("academicYear", "سال تحصیلی") ?: "سال تحصیلی", style = MaterialTheme.typography.labelSmall, color = GMuted) } },
                navigationIcon = { if (screen != GScreen.Home) IconButton(onClick = { onNavigate(GScreen.Home) }) { Icon(Icons.Default.ArrowForward, "بازگشت") } },
                actions = { IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "بروزرسانی") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(
                    Triple(GScreen.Home, "خانه", Icons.Default.Home),
                    Triple(GScreen.Grades, "نمرات", Icons.Default.Assessment),
                    Triple(GScreen.Attendance, "حضور", Icons.Default.HowToReg),
                    Triple(GScreen.Messages, "ارتباط", Icons.Default.Chat),
                ).forEach { (s, label, icon) -> NavigationBarItem(selected = screen == s, onClick = { onNavigate(s) }, icon = { Icon(icon, null) }, label = { Text(label) }) }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (children.size > 1) ChildSelector(children, child?.obj("student")?.s("id").orEmpty(), onSelectChild)
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = GPurple)
            if (error.isNotBlank()) Surface(color = Color(0xFFFFECEE), modifier = Modifier.fillMaxWidth()) { Text(error, color = GRed, modifier = Modifier.padding(10.dp)) }
            when (screen) {
                GScreen.Home -> HomeScreen(dashboard, child, onNavigate)
                GScreen.Profile -> ProfileScreen(child)
                GScreen.Grades -> GradesScreen(child, onNavigate)
                GScreen.ExamReport -> ExamReportScreen(child)
                GScreen.Attendance -> AttendanceScreen(child)
                GScreen.Homework -> HomeworkScreen(child)
                GScreen.Schedule -> ScheduleScreen(child)
                GScreen.Messages -> MessagesScreen(dashboard, child, onDashboardChanged)
                GScreen.Settings -> SettingsScreen(dashboard, biometricEnabled, onEnableBiometric, onDisableBiometric, onTokenChanged, onLogout)
                else -> Unit
            }
        }
    }
}

private fun screenTitle(s: GScreen) = when (s) {
    GScreen.Home -> "مدرسه‌یار اولیا"; GScreen.Profile -> "مشخصات فرزند"; GScreen.Grades -> "کارنامه و نمرات"; GScreen.ExamReport -> "گزارش آزمون‌ها"; GScreen.Attendance -> "حضور و غیاب"; GScreen.Homework -> "تکالیف"; GScreen.Schedule -> "برنامه هفتگی"; GScreen.Messages -> "ارتباط با مدرسه"; GScreen.Settings -> "حساب و امنیت"; else -> "مدرسه‌یار اولیا"
}

@Composable
private fun ChildSelector(children: List<JSONObject>, selected: String, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val current = children.firstOrNull { it.obj("student")?.s("id") == selected } ?: children.firstOrNull()
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.ChildCare, null, tint = GPurple); Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(childName(current?.obj("student")), fontWeight = FontWeight.Bold); Text(current?.obj("student")?.s("className", "کلاس ثبت نشده") ?: "", style = MaterialTheme.typography.labelSmall, color = GMuted) }; Icon(Icons.Default.ExpandMore, null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.fillMaxWidth(.9f)) {
            children.forEach { ch -> val st = ch.obj("student"); DropdownMenuItem(text = { Column { Text(childName(st), fontWeight = FontWeight.Bold); Text(st?.s("className", "—") ?: "—", style = MaterialTheme.typography.labelSmall) } }, onClick = { onSelect(st?.s("id").orEmpty()); open = false }, leadingIcon = { Icon(Icons.Default.Person, null) }) }
        }
    }
}

@Composable
private fun HomeScreen(dashboard: JSONObject?, child: JSONObject?, onNavigate: (GScreen) -> Unit) {
    if (dashboard == null || child == null) { EmptyState("در حال دریافت اطلاعات خانواده…"); return }
    val st = child.obj("student")
    var homePhoto by remember(st?.s("id")) { mutableStateOf<String?>(null) }
    LaunchedEffect(st?.s("id")) {
        val id = st?.s("id").orEmpty()
        if (id.isNotBlank()) runCatching { ApiClient.get("/api/guardian/student-dossier?studentId=${urlEnc(id)}").obj("dossier")?.s("photoDataUrl") }.onSuccess { homePhoto = it }
    }
    val attToday = child.obj("attendanceToday")?.obj("summary") ?: JSONObject()
    val homework = child.arr("homework")
    val submissions = child.arr("homeworkSubmissions")
    val pending = homework.count { hw -> submissions.none { it.s("homeworkId") == hw.s("id") } }
    val termReports = child.obj("termReports") ?: JSONObject()
    val published = listOf("semester2", "semester1", "annual").mapNotNull { k -> termReports.optJSONObject(k) }.firstOrNull { it.s("reportCardStatus") == "published" }
    val avg = published?.opt("average")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = GPurple), shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    StudentAvatar(homePhoto, childName(st), 58)
                    Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(childName(st), color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge); Text(st?.s("className", "کلاس ثبت نشده") ?: "", color = Color.White.copy(.8f)); Text("کد دانش‌آموزی ${st?.s("studentCode", "—") ?: "—"}", color = Color.White.copy(.7f), style = MaterialTheme.typography.labelSmall) }
                    Icon(Icons.Default.School, null, tint = Color.White.copy(.9f), modifier = Modifier.size(42.dp))
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("حاضر امروز", attToday.optInt("present").toString(), GGreen, Modifier.weight(1f))
                MiniStat("غیبت", attToday.optInt("absent").toString(), GRed, Modifier.weight(1f))
                MiniStat("تکلیف مانده", pending.toString(), GOrange, Modifier.weight(1f))
                MiniStat("معدل", if (avg == null || avg == JSONObject.NULL) "—" else numText(avg), GPurple, Modifier.weight(1f))
            }
        }
        item { Text("دسترسی سریع", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium) }
        item {
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(430.dp), userScrollEnabled = false, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                gridItems(features) { f -> FeatureCard(f) { onNavigate(f.screen) } }
            }
        }
        item { ActionCenter(child, dashboard, onNavigate) }
    }
}

@Composable private fun MiniStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) { Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium); Text(label, style = MaterialTheme.typography.labelSmall, color = GMuted) } } }
@Composable private fun FeatureCard(f: GuardianFeature, onClick: () -> Unit) { Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Surface(shape = RoundedCornerShape(14.dp), color = f.color.copy(.12f)) { Icon(f.icon, null, tint = f.color, modifier = Modifier.padding(10.dp).size(26.dp)) }; Spacer(Modifier.height(10.dp)); Text(f.title, fontWeight = FontWeight.Bold); Text(f.subtitle, style = MaterialTheme.typography.labelSmall, color = GMuted) } } }

@Composable
private fun ActionCenter(child: JSONObject, dashboard: JSONObject, onNavigate: (GScreen) -> Unit) {
    val att = child.obj("attendanceToday")?.obj("summary") ?: JSONObject(); val homework = child.arr("homework"); val notices = dashboard.arr("guardianNotifications")
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp)) {
            Text("مرکز اقدام خانواده", fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            ActionRow(Icons.Default.HowToReg, "حضور امروز", if (att.optInt("absent") > 0) "${att.optInt("absent")} غیبت ثبت شده" else "وضعیت عادی", GGreen) { onNavigate(GScreen.Attendance) }
            ActionRow(Icons.Default.Assignment, "تکالیف اخیر", "${homework.size} تکلیف در فهرست", GOrange) { onNavigate(GScreen.Homework) }
            ActionRow(Icons.Default.Notifications, "پیام‌های مدرسه", "${notices.count { !it.optBoolean("read") }} پیام خوانده‌نشده", GPurple) { onNavigate(GScreen.Messages) }
        }
    }
}
@Composable private fun ActionRow(icon: ImageVector, title: String, sub: String, color: Color, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = CircleShape, color = color.copy(.12f)) { Icon(icon, null, tint = color, modifier = Modifier.padding(9.dp)) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(sub, color = GMuted, style = MaterialTheme.typography.labelSmall) }; Icon(Icons.Default.ChevronLeft, null, tint = GMuted) } }

@Composable
private fun ProfileScreen(child: JSONObject?) {
    val scope = rememberCoroutineScope(); var dossier by remember(child) { mutableStateOf<JSONObject?>(null) }; var error by remember { mutableStateOf("") }; var loading by remember { mutableStateOf(false) }
    val st = child?.obj("student")
    LaunchedEffect(st?.s("id")) { val id = st?.s("id").orEmpty(); if (id.isNotBlank()) { loading = true; runCatching { ApiClient.get("/api/guardian/student-dossier?studentId=${urlEnc(id)}").obj("dossier") }.onSuccess { dossier = it }.onFailure { error = it.message.orEmpty() }; loading = false } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { StudentAvatar(dossier?.s("photoDataUrl"), childName(st), 90); Spacer(Modifier.height(10.dp)); Text(childName(st), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge); Text(st?.s("className", "—") ?: "—", color = GMuted) } }
        }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (error.isNotBlank()) item { ErrorCard(error) }
        item { InfoGrid(listOf("کد دانش‌آموزی" to (st?.s("studentCode", "—") ?: "—"), "کد ملی" to (st?.s("nationalId", "—") ?: "—"), "کلاس" to (st?.s("className", "—") ?: "—"), "سال تحصیلی" to (dossier?.obj("school")?.s("academicYear", "—") ?: "—"))) }
        dossier?.let { d ->
            item { SectionTitle("اطلاعات خانواده") }
            items(d.arr("guardians")) { g -> SimpleCard(g.s("name", "ولی دانش‌آموز"), "${g.s("relation", "ولی")} · ${g.s("mobile", "شماره ثبت نشده")}", Icons.Default.FamilyRestroom, GPurple) }
            item { SectionTitle("خلاصه آموزشی") }
            item { val a = d.obj("analysis") ?: JSONObject(); InfoGrid(listOf("میانگین کلی" to numText(a.opt("overallAveragePercent")) + if (a.has("overallAveragePercent")) "٪" else "", "تعداد آزمون‌ها" to d.arr("exams").size.toString(), "غیبت" to (d.obj("attendance")?.optInt("absent") ?: 0).toString(), "تأخیر" to (d.obj("attendance")?.optInt("late") ?: 0).toString())) }
        }
    }
}

@Composable
private fun GradesScreen(child: JSONObject?, onNavigate: (GScreen) -> Unit) {
    val termReports = child?.obj("termReports") ?: JSONObject(); val st = child?.obj("student")
    val scope = rememberCoroutineScope(); var analysis by remember { mutableStateOf<JSONObject?>(null) }; var loading by remember { mutableStateOf(false) }; var period by remember { mutableStateOf("month") }; var subject by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }
    val subjects = child?.optJSONArray("analysisSubjects")?.let { a -> List(a.length()) { a.optString(it) } }.orEmpty()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("کارنامه‌های رسمی") }
        listOf("semester1" to "نیم‌سال اول", "semester2" to "نیم‌سال دوم", "annual" to "سالانه").forEach { (k, label) -> item { TermCard(label, termReports.optJSONObject(k)) } }
        item { SectionTitle("تحلیل وضعیت تحصیلی") }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    ChoiceDropdown("بازه", period, listOf("week" to "هفته اخیر", "month" to "ماه اخیر", "term" to "نیم‌سال", "year" to "سال تحصیلی")) { period = it }
                    Spacer(Modifier.height(8.dp)); ChoiceDropdown("درس", subject, listOf("" to "همه درس‌ها") + subjects.map { it to it }) { subject = it }
                    Spacer(Modifier.height(12.dp)); Button(onClick = { val id = st?.s("id").orEmpty(); if (id.isNotBlank()) scope.launch { loading = true; error = ""; try { analysis = ApiClient.get("/api/guardian/student-analysis?studentId=${urlEnc(id)}&period=${urlEnc(period)}&subject=${urlEnc(subject)}").obj("analysis") } catch (t: Throwable) { error = t.message.orEmpty() } finally { loading = false } } }, modifier = Modifier.fillMaxWidth()) { Text("نمایش تحلیل") }
                }
            }
        }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (error.isNotBlank()) item { ErrorCard(error) }
        analysis?.let { a ->
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniStat("میانگین", numText(a.obj("current")?.opt("averagePercent")) + "٪", GPurple, Modifier.weight(1f)); MiniStat("آزمون", (a.obj("current")?.optInt("examCount") ?: 0).toString(), GBlue, Modifier.weight(1f)); MiniStat("غیبت", (a.obj("attendance")?.optInt("absent") ?: 0).toString(), GRed, Modifier.weight(1f)) } }
            item { Text("درس‌ها", fontWeight = FontWeight.Bold) }
            items(a.arr("subjects")) { s -> SimpleCard(s.s("subject", "درس"), "${numText(s.opt("percent"))}٪ · ${s.optInt("count")} نتیجه", Icons.Default.MenuBook, GBlue) }
            item { Text("نظر معلم", fontWeight = FontWeight.Bold) }
            items(a.arr("teacherNotes")) { n -> SimpleCard(n.s("teacherName", "معلم"), n.s("teacherComment", ""), Icons.Default.RateReview, GPurple) }
        }
        item { OutlinedButton(onClick = { onNavigate(GScreen.ExamReport) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Quiz, null); Spacer(Modifier.width(8.dp)); Text("گزارش جامع آزمون‌های فرزند") } }
    }
}

@Composable private fun TermCard(label: String, r: JSONObject?) { val published = r?.s("reportCardStatus") == "published"; Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (published) Color.White else Color(0xFFF3F1F7))) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (published) Icons.Default.Verified else Icons.Default.HourglassEmpty, null, tint = if (published) GGreen else GMuted); Spacer(Modifier.width(8.dp)); Text(label, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); AssistChip(onClick = {}, label = { Text(if (published) "منتشر شده" else "در انتظار انتشار") }) }; if (published) { Spacer(Modifier.height(8.dp)); Row { Text("معدل: ", color = GMuted); Text(numText(r?.opt("average")), fontWeight = FontWeight.Black, color = GPurple); Spacer(Modifier.width(18.dp)); Text("انضباط: ${numText(r?.opt("disciplineScore"))}", color = GMuted) }; val subs = r?.arr("completeSubjects").takeUnless { it.isNullOrEmpty() } ?: r?.arr("subjects").orEmpty(); subs.take(8).forEach { s -> HorizontalDivider(Modifier.padding(vertical = 6.dp)); Row { Text(s.s("subject", "درس"), Modifier.weight(1f)); Text("${numText(s.opt("finalScore"))}", fontWeight = FontWeight.Bold) } } } } } }

@Composable
private fun ExamReportScreen(child: JSONObject?) {
    val st = child?.obj("student"); val scope = rememberCoroutineScope(); var data by remember { mutableStateOf<JSONObject?>(null) }; var loading by remember { mutableStateOf(false) }; var error by remember { mutableStateOf("") }
    LaunchedEffect(st?.s("id")) { val id = st?.s("id").orEmpty(); if (id.isNotBlank()) { loading = true; try { data = ApiClient.get("/api/guardian/exam-report/t06?studentId=${urlEnc(id)}") } catch (t: Throwable) { error = t.message.orEmpty() } finally { loading = false } } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }; if (error.isNotBlank()) item { ErrorCard(error) }
        data?.let { d ->
            item { val o = d.obj("overall") ?: JSONObject(); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniStat("میانگین /۲۰", numText(o.opt("average20")), GPurple, Modifier.weight(1f)); MiniStat("نتیجه", o.optInt("count").toString(), GBlue, Modifier.weight(1f)); MiniStat("بهترین", numText(o.opt("best20")), GGreen, Modifier.weight(1f)) } }
            item { SectionTitle("آزمون‌های منتشرشده") }
            items(d.arr("exams")) { e -> Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = CircleShape, color = GLilac) { Icon(Icons.Default.Quiz, null, tint = GPurple, modifier = Modifier.padding(10.dp)) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(e.s("title", "آزمون"), fontWeight = FontWeight.Bold); Text("${e.s("subject", "—")} · ${e.s("chapter", "فصل نامشخص")}", color = GMuted, style = MaterialTheme.typography.labelSmall) }; Column(horizontalAlignment = Alignment.End) { Text("${numText(e.opt("score"))} / ${numText(e.opt("total"))}", fontWeight = FontWeight.Black); Text("${numText(e.opt("percent"))}٪", color = GPurple) } } } }
            item { SectionTitle("پیشنهادهای مطالعه") }
            val recs = d.optJSONArray("recommendations")?.let { a -> List(a.length()) { a.optString(it) } }.orEmpty(); items(recs) { SimpleCard("پیشنهاد", it, Icons.Default.Lightbulb, GOrange) }
        }
    }
}

@Composable
private fun AttendanceScreen(child: JSONObject?) {
    val st = child?.obj("student"); val today = child?.obj("attendanceToday"); val scope = rememberCoroutineScope(); var day by remember { mutableStateOf<JSONObject?>(today?.obj("attendance") ?: today) }; var date by remember { mutableStateOf("") }; var loading by remember { mutableStateOf(false) }; var error by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Text("مشاهده حضور یک روز", fontWeight = FontWeight.Bold); OutlinedTextField(date, { date = it }, label = { Text("تاریخ YYYY-MM-DD (خالی = امروز)") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); Button(onClick = { val id = st?.s("id").orEmpty(); scope.launch { loading = true; error = ""; try { day = ApiClient.get("/api/guardian/attendance-day?studentId=${urlEnc(id)}&date=${urlEnc(date)}").obj("attendance") } catch (t: Throwable) { error = t.message.orEmpty() } finally { loading = false } } }, modifier = Modifier.fillMaxWidth()) { Text("دریافت") } } } }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }; if (error.isNotBlank()) item { ErrorCard(error) }
        day?.let { d ->
            item { val s = d.obj("summary") ?: JSONObject(); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { MiniStat("حاضر", s.optInt("present").toString(), GGreen, Modifier.weight(1f)); MiniStat("در انتظار", s.optInt("pending").toString(), GOrange, Modifier.weight(1f)); MiniStat("موجه", s.optInt("excused").toString(), GBlue, Modifier.weight(1f)); MiniStat("غیرموجه", s.optInt("unexcused").toString(), GRed, Modifier.weight(1f)) } }
            item { SectionTitle("جزئیات کلاس‌های روز") }
            val rows = d.arr("rows").ifEmpty { d.arr("periods") }.ifEmpty { d.arr("entries") }; items(rows) { r -> SimpleCard(r.s("subject", "کلاس"), listOf("زنگ ${r.optInt("periodNo")}", r.s("statusLabel", r.s("status", "")), r.s("reason", "")).filter { it.isNotBlank() }.joinToString(" · "), Icons.Default.Schedule, when (r.s("status")) { "present" -> GGreen; "unexcused" -> GRed; else -> GOrange }) }
        }
    }
}

@Composable
private fun HomeworkScreen(child: JSONObject?) {
    val homework = child?.arr("homework").orEmpty(); val subs = child?.arr("homeworkSubmissions").orEmpty()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniStat("کل تکالیف", homework.size.toString(), GPurple, Modifier.weight(1f)); MiniStat("تحویل شده", homework.count { hw -> subs.any { it.s("homeworkId") == hw.s("id") } }.toString(), GGreen, Modifier.weight(1f)); MiniStat("باقی‌مانده", homework.count { hw -> subs.none { it.s("homeworkId") == hw.s("id") } }.toString(), GOrange, Modifier.weight(1f)) } }
        items(homework) { hw -> val sub = subs.firstOrNull { it.s("homeworkId") == hw.s("id") }; Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Surface(shape = CircleShape, color = (if (sub != null) GGreen else GOrange).copy(.12f)) { Icon(if (sub != null) Icons.Default.CheckCircle else Icons.Default.Assignment, null, tint = if (sub != null) GGreen else GOrange, modifier = Modifier.padding(10.dp)) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(hw.s("title", "تکلیف کلاسی"), fontWeight = FontWeight.Bold); Text(hw.s("subject", "درس"), color = GMuted) }; AssistChip(onClick = {}, label = { Text(if (sub != null) "تحویل شده" else "در انتظار") }) }; Spacer(Modifier.height(8.dp)); Text("مهلت: ${hw.s("dueAt", "مشخص نشده")}", style = MaterialTheme.typography.bodySmall); hw.s("description").takeIf { it.isNotBlank() }?.let { Text(it, color = GMuted, style = MaterialTheme.typography.bodySmall) }; if (sub != null) { HorizontalDivider(Modifier.padding(vertical = 8.dp)); Text("وضعیت پاسخ: ${sub.s("status", "ثبت شده")}", color = GGreen); if (sub.has("score")) Text("نمره: ${numText(sub.opt("score"))}", fontWeight = FontWeight.Bold) } } } }
    }
}

@Composable
private fun ScheduleScreen(child: JSONObject?) {
    val rows = child?.arr("weeklySchedule").orEmpty(); val events = child?.arr("calendarEvents").orEmpty(); val sessions = child?.arr("onlineClassSessions").orEmpty(); val week = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        week.forEach { day -> val dayRows = rows.filter { it.s("day") == day }.sortedBy { it.optInt("periodNo") }; if (dayRows.isNotEmpty()) { item { SectionTitle(day) }; items(dayRows) { r -> SimpleCard(r.s("subject", "درس"), "زنگ ${r.optInt("periodNo")} · ${r.s("startTime", "—")} تا ${r.s("endTime", "—")} · ${r.s("teacherName", "")}", Icons.Default.MenuBook, GPurple) } } }
        item { SectionTitle("کلاس‌های آنلاین") }; if (sessions.isEmpty()) item { EmptyState("جلسه آنلاین فعالی ثبت نشده است.") } else items(sessions) { s -> SimpleCard(s.s("subject", "کلاس آنلاین"), "${s.s("date", "")} · زنگ ${s.optInt("periodNo")} · ${s.s("status", "")}", Icons.Default.VideoCall, GBlue) }
        item { SectionTitle("رویدادهای آموزشی") }; if (events.isEmpty()) item { EmptyState("رویدادی ثبت نشده است.") } else items(events.take(20)) { e -> SimpleCard(e.s("title", "رویداد"), e.s("startDate", e.s("date", "")), Icons.Default.Event, GOrange) }
    }
}

@Composable
private fun MessagesScreen(dashboard: JSONObject?, child: JSONObject?, onDashboardChanged: (JSONObject) -> Unit) {
    if (dashboard == null || child == null) { EmptyState("اطلاعات ارتباط با مدرسه در دسترس نیست."); return }
    val scope = rememberCoroutineScope(); var busy by remember { mutableStateOf(false) }; var error by remember { mutableStateOf("") }; var meetingText by remember { mutableStateOf("") }; var contactTitle by remember { mutableStateOf("") }; var contactText by remember { mutableStateOf("") }; var category by remember { mutableStateOf("academic") }
    val sid = child.obj("student")?.s("id").orEmpty(); val notices = dashboard.arr("guardianNotifications"); val reqs = dashboard.arr("requests").filter { it.s("studentId") == sid }; val invites = reqs.filter { it.s("kind", it.s("type")) == "meeting_invitation" }; val outgoing = reqs.filter { it.s("kind", it.s("type")) != "meeting_invitation" }
    val consents = dashboard.arr("consentForms").mapNotNull { c -> c.arr("childResponses").firstOrNull { it.s("studentId") == sid }?.let { c to it } }
    suspend fun reload() { onDashboardChanged(ApiClient.get("/api/guardian/dashboard")) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }; if (error.isNotBlank()) item { ErrorCard(error) }
        item { SectionTitle("پیام‌های مستقیم مدرسه") }
        if (notices.isEmpty()) item { EmptyState("پیام مستقیمی ثبت نشده است.") } else items(notices) { n -> SimpleCard(n.s("title", "پیام مدرسه") + if (!n.optBoolean("read")) " · جدید" else "", n.s("body", n.s("message", "")), Icons.Default.Notifications, if (!n.optBoolean("read")) GPurple else GMuted) }
        if (notices.any { !it.optBoolean("read") }) item { OutlinedButton(onClick = { scope.launch { busy = true; try { ApiClient.post("/api/guardian/notifications/read"); reload() } catch (t: Throwable) { error = t.message.orEmpty() } finally { busy = false } } }, modifier = Modifier.fillMaxWidth()) { Text("علامت‌گذاری همه پیام‌ها به عنوان خوانده‌شده") } }
        item { SectionTitle("دعوت‌های ملاقات") }
        if (invites.isEmpty()) item { EmptyState("دعوت ملاقاتی ثبت نشده است.") } else items(invites) { r -> Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Text("ملاقات با ${r.s("teacherName", "معلم")}", fontWeight = FontWeight.Bold); Text("${r.s("meetingDate", "—")} · ساعت ${r.s("meetingTime", "—")} · ${r.s("location", "مدرسه")}", color = GMuted); if (r.s("status") in listOf("pending", "pending_guardian")) { Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { SmallAction("تأیید", GGreen) { respondMeeting(scope, r.s("id"), sid, "accepted", "", { scope.launch { reload() } }, { error = it }) }; SmallAction("نمی‌توانم", GRed) { respondMeeting(scope, r.s("id"), sid, "unavailable", "", { scope.launch { reload() } }, { error = it }) } }; Spacer(Modifier.height(6.dp)); SmallAction("درخواست زمان دیگر", GPurple) { respondMeeting(scope, r.s("id"), sid, "request_new_time", "زمان دیگری لطفاً هماهنگ شود.", { scope.launch { reload() } }, { error = it }) } } else AssistChip(onClick = {}, label = { Text(r.s("status")) }) } } }
        item { SectionTitle("درخواست ملاقات") }
        item { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { OutlinedTextField(meetingText, { meetingText = it.take(1000) }, label = { Text("موضوع یا دلیل ملاقات") }, modifier = Modifier.fillMaxWidth(), minLines = 3); Spacer(Modifier.height(8.dp)); Button(onClick = { scope.launch { busy = true; error = ""; try { ApiClient.post("/api/guardian/meeting-request", JSONObject().put("studentId", sid).put("message", meetingText)); meetingText = ""; reload() } catch (t: Throwable) { error = t.message.orEmpty() } finally { busy = false } } }, enabled = meetingText.trim().length >= 3, modifier = Modifier.fillMaxWidth()) { Text("ثبت درخواست ملاقات") } } } }
        if (outgoing.isNotEmpty()) { item { Text("پیگیری درخواست‌ها", fontWeight = FontWeight.Bold) }; items(outgoing) { r -> SimpleCard(r.s("title", "درخواست"), "${r.s("message", "")} · وضعیت: ${r.s("status", "open")}", Icons.Default.TrackChanges, GBlue) } }
        item { SectionTitle("درخواست غیرحضوری") }
        item { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { ChoiceDropdown("نوع درخواست", category, listOf("academic" to "آموزشی", "administrative" to "اداری", "welfare" to "رفاهی / انضباطی", "general" to "سایر")) { category = it }; Spacer(Modifier.height(8.dp)); OutlinedTextField(contactTitle, { contactTitle = it.take(120) }, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(contactText, { contactText = it.take(1200) }, label = { Text("متن درخواست") }, minLines = 3, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); Button(onClick = { scope.launch { busy = true; error = ""; try { ApiClient.post("/api/guardian/contact-request", JSONObject().put("studentId", sid).put("title", contactTitle).put("message", contactText).put("category", category)); contactTitle = ""; contactText = ""; reload() } catch (t: Throwable) { error = t.message.orEmpty() } finally { busy = false } } }, enabled = contactTitle.trim().length >= 3 && contactText.trim().length >= 3, modifier = Modifier.fillMaxWidth()) { Text("ارسال درخواست به مدرسه") } } } }
        item { SectionTitle("اعلان‌های فرزند") }
        val childNotices = child.arr("notifications")
        if (childNotices.isEmpty()) item { EmptyState("اعلان دیگری برای این فرزند ثبت نشده است.") } else items(childNotices.take(20)) { n -> SimpleCard(n.s("title", "اعلان"), n.s("message", n.s("body", n.s("text", ""))), Icons.Default.Campaign, GBlue) }
        item { SectionTitle("رضایت‌نامه‌ها") }
        if (consents.isEmpty()) item { EmptyState("رضایت‌نامه‌ای برای پاسخ وجود ندارد.") } else items(consents) { (c, cr) -> val response = cr.obj("response"); Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Text(c.s("title", "رضایت‌نامه"), fontWeight = FontWeight.Bold); Text(c.s("description", c.s("body", "")), color = GMuted); if (response != null) AssistChip(onClick = {}, label = { Text(if (response.s("decision") == "approved") "تأیید شده" else "عدم موافقت ثبت شده") }) else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { scope.launch { runCatching { ApiClient.post("/api/guardian/consents/respond", JSONObject().put("studentId", sid).put("consentId", c.s("id")).put("decision", "approved")); reload() }.onFailure { error = it.message.orEmpty() } } }, colors = ButtonDefaults.buttonColors(containerColor = GGreen)) { Text("موافقم") }; OutlinedButton(onClick = { scope.launch { runCatching { ApiClient.post("/api/guardian/consents/respond", JSONObject().put("studentId", sid).put("consentId", c.s("id")).put("decision", "declined")); reload() }.onFailure { error = it.message.orEmpty() } } }) { Text("موافق نیستم") } } } } }
    }
}

private fun respondMeeting(scope: kotlinx.coroutines.CoroutineScope, requestId: String, studentId: String, decision: String, note: String, ok: () -> Unit, fail: (String) -> Unit) { scope.launch { try { ApiClient.post("/api/guardian/meeting-invitations/respond", JSONObject().put("requestId", requestId).put("studentId", studentId).put("decision", decision).put("note", note)); ok() } catch (t: Throwable) { fail(t.message.orEmpty()) } } }

@Composable private fun SmallAction(label: String, color: Color, onClick: () -> Unit) { Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)) { Text(label, style = MaterialTheme.typography.labelSmall) } }

@Composable
private fun SettingsScreen(dashboard: JSONObject?, biometricEnabled: Boolean, onEnableBiometric: () -> Unit, onDisableBiometric: () -> Unit, onTokenChanged: (String) -> Unit, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope(); var sessions by remember { mutableStateOf<List<JSONObject>>(emptyList()) }; var error by remember { mutableStateOf("") }; var loading by remember { mutableStateOf(false) }
    var currentPass by remember { mutableStateOf("") }; var newPass by remember { mutableStateOf("") }; var repeatPass by remember { mutableStateOf("") }; var passwordSaved by remember { mutableStateOf("") }
    fun loadSessions() { scope.launch { loading = true; error = ""; try { sessions = ApiClient.get("/api/guardian/sessions").arr("sessions") } catch (t: Throwable) { error = t.message.orEmpty() } finally { loading = false } } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { InfoGrid(listOf("نام ولی" to (dashboard?.obj("guardian")?.s("name", dashboard?.obj("guardian")?.s("guardianName", "ولی دانش‌آموز") ?: "") ?: "ولی دانش‌آموز"), "سال تحصیلی" to (dashboard?.s("academicYear", "—") ?: "—"), "فرزندان متصل" to (dashboard?.optJSONArray("children")?.length() ?: 0).toString(), "نوع حساب" to if (dashboard?.optBoolean("legacy") == true) "قدیمی" else "امن جدید")) }
        item { SectionTitle("تغییر رمز عبور") }
        item { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) {
            OutlinedTextField(currentPass, { currentPass = it }, label = { Text("رمز فعلی") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
            OutlinedTextField(newPass, { newPass = it }, label = { Text("رمز جدید") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
            OutlinedTextField(repeatPass, { repeatPass = it }, label = { Text("تکرار رمز جدید") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth());
            if (passwordSaved.isNotBlank()) Text(passwordSaved, color = GGreen, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(8.dp)); Button(onClick = { scope.launch { loading = true; error = ""; passwordSaved = ""; try { val d = ApiClient.post("/api/guardian/change-password", JSONObject().put("currentPassword", currentPass).put("newPassword", newPass)); d.optString("token").takeIf { it.isNotBlank() }?.let { onTokenChanged(it) }; currentPass = ""; newPass = ""; repeatPass = ""; passwordSaved = "رمز عبور با موفقیت تغییر کرد." } catch (t: Throwable) { error = t.message.orEmpty() } finally { loading = false } } }, enabled = currentPass.isNotBlank() && newPass.length >= 8 && newPass == repeatPass, modifier = Modifier.fillMaxWidth()) { Text("تغییر رمز") }
        } } }
        item { SectionTitle("ورود امن") }
        item { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Fingerprint, null, tint = GPurple, modifier = Modifier.size(36.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("قفل اثر انگشت", fontWeight = FontWeight.Bold); Text(if (biometricEnabled) "فعال است؛ هر بار باز شدن برنامه تأیید می‌شود." else "برای ورود سریع و امن فعال کنید.", color = GMuted, style = MaterialTheme.typography.bodySmall) }; Switch(checked = biometricEnabled, onCheckedChange = { if (it) onEnableBiometric() else onDisableBiometric() }) } } }
        item { SectionTitle("دستگاه‌ها و نشست‌های فعال") }
        item { Button(onClick = { loadSessions() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Devices, null); Spacer(Modifier.width(8.dp)); Text("نمایش / به‌روزرسانی دستگاه‌ها") } }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }; if (error.isNotBlank()) item { ErrorCard(error) }
        items(sessions) { s -> Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(14.dp)) { Row { Icon(Icons.Default.PhoneAndroid, null, tint = GPurple); Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(s.s("deviceLabel", "دستگاه"), fontWeight = FontWeight.Bold); Text((if (s.optBoolean("current")) "همین دستگاه · " else "") + s.s("ip", "IP نامشخص"), color = GMuted, style = MaterialTheme.typography.labelSmall) }; if (s.optBoolean("current")) AssistChip(onClick = {}, label = { Text("فعلی") }) }; TextButton(onClick = { scope.launch { try { val r = ApiClient.post("/api/guardian/sessions/revoke", JSONObject().put("sessionId", s.s("id"))); if (r.optBoolean("requiresLogin")) onLogout() else loadSessions() } catch (t: Throwable) { error = t.message.orEmpty() } } }) { Text("بستن نشست", color = GRed) } } } }
        item { OutlinedButton(onClick = { scope.launch { runCatching { ApiClient.post("/api/guardian/sessions/revoke-others") }.onSuccess { loadSessions() }.onFailure { error = it.message.orEmpty() } } }, modifier = Modifier.fillMaxWidth()) { Text("خروج از سایر دستگاه‌ها") } }
        item { Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = GRed), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Logout, null); Spacer(Modifier.width(8.dp)); Text("خروج از حساب") } }
    }
}

@Composable
private fun ChoiceDropdown(label: String, value: String, options: List<Pair<String, String>>, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }; val shown = options.firstOrNull { it.first == value }?.second ?: options.firstOrNull()?.second.orEmpty()
    Box(Modifier.fillMaxWidth()) { OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) { Text(label, style = MaterialTheme.typography.labelSmall, color = GMuted); Text(shown, fontWeight = FontWeight.Bold) }; Icon(Icons.Default.ExpandMore, null) }; DropdownMenu(expanded = open, onDismissRequest = { open = false }) { options.forEach { (v, t) -> DropdownMenuItem(text = { Text(t) }, onClick = { onChange(v); open = false }) } } }
}

@Composable private fun SectionTitle(text: String) { Text(text, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = GDark, modifier = Modifier.padding(top = 4.dp)) }
@Composable private fun ErrorCard(text: String) { Surface(color = Color(0xFFFFECEE), shape = RoundedCornerShape(14.dp)) { Text(text, color = GRed, modifier = Modifier.padding(12.dp)) } }
@Composable private fun EmptyState(text: String) { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text(text, color = GMuted) } }
@Composable private fun SimpleCard(title: String, subtitle: String, icon: ImageVector, color: Color) { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = CircleShape, color = color.copy(.12f)) { Icon(icon, null, tint = color, modifier = Modifier.padding(9.dp)) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); if (subtitle.isNotBlank()) Text(subtitle, color = GMuted, style = MaterialTheme.typography.bodySmall) } } } }
@Composable
private fun InfoGrid(values: List<Pair<String, String>>) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp)) {
            values.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { (k, v) ->
                        Column(Modifier.weight(1f).padding(8.dp)) {
                            Text(k, color = GMuted, style = MaterialTheme.typography.labelSmall)
                            Text(v.ifBlank { "—" }, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StudentAvatar(dataUrl: String?, name: String, size: Int) {
    val bitmap = remember(dataUrl) { dataUrl?.takeIf { it.startsWith("data:image/") }?.substringAfter("base64,")?.let { runCatching { BitmapFactory.decodeByteArray(Base64.decode(it, Base64.DEFAULT), 0, Base64.decode(it, Base64.DEFAULT).size) }.getOrNull() } }
    if (bitmap != null) Image(bitmap.asImageBitmap(), contentDescription = name, modifier = Modifier.size(size.dp).clip(CircleShape)) else Surface(shape = CircleShape, color = GLilac, modifier = Modifier.size(size.dp)) { Box(contentAlignment = Alignment.Center) { Text(name.take(1), color = GPurple, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall) } }
}
