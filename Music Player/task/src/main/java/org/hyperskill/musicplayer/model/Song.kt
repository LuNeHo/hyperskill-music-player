package org.hyperskill.musicplayer.model

sealed interface Song {

    data class Track(
        val id: Int,
        val title: String,
        val artist: String,
        val duration: Long,
        var state: TrackState = TrackState.STOPPED
    ) : Song

    data class SongSelector(
        val id: Int,
        val title: String,
        val artist: String,
        val duration: Long,
        var isSelected: Boolean = false
    ) : Song
}

enum class TrackState { PLAYING, PAUSED, STOPPED }
