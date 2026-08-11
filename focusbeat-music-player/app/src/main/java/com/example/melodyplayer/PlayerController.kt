package com.example.melodyplayer

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Build
import android.os.Message
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.melodyplayer.service.MusicPlayerService

/**
 * 连接 MusicPlayerService，通过 Broadcast 接收进度，
 * 再用 Handler 将更新投递到主线程以刷新 Compose UI（课程中的 Handler 机制）。
 */
class PlayerController(private val context: Context) {

    var isBound by mutableStateOf(false)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var songTitle by mutableStateOf("")
        private set

    var songIndex by mutableIntStateOf(0)
        private set

    var currentPositionMs by mutableIntStateOf(0)
        private set

    var durationMs by mutableIntStateOf(0)
        private set

    var isUserSeeking by mutableStateOf(false)
        private set

    val progress: Float
        get() = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        } else {
            0f
        }

    private var musicService: MusicPlayerService? = null

    private val uiHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MusicConstants.MSG_UPDATE_PROGRESS -> {
                    currentPositionMs = msg.arg1
                    durationMs = msg.arg2
                }
                MusicConstants.MSG_UPDATE_STATE -> {
                    isPlaying = msg.arg1 == 1
                    songIndex = msg.arg2
                    songTitle = msg.obj as? String ?: songTitle
                }
                MusicConstants.MSG_SONG_CHANGED -> {
                    songIndex = msg.arg1
                    songTitle = msg.obj as? String ?: songTitle
                    currentPositionMs = 0
                }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                MusicConstants.ACTION_PROGRESS -> {
                    val position = intent.getIntExtra(MusicConstants.EXTRA_POSITION, 0)
                    val duration = intent.getIntExtra(MusicConstants.EXTRA_DURATION, 0)
                    uiHandler.obtainMessage(
                        MusicConstants.MSG_UPDATE_PROGRESS,
                        position,
                        duration,
                    ).sendToTarget()
                }
                MusicConstants.ACTION_STATE -> {
                    val playing = intent.getBooleanExtra(MusicConstants.EXTRA_IS_PLAYING, false)
                    val index = intent.getIntExtra(MusicConstants.EXTRA_SONG_INDEX, 0)
                    val title = intent.getStringExtra(MusicConstants.EXTRA_SONG_TITLE) ?: ""
                    uiHandler.obtainMessage(
                        MusicConstants.MSG_UPDATE_STATE,
                        if (playing) 1 else 0,
                        index,
                        title,
                    ).sendToTarget()
                }
                MusicConstants.ACTION_SONG_CHANGED -> {
                    val index = intent.getIntExtra(MusicConstants.EXTRA_SONG_INDEX, 0)
                    val title = intent.getStringExtra(MusicConstants.EXTRA_SONG_TITLE) ?: ""
                    uiHandler.obtainMessage(
                        MusicConstants.MSG_SONG_CHANGED,
                        index,
                        0,
                        title,
                    ).sendToTarget()
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            musicService = (binder as MusicPlayerService.MusicBinder).getService()
            isBound = true
            syncFromService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    fun bind() {
        val intent = Intent(context, MusicPlayerService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        val filter = IntentFilter().apply {
            addAction(MusicConstants.ACTION_PROGRESS)
            addAction(MusicConstants.ACTION_STATE)
            addAction(MusicConstants.ACTION_SONG_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(broadcastReceiver, filter)
        }
    }

    fun unbind() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
        try {
            context.unregisterReceiver(broadcastReceiver)
        } catch (_: IllegalArgumentException) {
        }
        uiHandler.removeCallbacksAndMessages(null)
    }

    private fun syncFromService() {
        musicService?.let { service ->
            songTitle = service.getCurrentSong().title
            songIndex = service.getCurrentIndex()
            isPlaying = service.isPlaying()
            durationMs = service.getDuration()
            currentPositionMs = service.getCurrentPosition()
        }
    }

    fun togglePlayPause() {
        musicService?.togglePlayPause()
    }

    fun playNext() {
        musicService?.playNext()
    }

    fun playPrevious() {
        musicService?.playPrevious()
    }

    fun playSong(index: Int) {
        musicService?.play(index)
    }

    fun onSeekStart() {
        isUserSeeking = true
    }

    fun onSeek(progressFraction: Float) {
        val duration = durationMs.coerceAtLeast(1)
        currentPositionMs = (progressFraction * duration).toInt()
    }

    fun onSeekFinished(progressFraction: Float) {
        isUserSeeking = false
        val duration = durationMs.coerceAtLeast(1)
        musicService?.seekTo((progressFraction * duration).toInt())
    }
}
