package org.hyperskill.musicplayer.domain

interface AudioPlayerDataSource {
    fun play(startPositionMs: Int = 0)
    fun seekTo(positionMs: Int)
    fun pause()
    fun resume()
    fun stop()
    fun getCurrentPosition(): Int
    fun isPlaying(): Boolean
    fun setOnCompletionListener(listener: () -> Unit)
}