package org.hyperskill.musicplayer.presentation.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.hyperskill.musicplayer.R
import org.hyperskill.musicplayer.databinding.ListItemSongBinding
import org.hyperskill.musicplayer.databinding.ListItemSongSelectorBinding
import org.hyperskill.musicplayer.domain.model.Song
import org.hyperskill.musicplayer.domain.model.Song.SongSelector
import org.hyperskill.musicplayer.domain.model.Song.Track
import org.hyperskill.musicplayer.domain.model.TrackState
import java.text.SimpleDateFormat
import java.util.EnumSet
import java.util.Locale

class SongsAdapter(val itemSongListener: OnSongInteraction) :
    ListAdapter<Song, RecyclerView.ViewHolder>(DiffCallback) {
    private val format = SimpleDateFormat("mm:ss", Locale.getDefault())
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TRACK -> TrackViewHolder(ListItemSongBinding.inflate(layoutInflater, parent, false))
            SELECTOR -> SongSelectorViewHolder(
                ListItemSongSelectorBinding.inflate(layoutInflater, parent, false)
            )

            else -> throw AssertionError()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        val changes =
            if (payloads.isEmpty()) emptySet<ChangeField>()
            else EnumSet.noneOf(ChangeField::class.java).also { changes ->
                payloads.forEach { payload ->
                    (payload as? Collection<*>)?.filterIsInstanceTo(changes)
                }
            }
        when (val song = getItem(position)) {
            is Track -> with(holder as TrackViewHolder) {
                when {
                    changes.isEmpty() -> bind(song)
                    ChangeField.STATE in changes -> bindState(song.state)
                }
            }

            is SongSelector -> with(holder as SongSelectorViewHolder) {
                when {
                    changes.isEmpty() -> bind(song)
                    ChangeField.IS_SELECTED in changes -> bindIsSelected(song.isSelected)
                }
            }

            else -> throw UnsupportedOperationException()
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Track -> TRACK
        is SongSelector -> SELECTOR
    }

    interface OnSongInteraction {
        fun onSongClick(itemPosition: Int)
        fun onSongLongClick(itemPosition: Int)
    }

    inner class TrackViewHolder(private val binding: ListItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.songItemImgBtnPlayPause.setOnClickListener {
                itemSongListener.onSongClick(adapterPosition)
            }
            binding.root.setOnLongClickListener {
                itemSongListener.onSongLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }

        fun bind(song: Track) {
            bindState(song.state)
            binding.apply {
                songItemTvArtist.text = song.artist
                songItemTvTitle.text = song.title
                songItemTvDuration.text = format.format(song.duration)
            }
        }

        fun bindState(trackState: TrackState) {
            binding.songItemImgBtnPlayPause.setImageResource(
                if (trackState == TrackState.PLAYING) R.drawable.ic_pause
                else R.drawable.ic_play
            )
        }
    }

    inner class SongSelectorViewHolder(private val binding: ListItemSongSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemSongListener.onSongClick(adapterPosition)
            }
        }

        fun bind(song: SongSelector) {
            bindIsSelected(song.isSelected)
            binding.apply {
                songSelectorItemTvArtist.text = song.artist
                songSelectorItemTvTitle.text = song.title
                songSelectorItemTvDuration.text = format.format(song.duration)
            }
        }

        fun bindIsSelected(isSelected: Boolean) {
            binding.apply {
                root.setBackgroundColor(
                    if (isSelected)
                        Color.LTGRAY
                    else
                        Color.WHITE
                )
                songSelectorItemCheckBox.isChecked = isSelected
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = when (oldItem) {
            is Track -> newItem is Track && oldItem.id == newItem.id
            is SongSelector -> newItem is SongSelector && oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem == newItem

        override fun getChangePayload(oldItem: Song, newItem: Song): Any? = listOfNotNull(
            ChangeField.STATE.takeIf { oldItem is Track && newItem is Track && oldItem.state != newItem.state },
            ChangeField.IS_SELECTED.takeIf { oldItem is SongSelector && newItem is SongSelector && oldItem.isSelected != newItem.isSelected }
        ).ifEmpty { null }
    }

    enum class ChangeField {
        STATE, IS_SELECTED,
    }

    companion object {
        const val TRACK = 0
        const val SELECTOR = 1
    }
}