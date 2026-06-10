package com.game.recorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import         android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowManager

class RecordService : Service() {

    companion object {
        var isRunning = false
    }

    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var videoUri: Uri? = null
    private var pfd: ParcelFileDescriptor? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val resultCode = intent?.getIntExtra("resultCode", -1) ?: -1
            val resultData = intent?.getParcelableExtra("data") as? Intent

            if (resultCode != -1 && resultData != null) {
                startForegroundServiceWithNotification()
                startRecording(resultCode, resultData)
                isRunning = true
            } else {
                stopSelf()
            }
        } catch (t: Throwable) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "recorder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Screen Recorder", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("جاري تسجيل الشاشة والصوت")
                .setContentText("تطبيق مسجل الألعاب يعمل الآن في الخلفية...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("جاري تسجيل الشاشة والصوت")
                .setContentText("تطبيق مسجل الألعاب يعمل الآن في الخلفية...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(1, notification)
        }
    }

    private fun startRecording(resultCode: Int, resultData: Intent) {
        try {
            val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mpManager.getMediaProjection(resultCode, resultData)

            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            
            var screenWidth = metrics.widthPixels
            var screenHeight = metrics.heightPixels
            if (screenWidth % 2 != 0) screenWidth--
            if (screenHeight % 2 != 0) screenHeight--
            val screenDensity = metrics.densityDpi

            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "GameRecord_${System.currentTimeMillis()}.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/GameRecorder")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            videoUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            pfd = contentResolver.openFileDescriptor(videoUri!!, "rw")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoSize(screenWidth, screenHeight)
                setVideoFrameRate(30)
                setVideoEncodingBitRate(6 * 1024 * 1024) 
                setOutputFile(pfd?.fileDescriptor)
                prepare()
            }

            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "MainScreen",
                screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface, null, null
            )

            mediaRecorder?.start()
            
        } catch (t: Throwable) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        isRunning = false
        try {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (e: Exception) { e.printStackTrace() }

        try {
            virtualDisplay?.release()
            mediaProjection?.stop()
        } catch (e: Exception) { e.printStackTrace() }

        try {
            pfd?.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && videoUri != null) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                contentResolver.update(videoUri!!, values, null, null)
            }
        } catch (e: Exception) { e.printStackTrace() }
        
        super.onDestroy()
    }
}
