package org.hyperskill.musicplayer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hyperskill.musicplayer.domain.AudioPlayerDataSource
import org.hyperskill.musicplayer.domain.model.Playlist
import org.hyperskill.musicplayer.domain.model.Song.SongSelector
import org.hyperskill.musicplayer.domain.model.Song.Track
import org.hyperskill.musicplayer.domain.model.TrackState

class MusicPlayerViewModel(private val audioPlayer: AudioPlayerDataSource) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var currentPlaylistName = ""
    private var progressJob: Job? = null

    init {
        audioPlayer.setOnCompletionListener { handleIntent(UserIntent.StopSong) }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTracker()
    }

    fun handleIntent(intent: UserIntent) {
        when (intent) {
            UserIntent.ConsumeEvent -> updateState { copy(uiEvent = null) }
            UserIntent.Search -> showAllSongs()
            UserIntent.PlayPauseSong -> playCurrentSong()
            UserIntent.StopSong -> stopCurrentSong()
            UserIntent.DisplaySongSelection -> if (_uiState.value.isReadyToPlayMusic != false) enableSelectionMode()
            UserIntent.QuitSongSelection -> updateState { copy(isReadyToPlayMusic = true) }
            UserIntent.DisplayLoadPlaylistOption -> updateState {
                copy(uiEvent = UiEvent.ShowLoadPlaylistDialog(playlists))
            }

            UserIntent.DisplayDeletePlaylistOption -> updateState {
                copy(uiEvent = UiEvent.ShowDeletePlaylistDialog(playlists))
            }

            UserIntent.SuspendSongProgress -> stopProgressTracker()

            is UserIntent.SeekTo -> seekTo(intent.progress)

            is UserIntent.ClickSong -> {
                if (_uiState.value.isReadyToPlayMusic == true) playSong(intent.position)
                else selectSong(intent.position)
            }

            is UserIntent.LongClickSong -> if (_uiState.value.isReadyToPlayMusic == true) {
                enableSelectionMode()
                selectSong(intent.position)
            }

            is UserIntent.AddPlaylist -> addPlaylist(intent.playlistName)
            is UserIntent.LoadPlaylist -> updateCurrentPlaylist(intent.playlistName)
            is UserIntent.DeletePlaylist -> deletePlaylist(intent.playlistName)
            is UserIntent.UpdateProgress -> updateState { copy(currentSongProgress = intent.progress) }
        }
    }

    private fun updateState(reducer: UiState.() -> UiState) {
        _uiState.update { it.reducer() }
    }

    private fun showAllSongs() {
        val updatedPlaylists = _uiState.value.playlists.toMutableMap()

        updatedPlaylists[ALL_SONGS] = getAllSongs()
        updatedPlaylists[ALL_SONGS]?.let {
            when (_uiState.value.isReadyToPlayMusic) {
                true -> {
                    updateState { copy(playlists = updatedPlaylists, currentPlaylist = it) }
                    currentPlaylistName = ALL_SONGS
                }

                false -> updateState { copy(songSelectors = it.tracks.map { it.toSongSelector() }) }

                null -> {
                    updateState {
                        copy(
                            isReadyToPlayMusic = true,
                            playlists = updatedPlaylists,
                            currentPlaylist = it
                        )
                    }
                    currentPlaylistName = ALL_SONGS
                }
            }
        }
    }

    private fun getAllSongs(): Playlist {
        val allSongs: MutableList<Track> = mutableListOf()

        for (i in 0..9) {
            allSongs.add(
                Track(
                    id = i + 1,
                    title = "title${i + 1}",
                    artist = "artist${i + 1}",
                    duration = 215_000
                )
            )
        }
        return Playlist(allSongs)
    }

    private fun playCurrentSong() {
        _uiState.value.currentPlaylist.currentTrack?.let { currentTrack ->
            playAudio(currentTrack.state)
            val newTrackState = when (currentTrack.state) {
                TrackState.PLAYING -> TrackState.PAUSED
                TrackState.PAUSED, TrackState.STOPPED -> TrackState.PLAYING
            }
            val newTracks = _uiState.value.currentPlaylist.tracks.map {
                if (it.id == currentTrack.id) it.copy(state = newTrackState) else it
            }
            val newCurrentTrack = newTracks.find { it.id == currentTrack.id }
            updateState { copy(currentPlaylist = Playlist(newTracks, newCurrentTrack)) }
        }
    }

    private fun stopCurrentSong() {
        _uiState.value.currentPlaylist.currentTrack?.let { currentTrack ->
            stopAudio()
            val newTracks = _uiState.value.currentPlaylist.tracks.map {
                if (it.id == currentTrack.id) it.copy(state = TrackState.STOPPED) else it
            }
            val newCurrentTrack = newTracks.find { it.id == currentTrack.id }
            updateState { copy(currentPlaylist = Playlist(newTracks, newCurrentTrack)) }
        }
    }

    private fun playSong(position: Int) {
        val selectedTrack = _uiState.value.currentPlaylist.tracks[position]
        val isDifferentSong = selectedTrack.id != _uiState.value.currentPlaylist.currentTrack?.id

        if (isDifferentSong) {
            audioPlayer.stop()
            stopProgressTracker()
            audioPlayer.play()
            startProgressTracker()
        } else {
            playAudio(selectedTrack.state)
        }
        val newTrackState = if (isDifferentSong) TrackState.PLAYING
        else when (selectedTrack.state) {
            TrackState.PLAYING -> TrackState.PAUSED
            TrackState.PAUSED, TrackState.STOPPED -> TrackState.PLAYING
        }
        val newTracks = _uiState.value.currentPlaylist.tracks.map {
            if (it.id == selectedTrack.id) it.copy(state = newTrackState)
            else it.copy(state = TrackState.STOPPED)
        }
        val newCurrentTrack = newTracks.find { it.id == selectedTrack.id }
        updateState {
            copy(
                currentPlaylist = Playlist(newTracks, newCurrentTrack),
                currentSongProgress = if (isDifferentSong) 0 else currentSongProgress
            )
        }
    }

    private fun playAudio(trackState: TrackState) {
        when (trackState) {
            TrackState.PLAYING -> {
                audioPlayer.pause()
                stopProgressTracker()
            }

            TrackState.PAUSED -> {
                audioPlayer.resume()
                startProgressTracker()
            }

            TrackState.STOPPED -> {
                audioPlayer.play(_uiState.value.currentSongProgress)
                startProgressTracker()
            }
        }
    }

    private fun seekTo(positionMs: Int) {
        audioPlayer.seekTo(positionMs)
        updateState { copy(currentSongProgress = positionMs) }
        if (audioPlayer.isPlaying()) startProgressTracker()
    }

    private fun stopAudio() {
        audioPlayer.stop()
        stopProgressTracker()
        updateState { copy(currentSongProgress = 0) }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(500)
                if (audioPlayer.isPlaying()) updateState {
                    copy(currentSongProgress = audioPlayer.getCurrentPosition())
                }
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun enableSelectionMode() {
        val allSongsPlaylist = _uiState.value.playlists[ALL_SONGS]?.tracks ?: emptyList()

        if (allSongsPlaylist.isEmpty()) updateState {
            copy(uiEvent = UiEvent.ShowToast(ToastMessage.NO_SONGS_LOADED))
        }
        else updateState {
            val newSongSelectors = allSongsPlaylist.map { it.toSongSelector() }
            copy(isReadyToPlayMusic = false, songSelectors = newSongSelectors)
        }
    }

    private fun selectSong(position: Int) {
        val selectedSong = _uiState.value.songSelectors[position]
        val newSongSelectors = _uiState.value.songSelectors.map {
            if (it.id == selectedSong.id) it.copy(isSelected = !it.isSelected)
            else it
        }
        updateState { copy(songSelectors = newSongSelectors) }
    }

    private fun updateCurrentPlaylist(playlistName: String) {
        val selectedPlaylist = _uiState.value.playlists[playlistName]

        selectedPlaylist?.let { playlist ->
            val currentTrack = _uiState.value.currentPlaylist.currentTrack
            if (_uiState.value.isReadyToPlayMusic == true) {
                val newTracks = playlist.tracks.map {
                    if (it.id == currentTrack?.id) it.copy(state = currentTrack.state) else it
                }
                val isCurrentTrackStillPresent = newTracks.any { it.id == currentTrack?.id }
                val newCurrentTrack = newTracks.find { it.id == currentTrack?.id }
                val newPlaylist = Playlist(newTracks, newCurrentTrack ?: newTracks.first())
                if (!isCurrentTrackStillPresent) {
                    audioPlayer.stop()
                    stopProgressTracker()
                }
                updateState {
                    copy(
                        currentPlaylist = newPlaylist,
                        currentSongProgress = if (isCurrentTrackStillPresent) currentSongProgress else 0
                    )
                }
                currentPlaylistName = playlistName
            } else {
                val selectedSongsIds =
                    _uiState.value.songSelectors.filter { it.isSelected }.map { it.id }
                val newSongSelectors = playlist.tracks.map {
                    if (it.id in selectedSongsIds) it.toSongSelector(true)
                    else it.toSongSelector()
                }
                updateState { copy(songSelectors = newSongSelectors) }
            }
        }
    }

    private fun addPlaylist(playlistName: String) {
        val selectedTracks =
            _uiState.value.songSelectors.filter { it.isSelected }.map { it.toTrack() }

        updateState {
            when {
                selectedTracks.isEmpty() -> copy(uiEvent = UiEvent.ShowToast(ToastMessage.NO_SONG_SELECTED))
                playlistName.isBlank() -> copy(uiEvent = UiEvent.ShowToast(ToastMessage.NO_NAME))
                playlistName == ALL_SONGS -> copy(uiEvent = UiEvent.ShowToast(ToastMessage.ALL_SONGS_NAME))
                else -> {
                    val updatedPlaylists = playlists.toMutableMap()
                    updatedPlaylists[playlistName] = Playlist(tracks = selectedTracks)
                    copy(isReadyToPlayMusic = true, playlists = updatedPlaylists)
                }
            }
        }
    }

    private fun deletePlaylist(playlistName: String) {
        val updatedPlaylists = _uiState.value.playlists.toMutableMap()
        val allSongsPlaylist = _uiState.value.playlists[ALL_SONGS] ?: getAllSongs()

        when {
            _uiState.value.isReadyToPlayMusic == false -> updateState {
                val newSongSelectors = allSongsPlaylist.tracks.map { it.toSongSelector() }
                copy(currentPlaylist = allSongsPlaylist, songSelectors = newSongSelectors)
            }

            currentPlaylistName == playlistName -> updateState { copy(currentPlaylist = allSongsPlaylist) }
        }
        updatedPlaylists.remove(playlistName)
        updateState { copy(playlists = updatedPlaylists) }
    }

    private fun Track.toSongSelector(isSelected: Boolean = false) = SongSelector(
        isSelected = isSelected,
        id = this.id,
        title = this.title,
        artist = this.artist,
        duration = this.duration
    )

    private fun SongSelector.toTrack() = Track(
        id = this.id,
        title = this.title,
        artist = this.artist,
        duration = this.duration
    )

    companion object {
        const val ALL_SONGS = "All Songs"
    }
}

class MusicPlayerViewModelFactory(private val audioPlayer: AudioPlayerDataSource) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicPlayerViewModel::class.java)) {
            return MusicPlayerViewModel(audioPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}