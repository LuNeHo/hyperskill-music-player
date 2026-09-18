package org.hyperskill.musicplayer.domain.model

import org.hyperskill.musicplayer.domain.model.Song.Track

data class Playlist(
    val tracks: List<Track> = emptyList(),
    val currentTrack: Track? = tracks.firstOrNull()
)