package com.sonora.musictools

import android.os.Bundle
import android.media.AudioAttributes
import android.media.MediaPlayer
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
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
    var selectedTrack by remember { mutableStateOf<MusicTrack?>(null) }
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf(ApiKeyStore.read(context)) }

    LaunchedEffect(Unit) {
        delay(1500)
        route = AppRoute.LOGIN
    }

    BackHandler(enabled = route != AppRoute.HOME && route != AppRoute.LOGIN && route != AppRoute.SPLASH) {
        route = if (route == AppRoute.PLAYER) AppRoute.SEARCH else AppRoute.HOME
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
                    AppRoute.SEARCH -> SearchScreen(
                        apiKey = apiKey,
                        onSaveApiKey = { newKey ->
                            ApiKeyStore.save(context, newKey)
                            apiKey = newKey
                        },
                        onSelectTrack = { track ->
                            selectedTrack = track
                            route = AppRoute.PLAYER
                        },
                        onNavigate = { route = it }
                    )
                    AppRoute.PLAYER -> PlayerScreen(
                        track = selectedTrack,
                        apiKey = apiKey,
                        onBack = { route = AppRoute.SEARCH },
                        onNavigate = { route = it }
                    )
                    AppRoute.LIBRARY, AppRoute.PLAYLISTS ->
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
private fun SearchScreen(
    apiKey: String,
    onSaveApiKey: (String) -> Unit,
    onSelectTrack: (MusicTrack) -> Unit,
    onNavigate: (AppRoute) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var keyInput by remember { mutableStateOf("") }
    var showKeySetup by remember { mutableStateOf(apiKey.isBlank()) }
    var results by remember { mutableStateOf(emptyList<MusicTrack>()) }
    var loading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun search() {
        if (query.isBlank() || loading) return
        if (apiKey.isBlank()) {
            showKeySetup = true
            error = "Tambahkan API key untuk mulai mencari lagu."
            return
        }
        loading = true
        hasSearched = true
        error = ""
        results = emptyList()
        scope.launch {
            try {
                results = MusicApi.searchTracks(query.trim(), apiKey)
            } catch (exception: Exception) {
                error = exception.message ?: "Pencarian gagal. Periksa koneksi lalu coba lagi."
            } finally {
                loading = false
            }
        }
    }

    androidx.compose.material3.Scaffold(
        containerColor = Ink,
        contentWindowInsets = WindowInsets(0),
        bottomBar = { BottomNav(selected = AppRoute.SEARCH, onNavigate = onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 22.dp, bottom = 28.dp)
        ) {
            Eyebrow("SONORA / DISCOVER")
            Spacer(Modifier.height(7.dp))
            Text("Cari lagu", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.1).sp)
            Spacer(Modifier.height(5.dp))
            Text("Temukan track, lalu buka liriknya.", fontSize = 13.sp, color = Muted)
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SearchInput(
                    value = query,
                    onValueChange = { query = it },
                    hint = "Judul lagu atau nama artis",
                    modifier = Modifier.weight(1f),
                    leading = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Muted, modifier = Modifier.size(19.dp)) }
                )
                Spacer(Modifier.width(9.dp))
                Button(
                    onClick = { search() },
                    enabled = query.isNotBlank() && !loading,
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink),
                    modifier = Modifier.height(50.dp)
                ) { Text("Cari", fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(13.dp))
            if (apiKey.isNotBlank()) {
                TextButton(onClick = { showKeySetup = !showKeySetup }) {
                    Text(if (showKeySetup) "Tutup pengaturan key" else "Atur API key", color = Muted, fontSize = 12.sp)
                }
            }
            if (showKeySetup) {
                Surface(color = Panel, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Line)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp)) {
                        Text("API key Sylvatica", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(5.dp))
                        Text("Key disimpan terenkripsi di perangkat ini.", color = Muted, fontSize = 12.sp)
                        Spacer(Modifier.height(11.dp))
                        SearchInput(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            hint = "Tempel API key di sini",
                            obscured = true
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (keyInput.isNotBlank()) {
                                    onSaveApiKey(keyInput.trim())
                                    keyInput = ""
                                    showKeySetup = false
                                    error = ""
                                }
                            },
                            enabled = keyInput.isNotBlank(),
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink)
                        ) { Text("Simpan key", fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            if (error.isNotBlank()) {
                FeedbackNotice(text = error, isError = true)
                Spacer(Modifier.height(13.dp))
            }
            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 18.dp)) {
                    androidx.compose.material3.CircularProgressIndicator(color = Lime, strokeWidth = 2.dp, modifier = Modifier.size(21.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Mencari lagu…", fontSize = 13.sp, color = Muted)
                }
            } else if (hasSearched && error.isBlank() && results.isEmpty()) {
                FeedbackNotice(text = "Belum ada hasil. Coba judul atau nama artis lain.", isError = false)
            } else if (results.isNotEmpty()) {
                Spacer(Modifier.height(7.dp))
                Text("HASIL PENCARIAN  ·  ${results.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .9.sp, color = Muted)
                Spacer(Modifier.height(6.dp))
                results.forEachIndexed { index, track ->
                    SearchResultRow(index = index + 1, track = track, onClick = { onSelectTrack(track) })
                    HorizontalDivider(color = Line, thickness = .7.dp)
                }
            } else if (!hasSearched && error.isBlank()) {
                Spacer(Modifier.height(14.dp))
                Text("Cari judul lagu atau artis untuk melihat hasil dan liriknya.", fontSize = 13.sp, color = Quiet, lineHeight = 19.sp)
            }
        }
    }
}

@Composable
private fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    obscured: Boolean = false,
    leading: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.height(50.dp),
        color = Panel,
        shape = RoundedCornerShape(11.dp),
        border = BorderStroke(1.dp, Line)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading?.invoke()
            if (leading != null) Spacer(Modifier.width(9.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(hint, fontSize = 13.sp, color = Quiet, maxLines = 1, overflow = TextOverflow.Ellipsis)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = Cream),
                    visualTransformation = if (obscured) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(index: Int, track: MusicTrack, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(index.toString().padStart(2, '0'), fontSize = 10.sp, color = Quiet, modifier = Modifier.width(27.dp))
        Box(Modifier.size(43.dp).background(PanelRaised, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(track.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text("${track.artist}  ·  ${track.album}", fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text(track.duration, fontSize = 11.sp, color = Quiet)
        IconButton(onClick = onClick) {
            Icon(Icons.Outlined.PlayArrow, contentDescription = "Lihat lagu dan lirik", tint = Lime)
        }
    }
}

@Composable
private fun FeedbackNotice(text: String, isError: Boolean) {
    Surface(color = Panel, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (isError) ErrorRed.copy(alpha = .6f) else Line)) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            fontSize = 13.sp,
            color = if (isError) ErrorRed else Muted,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun PlayerScreen(
    track: MusicTrack?,
    apiKey: String,
    onBack: () -> Unit,
    onNavigate: (AppRoute) -> Unit
) {
    var lyrics by remember(track?.spotifyUrl) { mutableStateOf<TrackLyrics?>(null) }
    var media by remember(track?.spotifyUrl) { mutableStateOf<PlayableMedia?>(null) }
    var lyricsLoading by remember(track?.spotifyUrl) { mutableStateOf(track != null) }
    var mediaLoading by remember(track?.spotifyUrl) { mutableStateOf(track != null) }
    var lyricsError by remember(track?.spotifyUrl) { mutableStateOf("") }
    var mediaError by remember(track?.spotifyUrl) { mutableStateOf("") }
    var retryCount by remember(track?.spotifyUrl) { mutableStateOf(0) }
    var mediaPlayer by remember(track?.spotifyUrl) { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember(track?.spotifyUrl) { mutableStateOf(false) }
    var isBuffering by remember(track?.spotifyUrl) { mutableStateOf(false) }
    var playbackError by remember(track?.spotifyUrl) { mutableStateOf("") }
    var playbackPosition by remember(track?.spotifyUrl) { mutableStateOf(0) }
    var playbackDuration by remember(track?.spotifyUrl) { mutableStateOf(0) }

    LaunchedEffect(track?.spotifyUrl, apiKey, retryCount) {
        val selected = track ?: return@LaunchedEffect
        lyrics = null
        media = null
        lyricsError = ""
        mediaError = ""
        playbackError = ""
        if (apiKey.isBlank()) {
            lyricsLoading = false
            mediaLoading = false
            lyricsError = "Tambahkan API key dari halaman Search untuk memuat lirik."
            mediaError = "Tambahkan API key dari halaman Search untuk memutar lagu."
            return@LaunchedEffect
        }
        lyricsLoading = true
        mediaLoading = true
        coroutineScope {
            launch {
                runCatching { MusicApi.findLyrics(selected, apiKey) }
                    .onSuccess { lyrics = it }
                    .onFailure { lyricsError = it.message ?: "Lirik gagal dimuat. Periksa koneksi lalu coba lagi." }
                lyricsLoading = false
            }
            launch {
                runCatching { MusicApi.getPlayableMedia(selected, apiKey) }
                    .onSuccess { media = it }
                    .onFailure { mediaError = it.message ?: "Audio gagal dimuat. Periksa koneksi lalu coba lagi." }
                mediaLoading = false
            }
        }
    }

    DisposableEffect(media?.url) {
        val audioUrl = media?.url
        if (audioUrl.isNullOrBlank()) return@DisposableEffect onDispose { }
        val player = MediaPlayer()
        mediaPlayer = player
        isBuffering = true
        playbackError = ""
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        player.setOnPreparedListener {
            playbackDuration = it.duration.coerceAtLeast(0)
            isBuffering = false
            runCatching { it.start(); isPlaying = true }
                .onFailure { playbackError = "Audio tidak dapat diputar di perangkat ini." }
        }
        player.setOnCompletionListener {
            isPlaying = false
            playbackPosition = 0
            runCatching { it.seekTo(0) }
        }
        player.setOnErrorListener { _, _, _ ->
            isBuffering = false
            isPlaying = false
            playbackError = "Pemutaran audio terputus. Coba muat ulang lagu."
            true
        }
        runCatching {
            player.setDataSource(audioUrl)
            player.prepareAsync()
        }.onFailure {
            isBuffering = false
            playbackError = "Tautan audio tidak dapat dibuka. Coba muat ulang lagu."
        }
        onDispose {
            if (mediaPlayer === player) mediaPlayer = null
            runCatching { player.release() }
            isPlaying = false
            isBuffering = false
        }
    }

    LaunchedEffect(mediaPlayer) {
        while (mediaPlayer != null) {
            val player = mediaPlayer
            if (player != null && runCatching { player.isPlaying }.getOrDefault(false)) {
                playbackPosition = runCatching { player.currentPosition }.getOrDefault(playbackPosition)
            }
            delay(500)
        }
    }

    androidx.compose.material3.Scaffold(
        containerColor = Ink,
        contentWindowInsets = WindowInsets(0),
        bottomBar = { BottomNav(selected = AppRoute.PLAYER, onNavigate = onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 28.dp)
        ) {
            TextButton(onClick = onBack) { Text("‹  Kembali ke pencarian", color = Muted, fontWeight = FontWeight.SemiBold) }
            if (track == null) {
                Spacer(Modifier.height(46.dp))
                RecordMark(Modifier.size(100.dp), 100.dp)
                Spacer(Modifier.height(22.dp))
                Text("Pilih lagu dulu", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(7.dp))
                Text("Cari lagu untuk memutar audio dan melihat liriknya.", fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(17.dp))
                Button(onClick = { onNavigate(AppRoute.SEARCH) }, modifier = Modifier.height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink)) {
                    Text("Buka Search", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(Modifier.height(10.dp))
                Eyebrow("NOW PLAYING / SONORA")
                Spacer(Modifier.height(13.dp))
                Surface(color = Panel, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Line)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        RecordMark(Modifier.size(72.dp), 72.dp)
                        Spacer(Modifier.width(15.dp))
                        Column(Modifier.weight(1f)) {
                            Text(track.name, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 23.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(track.artist, fontSize = 13.sp, color = Muted)
                            Spacer(Modifier.height(3.dp))
                            Text("${track.album}  ·  ${media?.duration ?: track.duration}", fontSize = 11.sp, color = Quiet, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                when {
                    mediaLoading || isBuffering -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 14.dp)) {
                        androidx.compose.material3.CircularProgressIndicator(color = Lime, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(if (mediaLoading) "Mencari audio…" else "Memuat pemutar…", fontSize = 13.sp, color = Muted)
                    }
                    mediaError.isNotBlank() || playbackError.isNotBlank() -> {
                        FeedbackNotice(playbackError.ifBlank { mediaError }, isError = true)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { retryCount++ }, modifier = Modifier.height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = PanelRaised, contentColor = Cream)) {
                            Text("Coba lagi", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    mediaPlayer != null -> {
                        Slider(
                            value = playbackPosition.toFloat().coerceIn(0f, playbackDuration.coerceAtLeast(1).toFloat()),
                            onValueChange = { position ->
                                playbackPosition = position.toInt()
                                runCatching { mediaPlayer?.seekTo(position.toInt()) }
                            },
                            valueRange = 0f..playbackDuration.coerceAtLeast(1).toFloat()
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatPlaybackTime(playbackPosition), fontSize = 11.sp, color = Quiet)
                            Text(formatPlaybackTime(playbackDuration), fontSize = 11.sp, color = Quiet)
                        }
                        Spacer(Modifier.height(5.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            IconButton(
                                onClick = {
                                    runCatching {
                                        if (isPlaying) mediaPlayer?.pause() else mediaPlayer?.start()
                                        isPlaying = mediaPlayer?.isPlaying == true
                                    }.onFailure { playbackError = "Pemutaran audio gagal." }
                                },
                                enabled = !isBuffering,
                                modifier = Modifier.size(58.dp).background(Lime, CircleShape)
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = if (isPlaying) "Jeda" else "Putar",
                                    tint = Ink,
                                    modifier = Modifier.size(31.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(25.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Eyebrow("WORDS / LYRICS")
                        Spacer(Modifier.height(5.dp))
                        Text("Lirik lagu", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(if (lyrics?.syncedLyrics?.isNotBlank() == true) "TIMED LYRICS" else "", fontSize = 9.sp, color = Quiet, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                when {
                    lyricsLoading -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
                        androidx.compose.material3.CircularProgressIndicator(color = Lime, strokeWidth = 2.dp, modifier = Modifier.size(21.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Memuat lirik…", fontSize = 13.sp, color = Muted)
                    }
                    lyricsError.isNotBlank() -> FeedbackNotice(lyricsError, isError = true)
                    lyrics == null -> FeedbackNotice("Lirik tidak ditemukan. Coba lagu lain dari Search.", isError = false)
                    else -> {
                        val lyricsText = lyrics!!.plainLyrics.ifBlank {
                            lyrics!!.syncedLyrics.replace(Regex("\\[\\d{2}:\\d{2}(?:\\.\\d{1,3})?\\]\\s*"), "")
                        }
                        if (lyrics!!.instrumental && lyricsText.isBlank()) {
                            FeedbackNotice("Track ini instrumental dan tidak memiliki lirik.", isError = false)
                        } else if (lyricsText.isBlank()) {
                            FeedbackNotice("Lirik belum tersedia untuk lagu ini.", isError = false)
                        } else {
                            Text(lyricsText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 27.sp, color = Cream)
                        }
                    }
                }
            }
        }
    }
}

private fun formatPlaybackTime(milliseconds: Int): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0) / 1000)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
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
