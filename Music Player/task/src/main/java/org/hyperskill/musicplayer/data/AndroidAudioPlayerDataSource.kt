package org.hyperskill.musicplayer.data

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import org.hyperskill.musicplayer.R
import org.hyperskill.musicplayer.domain.AudioPlayerDataSource
import org.hyperskill.musicplayer.presentation.UserIntent

class AndroidAudioPlayerDataSource(private val context: Context) : AudioPlayerDataSource {
    private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }
    private var isPrepared = false

    override fun play(startPositionMs: Int) {
        initMediaPlayer()
        if (startPositionMs > 0) seekTo(startPositionMs)
        mediaPlayer.start()
    }

    override fun seekTo(positionMs: Int) {
        if (!isPrepared) initMediaPlayer()
        mediaPlayer.seekTo(positionMs)
    }

    override fun pause() = mediaPlayer.pause()

    override fun resume() = mediaPlayer.start()

    override fun stop() {
        with(mediaPlayer) {
            if (isPlaying) stop()
            reset()
        }
        isPrepared = false
    }

    override fun getCurrentPosition(): Int = mediaPlayer.currentPosition

    override fun isPlaying(): Boolean = mediaPlayer.isPlaying

    override fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer.setOnCompletionListener { listener() }
    }

    private fun initMediaPlayer() {
        val songUriStr = "android.resource://${context.packageName}/${R.raw.wisdom}"

        with(mediaPlayer) {
            reset()
            setDataSource(context, songUriStr.toUri())
            prepare()
        }
        isPrepared = true
    }
}