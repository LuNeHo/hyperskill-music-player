package org.hyperskill.musicplayer.model

import org.hyperskill.musicplayer.model.Song.Track

data class Playlist(val songs: List<Song> = emptyList(), val currentTrack: Track? = null)