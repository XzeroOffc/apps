package com.sonora.musictools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Ink = Color(0xFF171916)
private val Panel = Color(0xFF20231E)
private val PanelRaised = Color(0xFF292D26)
private val Paper = Color(0xFFF0EFE9)
private val Cream = Color(0xFFEDECE6)
private val Muted = Color(0xFFA2A69C)
private val Quiet = Color(0xFF8F9389)
private val Lime = Color(0xFFD5F06B)
private val Line = Color(0xFF393D35)
private val ErrorRed = Color(0xFFF09A87)

private enum class AppRoute(val title: String) {
    SPLASH("Memuat"), LOGIN("Login"), CHECKING("Memeriksa akun"), HOME("Beranda"),
    SEARCH("Cari lagu"), PLAYER("Pemutar musik"), LIBRARY("Koleksi musik"), PLAYLISTS("Playlist")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(23, 25, 22)
        window.navigationBarColor = android.graphics.Color.rgb(23, 25, 22)
        window.decorView.systemUiVisibility = 0
        setContent { SonoraApp() }
    }
}

@Composable
private fun SonoraApp() {
    var route by remember { mutableStateOf(AppRoute.SPLASH) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        delay(1500)
        route = AppRoute.LOGIN
    }

    BackHandler(enabled = route != AppRoute.HOME && route != AppRoute.LOGIN && route != AppRoute.SPLASH) {
        route = AppRoute.HOME
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Ink, contentColor = Cream) {
            AnimatedContent(
                targetState = route,
                label = "screen_route_transition",
                transitionSpec = {
                    (fadeIn(tween(420, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(tween(420, easing = FastOutSlowInEasing)) { it / 18 })
                        .togetherWith(fadeOut(tween(250)) + slideOutHorizontally(tween(250)) { -it / 28 })
                }
            ) { target ->
                when (target) {
                    AppRoute.SPLASH -> SplashScreen()
                    AppRoute.LOGIN -> LoginScreen(
                        username = username,
                        password = password,
                        error = loginError,
                        onUsernameChange = { username = it; loginError = "" },
                        onPasswordChange = { password = it; loginError = "" },
                        onLogin = {
                            loginError = ""
                            route = AppRoute.CHECKING
                        }
                    )
                    AppRoute.CHECKING -> CheckingScreen {
                        if (username == OwnerCredentials.username && password == OwnerCredentials.password) {
                            route = AppRoute.HOME
                        } else {
                            loginError = "Username atau password tidak sesuai."
                            route = AppRoute.LOGIN
                        }
                    }
                    AppRoute.HOME -> HomeScreen(
                        username = username,
                        onNavigate = { route = it },
                        onLogout = {
                            username = ""
                            password = ""
                            loginError = ""
                            route = AppRoute.LOGIN
                        }
                    )
                    AppRoute.SEARCH, AppRoute.PLAYER, AppRoute.LIBRARY, AppRoute.PLAYLISTS ->
                        ComingSoonScreen(
                            route = target,
                            onBack = { route = AppRoute.HOME },
                            onNavigate = { route = it }
                        )
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    val spin by rememberInfiniteTransition(label = "record_spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "record_rotation"
    )
    Box(
        modifier = Modifier.fillMaxSize().background(Ink),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RecordMark(modifier = Modifier.size(86.dp).rotate(spin), size = 86.dp)
            Spacer(Modifier.height(25.dp))
            BrandWordmark(fontSize = 29)
            Spacer(Modifier.height(9.dp))
            Text("Menyiapkan ruang musikmu", fontSize = 14.sp, color = Muted)
            Spacer(Modifier.height(28.dp))
            androidx.compose.material3.LinearProgressIndicator(
                modifier = Modifier.width(145.dp).height(2.dp),
                color = Lime,
                trackColor = Line
            )
        }
    }
}

@Composable
private fun CheckingScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(900)
        onFinished()
    }
    Box(Modifier.fillMaxSize().background(Ink), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RecordMark(modifier = Modifier.size(76.dp), size = 76.dp)
            Spacer(Modifier.height(23.dp))
            BrandWordmark(25)
            Spacer(Modifier.height(10.dp))
            Text("Memeriksa akun pemilik", fontSize = 14.sp, color = Muted)
            Spacer(Modifier.height(28.dp))
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(25.dp), color = Lime, strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun LoginScreen(
    username: String,
    password: String,
    error: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Ink)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 27.dp, vertical = 35.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RecordMark(Modifier.size(38.dp), 38.dp)
                Spacer(Modifier.width(11.dp))
                BrandWordmark(20)
            }
            Spacer(Modifier.height(62.dp))
            Eyebrow("MUSIC TOOLS / PRIVATE ACCESS")
            Spacer(Modifier.height(12.dp))
            Text("Semua musik,", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.8).sp, color = Cream)
            Text("satu ruang.", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.8).sp, color = Lime)
            Spacer(Modifier.height(12.dp))
            Text("Ruang kerja untuk mencari, memutar, dan mengatur musik.", fontSize = 14.sp, lineHeight = 21.sp, color = Muted)
            Spacer(Modifier.height(35.dp))

            Column(
                modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Eyebrow("AKUN PEMILIK")
                Spacer(Modifier.height(7.dp))
                Text("Selamat datang.", fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.7).sp)
                Spacer(Modifier.height(5.dp))
                Text("Masuk untuk membuka dashboard.", fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(21.dp))
                FieldLabel("Username")
                OwnerField(value = username, onValueChange = onUsernameChange, hint = "Masukkan username")
                Spacer(Modifier.height(15.dp))
                FieldLabel("Password")
                OwnerField(
                    value = password,
                    onValueChange = onPasswordChange,
                    hint = "Masukkan password",
                    secret = !showPassword,
                    trailing = {
                        TextButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = if (showPassword) "Sembunyikan password" else "Tampilkan password", tint = Muted, modifier = Modifier.size(19.dp))
                        }
                    }
                )
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
                }
                Spacer(Modifier.height(19.dp))
                Button(
                    onClick = onLogin,
                    enabled = username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink, disabledContainerColor = Color(0xFF737C57), disabledContentColor = Ink)
                ) {
                    Text("Masuk ke dashboard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(19.dp))
                }
            }
            Spacer(Modifier.height(17.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = Quiet, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text("Akses khusus pemilik aplikasi", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Quiet)
            }
            Spacer(Modifier.height(30.dp))
            Text("SONORA MUSIC TOOLS  ·  OWNER ACCESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, color = Quiet)
        }
    }
}

@Composable
private fun HomeScreen(username: String, onNavigate: (AppRoute) -> Unit, onLogout: () -> Unit) {
    androidx.compose.material3.Scaffold(
        containerColor = Ink,
        contentWindowInsets = WindowInsets(0),
        bottomBar = { BottomNav(selected = AppRoute.HOME, onNavigate = onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 22.dp, bottom = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Eyebrow("RUANG KERJA PEMILIK")
                    Spacer(Modifier.height(8.dp))
                    Text("Halo, ${username.ifBlank { "owner" }}.", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.1).sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(42.dp).background(PanelRaised, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(username.take(1).uppercase().ifBlank { "O" }, fontSize = 15.sp, color = Lime, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onLogout) {
                        Text("Keluar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Muted)
                    }
                }
            }
            Spacer(Modifier.height(7.dp))
            Text("Siapkan pengalaman musikmu dari satu tempat.", fontSize = 13.sp, color = Muted)
            Spacer(Modifier.height(23.dp))
            FeaturePanel(onClick = { onNavigate(AppRoute.SEARCH) })
            Spacer(Modifier.height(26.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Eyebrow("RUTE APLIKASI")
                    Spacer(Modifier.height(5.dp))
                    Text("Tools musik", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text("Pilih menu untuk melihat halaman", fontSize = 10.sp, color = Quiet)
            }
            Spacer(Modifier.height(10.dp))
            ToolRow("01", "Cari lagu", "Temukan musik dari judul atau artis", AppRoute.SEARCH, Icons.Outlined.Search, onNavigate)
            ToolRow("02", "Pemutar musik", "Putar audio dalam satu layar", AppRoute.PLAYER, Icons.Outlined.PlayArrow, onNavigate)
            ToolRow("03", "Koleksi musik", "Simpan lagu favorit", AppRoute.LIBRARY, Icons.Outlined.LibraryMusic, onNavigate)
            ToolRow("04", "Playlist", "Atur lagu dalam playlist", AppRoute.PLAYLISTS, Icons.Outlined.MusicNote, onNavigate)
            Spacer(Modifier.height(12.dp))
            Text("FITUR MUSIK SEDANG DISIAPKAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = Quiet)
        }
    }
}

@Composable
private fun FeaturePanel(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(218.dp).background(Panel, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(19.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Eyebrow("SONORA / MUSIC TOOLS")
            Spacer(Modifier.height(12.dp))
            Text("Musikmu.", fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.5).sp)
            Text("Caramu.", fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.5).sp, color = Lime)
            Spacer(Modifier.height(9.dp))
            Text("Tools musik sedang disiapkan.", fontSize = 12.sp, color = Muted)
            Spacer(Modifier.height(13.dp))
            Text("LIHAT TOOLS  →", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp, color = Lime)
        }
        RecordMark(Modifier.size(116.dp), 116.dp)
    }
}

@Composable
private fun ToolRow(
    number: String,
    title: String,
    subtitle: String,
    route: AppRoute,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onNavigate: (AppRoute) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onNavigate(route) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(number, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Quiet, modifier = Modifier.width(26.dp))
        Box(Modifier.size(38.dp).background(PanelRaised, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Cream, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("SOON", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .4.sp, color = Quiet)
    }
    HorizontalDivider(color = Line, thickness = .7.dp)
}

@Composable
private fun ComingSoonScreen(route: AppRoute, onBack: () -> Unit, onNavigate: (AppRoute) -> Unit) {
    androidx.compose.material3.Scaffold(
        containerColor = Ink,
        contentWindowInsets = WindowInsets(0),
        bottomBar = { BottomNav(selected = route, onNavigate = onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(top = 18.dp, bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) {
                    Text("‹  Dashboard", color = Muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(38.dp))
            RecordMark(Modifier.size(132.dp), 132.dp)
            Spacer(Modifier.height(30.dp))
            Eyebrow("${route.title.uppercase()} / SEDANG DISIAPKAN")
            Spacer(Modifier.height(12.dp))
            Text(route.title, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.1).sp, textAlign = TextAlign.Center)
            Text("segera hadir.", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.1).sp, color = Lime, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                "Halaman ini sudah tersedia di aplikasi. Fungsinya sedang dalam tahap pengembangan.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Muted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            OutlinedButton(
                onClick = onBack,
                border = BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Cream)
            ) { Text("Kembali ke dashboard", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun BottomNav(selected: AppRoute, onNavigate: (AppRoute) -> Unit) {
    val items = listOf(
        Triple(AppRoute.HOME, "Beranda", Icons.Outlined.Home),
        Triple(AppRoute.SEARCH, "Cari", Icons.Outlined.Search),
        Triple(AppRoute.PLAYER, "Putar", Icons.Outlined.PlayArrow),
        Triple(AppRoute.LIBRARY, "Koleksi", Icons.Outlined.LibraryMusic)
    )
    Row(
        modifier = Modifier.fillMaxWidth().background(Panel).padding(horizontal = 11.dp, vertical = 9.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (route, label, icon) ->
            val active = route == selected
            Column(
                modifier = Modifier.weight(1f).clickable { onNavigate(route) }.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = label, tint = if (active) Lime else Muted, modifier = Modifier.size(21.dp))
                Text(label, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, color = if (active) Lime else Muted)
            }
        }
    }
}

@Composable
private fun RecordMark(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp) {
    Box(modifier = modifier.background(Lime, CircleShape), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(size * .12f)) {
            val center = this.center
            val radius = this.size.minDimension / 2f
            drawCircle(color = Ink, radius = radius, style = Stroke(width = this.size.minDimension * .035f))
            drawCircle(color = Ink.copy(alpha = .42f), radius = radius * .72f, style = Stroke(width = this.size.minDimension * .025f))
            drawCircle(color = Ink.copy(alpha = .30f), radius = radius * .46f, style = Stroke(width = this.size.minDimension * .02f))
            drawCircle(color = Ink, radius = radius * .24f)
            drawCircle(color = Lime, radius = radius * .055f)
        }
    }
}

@Composable
private fun BrandWordmark(fontSize: Int) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text("SONORA", fontSize = fontSize.sp, fontWeight = FontWeight.Black, letterSpacing = (-1.2).sp, color = Cream)
        Text(".", fontSize = fontSize.sp, fontWeight = FontWeight.Black, color = Lime)
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = Quiet)
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Cream)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun OwnerField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    secret: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(51.dp).background(Ink, RoundedCornerShape(10.dp))
            .padding(start = 14.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (secret) {
            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Quiet, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(9.dp))
        } else if (hint.contains("username", ignoreCase = true)) {
            Text("@", color = Quiet, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(9.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(color = Cream, fontSize = 14.sp, fontWeight = FontWeight.Medium),
            visualTransformation = if (secret) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(hint, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Quiet)
                    inner()
                }
            }
        )
        trailing?.invoke()
    }
}
