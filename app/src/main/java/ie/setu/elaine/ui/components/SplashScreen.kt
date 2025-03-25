package ie.setu.elaine.ui.components

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import ie.setu.elaine.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(key1 = true) {
        scale.animateTo(targetValue = 5f,
            animationSpec = tween(durationMillis = 1050,
                easing = { OvershootInterpolator(1f).getInterpolation(it) }),

        )
        delay(2500L)
        navController.navigate("routineList")
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize() ) {
        Image(painter = painterResource(id = R.drawable.gradient), contentDescription = "Logo", modifier = Modifier.scale(scale.value))
    }
}