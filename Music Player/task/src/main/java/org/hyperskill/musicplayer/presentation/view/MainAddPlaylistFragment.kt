package org.hyperskill.musicplayer.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.hyperskill.musicplayer.databinding.FragmentMainAddPlaylistBinding

class MainAddPlaylistFragment : Fragment() {
    private lateinit var binding: FragmentMainAddPlaylistBinding

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
            addPlaylistBtnCancel.setOnClickListener {}
            addPlaylistBtnOk.setOnClickListener {}
        }
    }
}