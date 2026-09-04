package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.hyperskill.musicplayer.databinding.ActivityMainBinding
import org.hyperskill.musicplayer.presentation.MusicPlayerViewModel
import org.hyperskill.musicplayer.presentation.MusicPlayerViewModel.Companion.ALL_SONGS
import org.hyperskill.musicplayer.presentation.ToastMessage
import org.hyperskill.musicplayer.presentation.UiEvent
import org.hyperskill.musicplayer.presentation.UiState
import org.hyperskill.musicplayer.presentation.UserIntent
import org.hyperskill.musicplayer.presentation.view.MainAddPlaylistFragment
import org.hyperskill.musicplayer.presentation.view.MainPlayerControllerFragment
import org.hyperskill.musicplayer.presentation.view.SongsAdapter

class MainActivity : AppCompatActivity(), SongsAdapter.OnSongInteraction {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MusicPlayerViewModel by viewModels()
    private val songsAdapter = SongsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        observeUIState()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.mainMenuAddPlaylist -> {
            viewModel.handleIntent(UserIntent.DisplaySongSelection)
            true
        }

        R.id.mainMenuLoadPlaylist -> {
            viewModel.handleIntent(UserIntent.DisplayLoadPlaylistOption)
            true
        }

        R.id.mainMenuDeletePlaylist -> {
            viewModel.handleIntent(UserIntent.DisplayDeletePlaylistOption)
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onSongClick(itemPosition: Int) {
        viewModel.handleIntent(UserIntent.ClickSong(itemPosition))
    }

    override fun onSongLongClick(itemPosition: Int) {
        viewModel.handleIntent(UserIntent.LongClickSong(itemPosition))
    }

    private fun initUI() {
        binding.mainButtonSearch.setOnClickListener { viewModel.handleIntent(UserIntent.Search) }
        binding.mainSongList.adapter = songsAdapter
    }

    private fun observeUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> renderUI(state) }
            }
        }
    }

    private fun renderUI(state: UiState) {
        var bottomController: Fragment? = null
        var tag = ""

        renderEvent(state.uiEvent)
        when (state.isReadyToPlayMusic) {
            true -> {
                songsAdapter.submitList(state.currentPlaylist.tracks)
                bottomController = MainPlayerControllerFragment()
                tag = PLAY_MUSIC
            }

            false -> {
                songsAdapter.submitList(state.songSelectors)
                bottomController = MainAddPlaylistFragment()
                tag = ADD_PLAYLIST
            }

            null -> {}
        }
        bottomController?.let {
            val currentFragment = supportFragmentManager.findFragmentByTag(tag)
            if (currentFragment == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragmentContainer, it, tag)
                    .commit()
            }
        }
    }

    private fun renderEvent(uiEvent: UiEvent?) {
        when (uiEvent) {
            is UiEvent.ShowToast -> getToast(uiEvent.message).show()
            is UiEvent.ShowLoadPlaylistDialog -> getLoadPlaylistDialog(uiEvent.playlists.keys.toTypedArray()).show()
            is UiEvent.ShowDeletePlaylistDialog -> {
                val deletePlaylists = uiEvent.playlists.keys.filterNot { it == ALL_SONGS }
                getDeletePlaylistDialog(deletePlaylists.toTypedArray()).show()
            }

            null -> {}
        }
        viewModel.handleIntent(UserIntent.ConsumeEvent)
    }

    private fun getToast(toastMessage: ToastMessage): Toast {
        val message = when (toastMessage) {
            ToastMessage.NO_SONGS_LOADED -> R.string.search_error_no_songs_loaded
            ToastMessage.NO_SONG_SELECTED -> R.string.add_playlist_error_no_song_selected
            ToastMessage.NO_NAME -> R.string.add_playlist_error_no_name
            ToastMessage.ALL_SONGS_NAME -> R.string.add_playlist_error_all_songs_name
        }
        return Toast.makeText(this, message, Toast.LENGTH_SHORT)
    }

    private fun getLoadPlaylistDialog(playlists: Array<String>) = AlertDialog.Builder(this)
        .setTitle(R.string.dialog_load_playlist_title)
        .setItems(playlists) { _, pos -> viewModel.handleIntent(UserIntent.LoadPlaylist(playlists[pos])) }
        .setNegativeButton(android.R.string.cancel, null)

    private fun getDeletePlaylistDialog(playlists: Array<String>) = AlertDialog.Builder(this)
        .setTitle(R.string.dialog_delete_playlist_title)
        .setItems(playlists) { _, pos -> viewModel.handleIntent(UserIntent.DeletePlaylist(playlists[pos])) }
        .setNegativeButton(android.R.string.cancel, null)

    companion object {
        const val PLAY_MUSIC = "PLAY_MUSIC"
        const val ADD_PLAYLIST = "ADD_PLAYLIST"
    }
}
