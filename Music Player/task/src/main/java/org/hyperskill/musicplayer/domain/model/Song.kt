package org.hyperskill.musicplayer.domain.model

sealed interface Song {
    data class Track(
        val state: TrackState = TrackState.STOPPED,
        val id: Int,
        val title: String,
        val artist: String,
        val duration: Long
    ) : Song

    data class SongSelector(
        val isSelected: Boolean = false,
        val id: Int,
        val title: String,
        val artist: String,
        val duration: Long
    ) : Song
}

enum class TrackState { PLAYING, PAUSED, STOPPED }
