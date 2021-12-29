package com.example.compasssample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: CompassViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[CompassViewModel::class.java]

    }

    override fun onStart() {
        super.onStart()

        viewModel.registerSensors()

        setContent {
            MaterialTheme {
                CompassComposable(viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterSensors()
    }
}

@Composable
fun CompassComposable(viewModel: CompassViewModel) {
    val compassHeading: Float by viewModel.currentHeading.observeAsState(initial = 0.0f)
    val accuracyReading: Int by viewModel.currentAccuracy.observeAsState(initial = 0)
    val altitudeReading: Float by viewModel.currentAltitude.observeAsState(initial = 0.0f)

    val rotationAnimation = animateFloatAsState(targetValue = compassHeading)

    Column(Modifier.padding(12.dp)) {
        Text(text = "Elevation:  $altitudeReading")
        Text(text = "Accuracy: $accuracyReading m")
        Text(text = "Current heading: $compassHeading")
        Image(
            painter = painterResource(id = R.drawable.compass),
            contentDescription = stringResource(id = R.string.compass),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = rotationAnimation.value)
        )
    }
}
