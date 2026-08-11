package com.example.melodyplayer.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import com.example.melodyplayer.MusicConstants
import com.example.melodyplayer.R

class MusicPlayerService : Service() {

    data class Song(val title: String, val resId: Int)

    private val playlist = listOf(
        Song("SoundHelix Song 1", R.raw.song1),
        Song("SoundHelix Song 2", R.raw.song2),
    )

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    private var isPrepared = false

    private var progressThread: HandlerThread? = null
    private var progressHandler: Handler? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            val player = mediaPlayer ?: return
            if (player.isPlaying) {
                broadcastProgress(player.currentPosition, player.duration)
                progressHandler?.postDelayed(this, MusicConstants.PROGRESS_INTERVAL_MS)
            }
        }
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    private val binder = MusicBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        progressThread = HandlerThread("MusicProgressThread").apply { start() }
        progressHandler = Handler(progressThread!!.looper)
    }

    fun getPlaylist(): List<Song> = playlist

    fun getCurrentIndex(): Int = currentIndex

    fun getCurrentSong(): Song = playlist[currentIndex]

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun play(index: Int = currentIndex) {
        if (playlist.isEmpty()) return
        currentIndex = index.coerceIn(0, playlist.lastIndex)
        releasePlayer()
        preparePlayer(playlist[currentIndex].resId) {
            mediaPlayer?.start()
            broadcastState(true)
            broadcastSongChanged()
            startProgressPolling()
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                stopProgressPolling()
                broadcastState(false)
                broadcastProgress(player.currentPosition, player.duration)
            }
        }
    }

    fun resume() {
        mediaPlayer?.let { player ->
            if (isPrepared && !player.isPlaying) {
                player.start()
                broadcastState(true)
                startProgressPolling()
            }
        }
    }

    fun togglePlayPause() {
        if (mediaPlayer == null || !isPrepared) {
            play(currentIndex)
        } else if (mediaPlayer!!.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let { player ->
            val safePosition = positionMs.coerceIn(0, player.duration.coerceAtLeast(0))
            player.seekTo(safePosition)
            broadcastProgress(safePosition, player.duration)
        }
    }

    fun playNext() {
        val next = if (currentIndex >= playlist.lastIndex) 0 else currentIndex + 1
        play(next)
    }

    fun playPrevious() {
        val player = mediaPlayer
        if (player != null && player.currentPosition > 3000) {
            seekTo(0)
            return
        }
        val prev = if (currentIndex <= 0) playlist.lastIndex else currentIndex - 1
        play(prev)
    }

    private fun preparePlayer(resId: Int, onReady: () -> Unit) {
        mediaPlayer = MediaPlayer.create(this, resId)?.apply {
            setOnCompletionListener {
                stopProgressPolling()
                mainHandler.post { playNext() }
            }
        }
        isPrepared = mediaPlayer != null
        if (isPrepared) {
            onReady()
        }
    }

    private fun startProgressPolling() {
        progressHandler?.removeCallbacks(progressRunnable)
        progressHandler?.post(progressRunnable)
    }

    private fun stopProgressPolling() {
        progressHandler?.removeCallbacks(progressRunnable)
    }

    private fun broadcastProgress(position: Int, duration: Int) {
        val intent = Intent(MusicConstants.ACTION_PROGRESS).apply {
            setPackage(packageName)
            putExtra(MusicConstants.EXTRA_POSITION, position)
            putExtra(MusicConstants.EXTRA_DURATION, duration)
        }
        sendBroadcast(intent)
    }

    private fun broadcastState(playing: Boolean) {
        val intent = Intent(MusicConstants.ACTION_STATE).apply {
            setPackage(packageName)
            putExtra(MusicConstants.EXTRA_IS_PLAYING, playing)
            putExtra(MusicConstants.EXTRA_SONG_INDEX, currentIndex)
            putExtra(MusicConstants.EXTRA_SONG_TITLE, getCurrentSong().title)
        }
        sendBroadcast(intent)
    }

    private fun broadcastSongChanged() {
        val intent = Intent(MusicConstants.ACTION_SONG_CHANGED).apply {
            setPackage(packageName)
            putExtra(MusicConstants.EXTRA_SONG_INDEX, currentIndex)
            putExtra(MusicConstants.EXTRA_SONG_TITLE, getCurrentSong().title)
        }
        sendBroadcast(intent)
    }

    private fun releasePlayer() {
        stopProgressPolling()
        mediaPlayer?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
        isPrepared = false
    }

    override fun onDestroy() {
        releasePlayer()
        progressThread?.quitSafely()
        progressThread = null
        progressHandler = null
        super.onDestroy()
    }
}
