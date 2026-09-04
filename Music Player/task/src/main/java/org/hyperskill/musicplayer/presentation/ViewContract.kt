package org.hyperskill.musicplayer.presentation

import org.hyperskill.musicplayer.model.Playlist
import org.hyperskill.musicplayer.model.Song.SongSelector

data class UiState(
    val uiEvent: UiEvent? = null,
    val isReadyToPlayMusic: Boolean? = null,
    val playlists: Map<String, Playlist> = mapOf(),
    val currentPlaylist: Playlist = Playlist(),
    val songSelectors: List<SongSelector> = emptyList()
)

sealed interface UserIntent {
    data class ClickSong(val position: Int) : UserIntent
    data class LongClickSong(val position: Int) : UserIntent
    data class AddPlaylist(val playlistName: String) : UserIntent
    data class LoadPlaylist(val playlistName: String) : UserIntent
    data class DeletePlaylist(val playlistName: String) : UserIntent
    data object ConsumeEvent : UserIntent
    data object Search : UserIntent
    data object PlayPauseSong : UserIntent
    data object StopSong : UserIntent
    data object DisplaySongSelection : UserIntent
    data object QuitSongSelection : UserIntent
    data object DisplayLoadPlaylistOption : UserIntent
    data object DisplayDeletePlaylistOption : UserIntent
}

sealed interface UiEvent {
    data class ShowToast(val message: ToastMessage) : UiEvent
    data class ShowLoadPlaylistDialog(val playlists: Map<String, Playlist>) : UiEvent
    data class ShowDeletePlaylistDialog(val playlists: Map<String, Playlist>) : UiEvent
}

enum class ToastMessage {
    NO_SONGS_LOADED,
    NO_SONG_SELECTED,
    NO_NAME,
    ALL_SONGS_NAME,
}