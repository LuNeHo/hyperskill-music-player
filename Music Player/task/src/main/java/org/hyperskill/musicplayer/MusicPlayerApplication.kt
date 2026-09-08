package org.hyperskill.musicplayer

import android.app.Application
import org.hyperskill.musicplayer.data.AndroidAudioPlayerDataSource
import org.hyperskill.musicplayer.domain.AudioPlayerDataSource

class MusicPlayerApplication : Application() {
    val audioPlayerDataSource: AudioPlayerDataSource by lazy {
        AndroidAudioPlayerDataSource(applicationContext)
    }
}