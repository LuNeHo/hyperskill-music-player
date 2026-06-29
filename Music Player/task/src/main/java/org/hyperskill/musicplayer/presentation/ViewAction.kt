package org.hyperskill.musicplayer.presentation

sealed interface ViewAction {
    data class PlayOrPauseSong(val position: Int) : ViewAction
    data class AddSongToPlaylist(val position: Int) : ViewAction
    data object DisplaySongSelection : ViewAction
    data object Search : ViewAction
}