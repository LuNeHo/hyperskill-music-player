package org.hyperskill.musicplayer.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.hyperskill.musicplayer.R
import org.hyperskill.musicplayer.databinding.FragmentMainPlayerControllerBinding
import org.hyperskill.musicplayer.presentation.MusicPlayerViewModel
import org.hyperskill.musicplayer.presentation.UserIntent

class MainPlayerControllerFragment : Fragment() {
    private lateinit var binding: FragmentMainPlayerControllerBinding
    private val viewModel: MusicPlayerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainPlayerControllerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            controllerTvCurrentTime.text = getText(R.string.player_controller_init_time)
            controllerTvTotalTime.text = getText(R.string.player_controller_init_time)
            controllerBtnPlayPause.text = getText(R.string.controller_btn_play_pause)
            controllerBtnStop.text = getText(R.string.controller_btn_stop)
            controllerBtnPlayPause.setOnClickListener { viewModel.handleIntent(UserIntent.PlayPauseSong) }
            controllerBtnStop.setOnClickListener { viewModel.handleIntent(UserIntent.StopSong) }
        }
    }
}