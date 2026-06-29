package org.hyperskill.musicplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.hyperskill.musicplayer.model.Playlist
import org.hyperskill.musicplayer.model.Song.SongSelector
import org.hyperskill.musicplayer.model.Song.Track
import org.hyperskill.musicplayer.model.TrackState
import org.hyperskill.musicplayer.presentation.ViewAction

class MusicPlayerViewModel : ViewModel() {
    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()

    fun doOnAction(action: ViewAction) {
        when (action) {
            ViewAction.DisplaySongSelection -> _viewState.update { it.copy(isReadyToPlayMusic = false) }
            ViewAction.Search -> showAllSongs()
            is ViewAction.PlayOrPauseSong -> {
                if (_viewState.value.isReadyToPlayMusic == true) playSong(action.position)
            }

            is ViewAction.AddSongToPlaylist -> {
                if (_viewState.value.isReadyToPlayMusic == true) addSongToPlaylist(action.position)
            }
        }
    }

    private fun showAllSongs() {
        val updatedPlaylists = _viewState.value.playlists.toMutableList()
        val allSongsPlaylist = getAllSongs()

        if (!updatedPlaylists.contains(allSongsPlaylist)) updatedPlaylists.add(allSongsPlaylist)
        _viewState.update {
            it.copy(
                isReadyToPlayMusic = true,
                playlists = updatedPlaylists,
                currentPlaylist = allSongsPlaylist
            )
        }
    }

    private fun getAllSongs(): Playlist {
        val allSongs = listOf(
            Track(id = 1, title = "title1", artist = "artist1", duration = 215_000),
            Track(id = 2, title = "title2", artist = "artist2", duration = 215_000),
            Track(id = 3, title = "title3", artist = "artist3", duration = 215_000),
            Track(id = 4, title = "title4", artist = "artist4", duration = 215_000),
            Track(id = 5, title = "title5", artist = "artist5", duration = 215_000),
            Track(id = 6, title = "title6", artist = "artist6", duration = 215_000),
            Track(id = 7, title = "title7", artist = "artist7", duration = 215_000),
            Track(id = 8, title = "title8", artist = "artist8", duration = 215_000),
            Track(id = 9, title = "title9", artist = "artist9", duration = 215_000),
            Track(id = 10, title = "title10", artist = "artist10", duration = 215_000)
        )
        return Playlist(allSongs, allSongs.first())
    }

    private fun enableSelectionMode() {
        _viewState.update { currentState ->
            currentState.copy(
                isReadyToPlayMusic = false,
                currentPlaylist = currentState.currentPlaylist.toSongSelectors()
            )
        }
    }

    private fun disableSelectionMode() {
        _viewState.update { currentState ->
            currentState.copy(
                isReadyToPlayMusic = true,
                currentPlaylist = currentState.currentPlaylist.toTracks()
            )
        }
    }

    private fun playSong(position: Int) {
        val currentPlaylist = _viewState.value.currentPlaylist
        val previousTrack = currentPlaylist.currentTrack
        val selectedTrack = currentPlaylist.songs[position] as Track
        val updatedTrackState = when (selectedTrack.state) {
            TrackState.PLAYING -> TrackState.PAUSED
            TrackState.PAUSED, TrackState.STOPPED -> TrackState.PLAYING
        }
        val updatedSongs = currentPlaylist.songs.map { song ->
            when {
                (song as Track).id == selectedTrack.id -> song.copy(state = updatedTrackState)
                song.id == previousTrack?.id && song.id != selectedTrack.id -> song.copy(state = TrackState.STOPPED)
                else -> song
            }
        }
        val updatedCurrentTrack = updatedSongs.first { it.id == selectedTrack.id }
        _viewState.update { it.copy(currentPlaylist = Playlist(updatedSongs, updatedCurrentTrack)) }
    }

    private fun addSongToPlaylist(position: Int) {
        enableSelectionMode()
        _viewState.update { it.copy(isReadyToPlayMusic = false) }
    }

    private fun Playlist.toSongSelectors(): Playlist {
        val transformedSongs = this.songs.map { song ->
            when (song) {
                is Track -> SongSelector(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    duration = song.duration,
                    isSelected = false
                )

                is SongSelector -> song
            }
        }
        return Playlist(songs = transformedSongs, currentTrack = currentTrack)
    }

    private fun Playlist.toTracks(): Playlist {
        val transformedSongs = this.songs.map { song ->
            when (song) {
                is SongSelector -> Track(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    duration = song.duration,
                    state = TrackState.STOPPED
                )

                is Track -> song
            }
        }
        return Playlist(songs = transformedSongs, currentTrack = currentTrack)
    }

    data class ViewState(
        val isReadyToPlayMusic: Boolean? = null,
        val playlists: List<Playlist> = emptyList(),
        val currentPlaylist: Playlist = Playlist()
    )
}