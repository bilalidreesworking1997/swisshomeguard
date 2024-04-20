package ch.swisshomeguard.ui.cameras.cameradetails

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.swisshomeguard.R
import ch.swisshomeguard.model.events.Event
import ch.swisshomeguard.ui.cameras.cameradetails.CameraDetailsViewModel.UiModel
import ch.swisshomeguard.utils.convertTimeDate
import kotlinx.android.synthetic.main.camera_details_item.view.*
import kotlinx.android.synthetic.main.loading_or_error_item.view.*

class CameraDetailsAdapter(private val cameraDetailsViewModel: CameraDetailsViewModel) :
    PagingDataAdapter<UiModel, RecyclerView.ViewHolder>(
        EventDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.camera_details_item) {
            EventViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.camera_details_item,
                    parent,
                    false
                )
            )
        } else {
            SeparatorViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.camera_details_separator_item,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UiModel.EventItem -> R.layout.camera_details_item
            is UiModel.SeparatorItem -> R.layout.camera_details_separator_item
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O) // TODO Why do we need this?
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val uiModel = getItem(position)
        uiModel.let { // TODO Why do we need this?
            when (uiModel) {
                is UiModel.EventItem -> (holder as EventViewHolder).bind(uiModel.event)
                is UiModel.SeparatorItem -> (holder as SeparatorViewHolder).bind(uiModel.description)
            }
        }
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Event) = with(itemView) {

            val str = item.eventCreatedAt
            eventTime.text = convertTimeDate(context, str)

            recordingsList.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            val cameraDetailsInteriorAdapter =
                CameraDetailsInteriorAdapter(cameraDetailsViewModel, item)
            recordingsList.adapter = cameraDetailsInteriorAdapter

            cameraDetailsInteriorAdapter.submitList(item.eventRecordings.toList())
        }
    }

    inner class SeparatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val description: TextView = itemView.findViewById(R.id.separator_description)

        fun bind(separatorText: String) {
            description.text = separatorText
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<UiModel>() {
        override fun areItemsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
            return (oldItem is UiModel.EventItem && newItem is UiModel.EventItem && oldItem.event.id == newItem.event.id
                    || oldItem is UiModel.SeparatorItem && newItem is UiModel.SeparatorItem && oldItem.description == newItem.description)
        }

        override fun areContentsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
            return oldItem == newItem
        }
    }

    class EventLoadStateViewHolder(
        itemView: View,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.retryButton.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Loading) {
                showLoading(itemView)
            } else if (loadState is LoadState.Error) {
                showError(loadState, itemView)
            }
        }

        private fun showLoading(view: View) {
            with(view) {
                progressBar.visibility = View.VISIBLE
                retryButton.visibility = View.GONE
                errorMessage.visibility = View.GONE
            }
        }

        private fun showError(loadState: LoadState.Error, view: View) {
            with(view) {
                progressBar.visibility = View.GONE
                retryButton.visibility = View.VISIBLE
                errorMessage.visibility = View.VISIBLE
                errorMessage.text = loadState.error.localizedMessage
            }
        }
    }

    class EventLoadStateAdapter(private val retry: () -> Unit) :
        LoadStateAdapter<EventLoadStateViewHolder>() {
        override fun onBindViewHolder(holder: EventLoadStateViewHolder, loadState: LoadState) {
            holder.bind(loadState)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            loadState: LoadState
        ): EventLoadStateViewHolder {
            return EventLoadStateViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.loading_or_error_item,
                    parent,
                    false
                ),
                retry
            )
        }
    }
}