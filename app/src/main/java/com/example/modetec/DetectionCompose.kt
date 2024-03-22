package com.example.modetec

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

object ActivityRepository {
    private val _detectedActivity = MutableLiveData<String>()
    val detectedActivity: LiveData<String> = _detectedActivity

    fun updateDetectedActivity(activity: String) {
        _detectedActivity.postValue(activity)
    }
}

class ActivityRecognitionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val result = ActivityRecognitionResult.extractResult(intent)
        val detectedActivity = result?.mostProbableActivity
        val activityName = when (detectedActivity?.type) {
            DetectedActivity.IN_VEHICLE -> "in_vehicle"
            DetectedActivity.RUNNING -> "running"
            DetectedActivity.STILL -> "still"
            DetectedActivity.WALKING or DetectedActivity.ON_FOOT -> "walking"
            DetectedActivity.TILTING -> "tilting"
            DetectedActivity.ON_BICYCLE -> "bicycle"
            else -> "unknown"
        }
        ActivityRepository.updateDetectedActivity(activityName)
    }
}


class ActivityViewModel : ViewModel() {
    private val _currentActivity = MutableLiveData<String>("loading")
    val currentActivity: LiveData<String> = _currentActivity

    init {
        ActivityRepository.detectedActivity.observeForever {
            _currentActivity.value = it
        }
    }

    fun startTrackingUserActivity(context: Context) {
        val client = ActivityRecognition.getClient(context)
        val intent = Intent(context, ActivityRecognitionBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        client.requestActivityUpdates(10000, pendingIntent).addOnSuccessListener {
            // Successfully started activity updates
        }.addOnFailureListener {
            // Failed to start activity updates
        }
    }

//    fun updateActivity(activityType: Int, confidence: Int) {
//        val activityName = when (activityType) {
//            DetectedActivity.IN_VEHICLE -> "vehicle"
//            DetectedActivity.RUNNING -> "running"
//            DetectedActivity.STILL -> "still"
//            DetectedActivity.WALKING -> "walking"
//            else -> "unknown"
//        }
//        _currentActivity.value = activityName
//    }
}
