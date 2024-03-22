package com.example.modetec
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActivityRecognitionWithPermission()
        }
    }
}


@Composable
fun ActivityRecognitionWithPermission() {
    val activityViewModel = ActivityViewModel()
    val context = LocalContext.current
    val permissionStatus by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            activityViewModel.startTrackingUserActivity(context)
        } else {
            // Handle permission denial
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionStatus) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            activityViewModel.startTrackingUserActivity(context)
        }
    }

    //UI Composable to display activity
    MyApp(activityViewModel)
}


@Composable
fun MyApp(viewModel: ActivityViewModel) {
//    lateinit var dbHelper: ActivityLogDbHelper
    // define the ViewModel to update activity
    val currentActivity by viewModel.currentActivity.observeAsState("loading")
    var previousActivity by remember { mutableStateOf("loading") }
    var previousActivityChangeTime by remember { mutableStateOf(LocalDateTime.now()) }
    val context = LocalContext.current
    val dbHelper = ActivityLogDbHelper(context)

    // Simulate activity change
    LaunchedEffect(key1 = currentActivity) {
//        delay(2333)
//        previousActivity = currentActivity
//        currentActivity = "walking"  // Simulate an activity change
//        previousActivity = currentActivity
        val duration = Duration.between(previousActivityChangeTime, LocalDateTime.now())
        previousActivityChangeTime = LocalDateTime.now() // Update the time of activity change

        if (previousActivity != currentActivity) {
            Toast.makeText(
                context,
                "You have just $previousActivity for ${duration.toMinutes()} min, ${duration.seconds % 60} sec",
                Toast.LENGTH_LONG
            ).show()
        }
        previousActivity = currentActivity
        // Define custom formats
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        insertActivityLog(dbHelper, previousActivityChangeTime.format(dateFormatter),previousActivityChangeTime.format((timeFormatter)),duration.toMinutes().toInt(),previousActivity )
    }

    Column (verticalArrangement = Arrangement.spacedBy(30.dp)){
        Display_date_time()
        Showing_activity(currentActivity)
        if (currentActivity == "loading"){
            Welcome()
        }
        // special activities
        if (currentActivity == "running"){
            PlayMusic(musicResId = R.raw.running_bg)
        }
        if (currentActivity == "walking"){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF9C27B0)),
            ) {
                Text(
                    text = "COME ON!",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

        }
    }
}

@Composable
fun Welcome() {
    var showText by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (showText) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF000000)),
            ) {
                Text(
                    text = "W E L C O M E",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    // Use LaunchedEffect to start a coroutine that will update the state after 3 seconds
    LaunchedEffect(Unit) {
        delay(5000) // Wait for 3 seconds
        showText = false // Hide the text
    }
}


@Composable
fun Display_date_time() {
    var currentDateTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss"))
            delay(1000)
        }
    }

    // Styling the text
    Box(modifier = Modifier
        .background(MaterialTheme.colors.background)
        ) {
        Text(
            text = "Date & Time\n$currentDateTime",
//            color = MaterialTheme.colors.primary,
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(10.dp)
                .background(Color(0xFFECEFF1), shape = MaterialTheme.shapes.medium)
                .fillMaxWidth()
        )
    }
}

@Composable
fun Showing_activity(activityType: String) {
    val imageResource = when (activityType) {
        "walking" ->  R.drawable.walking
        "running" -> R.drawable.running
        "in_vehicle" -> R.drawable.vehicle
        "still" -> R.drawable.still
        "tilting" -> R.drawable.tilting
        "bicycle" -> R.drawable.bicycle
        "loading" -> R.drawable.loading
        else -> R.drawable.unknown
    }
//    Box(contentAlignment = Alignment.Center, modifier = Modifier) {
//        Image(painter = painterResource(id = imageResource), contentDescription = activityType, modifier = Modifier.fillMaxWidth())
//    }
    Image(painter = painterResource(id = imageResource), contentDescription = activityType, modifier = Modifier.fillMaxWidth())

    // Create an infinite repeating animation for the breathing effect
    val infiniteTransition = rememberInfiniteTransition()
    val animatedOpacity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            // Tween animation for smooth opacity transition
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Text with animated opacity and scale for the breathing effect
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = "$activityType...",
            color = Color.Black,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier =
            Modifier
                .graphicsLayer(
                    alpha = animatedOpacity,
                    scaleX = animatedScale,
                    scaleY = animatedScale
                )
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )
    }
}
@Composable
fun PlayMusic(musicResId: Int) {
    val context = LocalContext.current

    val mediaPlayer = remember {
        MediaPlayer.create(context, musicResId).apply {
            // Instead of setting isLooping, listen for completion
            setOnCompletionListener { mp ->
                mp.reset()
                mp.setDataSource(context.resources.openRawResourceFd(musicResId))
                mp.prepare()
                mp.start()
            }
        }
    }

    DisposableEffect(key1 = mediaPlayer) {
        mediaPlayer.start()

        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}
