package org.hyperskill.musicplayer.presentation.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.hyperskill.musicplayer.R
import org.hyperskill.musicplayer.databinding.ActivityMainBinding
import org.hyperskill.musicplayer.presentation.ViewAction
import org.hyperskill.musicplayer.presentation.viewmodel.MusicPlayerViewModel

class MainActivity : AppCompatActivity(), SongsAdapter.OnSongInteraction {
    private lateinit var binding: ActivityMainBinding
    private val musicPlayerViewModel = MusicPlayerViewModel()
    private val songsAdapter = SongsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initActions()
        observeViewState()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.mainMenuAddPlaylist -> {
            musicPlayerViewModel.doOnAction(ViewAction.DisplaySongSelection)
            true
        }

        R.id.mainMenuLoadPlaylist -> {
            getLoadPlaylistDialog().show()
            true
        }

        R.id.mainMenuDeletePlaylist -> {
            getDeletePlaylistDialog().show()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onSongClick(itemPosition: Int) {
        musicPlayerViewModel.doOnAction(ViewAction.PlayOrPauseSong(itemPosition))
    }

    override fun onSongLongClick(itemPosition: Int) {
        musicPlayerViewModel.doOnAction(ViewAction.AddSongToPlaylist(itemPosition))
    }

    private fun initViews() {
        binding.mainSongList.adapter = songsAdapter
    }

    private fun initActions() {
        binding.mainButtonSearch.setOnClickListener {
            musicPlayerViewModel.doOnAction(ViewAction.Search)
        }
    }

    private fun observeViewState() {
        lifecycleScope.launch { musicPlayerViewModel.viewState.collect { state -> displayUI(state) } }
    }

    private fun displayUI(state: MusicPlayerViewModel.ViewState) {
        state.currentPlaylist
        songsAdapter.submitList(state.currentPlaylist.songs)
        displayFooter(state.isReadyToPlayMusic)
    }

    private fun displayFooter(isReadyToPlayMusic: Boolean?) {
        isReadyToPlayMusic?.let {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    if (it) MainPlayerControllerFragment() else MainAddPlaylistFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getLoadPlaylistDialog() = AlertDialog.Builder(this)
        .setTitle(R.string.dialog_load_playlist_title)
        .setNegativeButton(android.R.string.cancel, null)

    private fun getDeletePlaylistDialog() = AlertDialog.Builder(this)
        .setTitle(R.string.dialog_delete_playlist_title)
        .setNegativeButton(android.R.string.cancel, null)
}
