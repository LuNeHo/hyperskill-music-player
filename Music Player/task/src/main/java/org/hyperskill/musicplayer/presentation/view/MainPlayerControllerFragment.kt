package org.hyperskill.musicplayer.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.hyperskill.musicplayer.MusicPlayerApplication
import org.hyperskill.musicplayer.R
import org.hyperskill.musicplayer.databinding.FragmentMainPlayerControllerBinding
import org.hyperskill.musicplayer.presentation.MusicPlayerViewModel
import org.hyperskill.musicplayer.presentation.MusicPlayerViewModelFactory
import org.hyperskill.musicplayer.presentation.UiState
import org.hyperskill.musicplayer.presentation.UserIntent
import java.text.SimpleDateFormat
import java.util.Locale

class MainPlayerControllerFragment : Fragment(), SeekBar.OnSeekBarChangeListener {
    private lateinit var binding: FragmentMainPlayerControllerBinding
    private val viewModel: MusicPlayerViewModel by activityViewModels {
        val app = requireActivity().application as MusicPlayerApplication
        MusicPlayerViewModelFactory(app.audioPlayerDataSource)
    }
    private val format = SimpleDateFormat("mm:ss", Locale.getDefault())
    private var selectedProgress: Int = 0

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
            controllerSeekBar.setOnSeekBarChangeListener(this@MainPlayerControllerFragment)
            controllerBtnPlayPause.setOnClickListener { viewModel.handleIntent(UserIntent.PlayPauseSong) }
            controllerBtnStop.setOnClickListener { viewModel.handleIntent(UserIntent.StopSong) }
        }
        observeUIState()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            binding.controllerTvCurrentTime.text = format.format(progress * 1000L)
            selectedProgress = progress * 1000
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        viewModel.handleIntent(UserIntent.SuspendSongProgress)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        viewModel.handleIntent(UserIntent.SeekTo(selectedProgress))
    }

    private fun observeUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> renderUI(state) }
            }
        }
    }

    private fun renderUI(state: UiState) {
        val currentTrackDuration = state.currentPlaylist.currentTrack?.duration?.toInt()
        with(binding) {
            controllerSeekBar.max = (currentTrackDuration ?: 0) / 1000
            controllerSeekBar.progress = state.currentSongProgress / 1000
            controllerTvCurrentTime.text = format.format(state.currentSongProgress.toLong())
            controllerTvTotalTime.text = format.format(currentTrackDuration?.toLong() ?: 0)
        }
    }
}