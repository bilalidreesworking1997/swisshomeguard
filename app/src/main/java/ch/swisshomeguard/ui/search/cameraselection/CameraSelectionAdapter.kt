package ch.swisshomeguard.ui.search.cameraselection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.swisshomeguard.R
import ch.swisshomeguard.model.dummy.DummyCamera
import ch.swisshomeguard.utils.Headers
import com.aminography.redirectglide.GlideApp
import kotlinx.android.synthetic.main.camera_selection_item.view.*

class CameraSelectionAdapter(private val viewModel: CameraSelectionViewModel) :
    ListAdapter<DummyCamera, CameraSelectionAdapter.ViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.camera_selection_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val monitoringYes = ResourcesCompat.getDrawable(
            itemView.resources,
            R.drawable.ic_monitoring_state_yes,
            null
        )
        private val monitoringNo =
            ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_monitoring_state_no, null)
        private val alarmYes =
            ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_alarm_state_yes, null)
        private val alarmNo =
            ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_alarm_state_no, null)

        init {
            itemView.setOnClickListener {
                val camera = getItem(layoutPosition)
//                viewModel.navigateToEventDetails(camera.camera_id)
            }
        }

        fun bind(item: DummyCamera) = with(itemView) {
            GlideApp.with(context)
                // TODO show real image when it is available from the server
                .load(Headers.getUrlWithHeaders(context.getString(R.string.dummy_camera_image)))
                .centerCrop()
                .placeholder(R.drawable.ic_outline_image)
                .error(R.drawable.ic_broken_image)
                .into(eventImage)


            item.detecting?.let {
                cameraSelected.isChecked = it
//                monitoringStateIcon.setImageDrawable(if (it) monitoringYes else monitoringNo)
            }
//            item.active_alarm?.let {
//                alarmStateText.text = if (it) "active alarm" else "inactive alarm"
//                alarmStateIcon.setImageDrawable(if (it) alarmYes else alarmNo)
//            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<DummyCamera>() {
    override fun areItemsTheSame(oldItem: DummyCamera, newItem: DummyCamera): Boolean {
        return oldItem.camera_id == newItem.camera_id
    }

    override fun areContentsTheSame(oldItem: DummyCamera, newItem: DummyCamera): Boolean {
        return oldItem == newItem
    }
}