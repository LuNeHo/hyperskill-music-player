package org.hyperskill.musicplayer.domain

interface AudioPlayerDataSource {
    fun play()
    fun stop()
    fun getCurrentPosition(): Int
    fun isPlaying(): Boolean
}