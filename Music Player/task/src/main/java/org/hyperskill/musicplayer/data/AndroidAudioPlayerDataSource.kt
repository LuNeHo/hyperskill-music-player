package org.hyperskill.musicplayer.data

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import org.hyperskill.musicplayer.R
import org.hyperskill.musicplayer.domain.AudioPlayerDataSource

class AndroidAudioPlayerDataSource(private val context: Context) : AudioPlayerDataSource {
    private var mediaPlayer: MediaPlayer? = null
    private var isStopped = false
    private val songUriStr = "android.resource://${context.packageName ?: ""}/${R.raw.wisdom}"

    override fun play() {
        // In case a track is already playing
        stop()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, songUriStr.toUri())
            prepare()
            start()
            isStopped = false
            setOnCompletionListener {
                isStopped = true
                release()
            }
        }
    }

    override fun stop() {
        mediaPlayer?.let {
            it.stop()
            isStopped = true
            it.release()
            mediaPlayer = null
        }
    }

    override fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    override fun isPlaying(): Boolean = !isStopped
}