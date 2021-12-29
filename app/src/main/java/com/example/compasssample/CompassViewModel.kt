package com.example.compasssample

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class CompassViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {

    private val sensorManager by lazy {
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val _currentHeading = MutableLiveData<Float>()
    val currentHeading: LiveData<Float>
        get() = _currentHeading

    private val _currentAccuracy = MutableLiveData<Int>()
    val currentAccuracy: LiveData<Int>
        get() = _currentAccuracy

    private val _currentAltitude = MutableLiveData<Float>()
    val currentAltitude: LiveData<Float>
        get() = _currentAltitude

    private var accelerometerArray = FloatArray(3)
    private var magnetometerArray = FloatArray(3)
    private var pressureArray = FloatArray(3)
    private var accelerometerSet = false
    private var magnetometerSet = false
    private var pressureSet = false


    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lowPass(event.values, accelerometerArray)
                accelerometerSet = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                lowPass(event.values, magnetometerArray)
                magnetometerSet = true
            }
            Sensor.TYPE_PRESSURE -> {
                lowPass(event.values, pressureArray)
                pressureSet = true
            }
        }
        if (accelerometerSet && magnetometerSet) {

            _currentHeading.value = calculateHeading(accelerometerArray, magnetometerArray)
        }

        if (pressureSet) {
            _currentAltitude.value = calculateAltitude(pressureArray[0])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor != null) {
            if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
                _currentAccuracy.value = accuracy
            Log.d(TAG, "accuracy is: $accuracy")
        }
    }

    fun registerSensors() {
        sensorManager.let { manager ->
            manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).let {
                manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).let {
                manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            manager.getDefaultSensor(Sensor.TYPE_PRESSURE).let {
                manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun unregisterSensors() {
        Log.d(TAG, "unregistering sensors")
        sensorManager.unregisterListener(this)
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        val smoothingFactor = 0.05f

        for (i in input.indices) {
            output[i] = output[i] + smoothingFactor * (input[i] - output[i])
        }
    }

    private fun calculateHeading(
        accelerometerArrayVals: FloatArray,
        magnetometerArrayVals: FloatArray
    ): Float {
        val r = FloatArray(9)
        var result = 0.0f
        if (SensorManager.getRotationMatrix(
                r,
                null,
                accelerometerArrayVals,
                magnetometerArrayVals
            )
        ) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            result = -(Math.toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360
        }
        return result
    }

    private fun calculateAltitude(pressureValue: Float): Float {
        val altitude: Float =
            SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressureValue)
        Log.d(TAG, "altitude is $altitude")
        return altitude
    }

    companion object {
        private const val TAG = "CompassVM"
    }
}

