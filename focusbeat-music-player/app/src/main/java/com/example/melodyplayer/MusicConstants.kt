package com.example.melodyplayer

object MusicConstants {
    const val ACTION_PROGRESS = "com.example.melodyplayer.ACTION_PROGRESS"
    const val ACTION_STATE = "com.example.melodyplayer.ACTION_STATE"
    const val ACTION_SONG_CHANGED = "com.example.melodyplayer.ACTION_SONG_CHANGED"

    const val EXTRA_POSITION = "position"
    const val EXTRA_DURATION = "duration"
    const val EXTRA_IS_PLAYING = "is_playing"
    const val EXTRA_SONG_INDEX = "song_index"
    const val EXTRA_SONG_TITLE = "song_title"

    const val MSG_UPDATE_PROGRESS = 1
    const val MSG_UPDATE_STATE = 2
    const val MSG_SONG_CHANGED = 3

    const val PROGRESS_INTERVAL_MS = 500L
}
