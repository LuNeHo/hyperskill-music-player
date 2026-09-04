package org.hyperskill.musicplayer.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.hyperskill.musicplayer.databinding.FragmentMainAddPlaylistBinding
import org.hyperskill.musicplayer.presentation.MusicPlayerViewModel
import org.hyperskill.musicplayer.presentation.UserIntent

class MainAddPlaylistFragment : Fragment() {
    private lateinit var binding: FragmentMainAddPlaylistBinding
    private val viewModel: MusicPlayerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainAddPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            addPlaylistBtnCancel.setOnClickListener {
                viewModel.handleIntent(UserIntent.QuitSongSelection)
            }
            addPlaylistBtnOk.setOnClickListener {
                val playlistName = binding.addPlaylistEtPlaylistName.text.toString()
                viewModel.handleIntent(UserIntent.AddPlaylist(playlistName))
            }
        }
    }
}