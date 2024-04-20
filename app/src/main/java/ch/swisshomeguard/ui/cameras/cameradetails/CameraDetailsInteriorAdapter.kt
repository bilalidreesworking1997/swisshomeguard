package ch.swisshomeguard.ui.cameras.cameradetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.swisshomeguard.BASE_URL
import ch.swisshomeguard.R
import ch.swisshomeguard.model.events.Event
import ch.swisshomeguard.model.events.EventRecording
import ch.swisshomeguard.utils.Headers
import com.aminography.redirectglide.GlideApp
import kotlinx.android.synthetic.main.camera_details_interior_item.view.*

class CameraDetailsInteriorAdapter(
    private val cameraDetailsViewModel: CameraDetailsViewModel,
    private val event: Event
) : ListAdapter<EventRecording, CameraDetailsInteriorAdapter.ViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CameraDetailsInteriorAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.camera_details_interior_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CameraDetailsInteriorAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                val position = layoutPosition
                val streams = emptyList<String>().toMutableList()
                event.eventRecordings.forEach { eventRecording ->
                    eventRecording.urls.stream?.let {
                        streams.add(it)
                    }
                }
                cameraDetailsViewModel.navigateToEventPlayer(streams, position)
            }
        }

        fun bind(item: EventRecording) = with(itemView) {
            GlideApp.with(context)
                .load(Headers.getUrlWithHeaders("$BASE_URL${item.urls.preview}"))
                .centerCrop()
                .placeholder(R.drawable.ic_outline_image)
                .error(R.drawable.ic_broken_image)
                .into(recordingImage)
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<EventRecording>() {
        override fun areItemsTheSame(oldItem: EventRecording, newItem: EventRecording): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EventRecording, newItem: EventRecording): Boolean {
            return oldItem == newItem
        }
    }
}