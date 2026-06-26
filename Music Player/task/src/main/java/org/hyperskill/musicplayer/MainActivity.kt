package org.hyperskill.musicplayer

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.hyperskill.musicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActions()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.mainMenuAddPlaylist -> {
            Toast.makeText(this, R.string.search_error_no_songs_loaded, Toast.LENGTH_SHORT).show()
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

    private fun initActions() {
        binding.mainButtonSearch.setOnClickListener {
            Toast.makeText(this, R.string.search_error_no_songs_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLoadPlaylistDialog() = AlertDialog.Builder(this)
        .setTitle(R.string.dialog_load_playlist_title)
        .setNegativeButton(android.R.string.cancel, null)

    private fun getDeletePlaylistDialog() = AlertDialog.Builder(this)
        .setTitle(R.string.dialog_delete_playlist_title)
        .setNegativeButton(android.R.string.cancel, null)
}