package org.hyperskill.musicplayer.model

import org.hyperskill.musicplayer.model.Song.Track

data class Playlist(
    val tracks: List<Track> = emptyList(),
    val currentTrack: Track? = null
)