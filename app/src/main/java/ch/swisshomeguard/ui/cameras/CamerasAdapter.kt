package ch.swisshomeguard.ui.cameras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.swisshomeguard.BASE_URL
import ch.swisshomeguard.R
import ch.swisshomeguard.model.system.SystemDevice
import ch.swisshomeguard.utils.Headers
import com.aminography.redirectglide.GlideApp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.camera_item.view.*

class CamerasAdapter(private val viewModel: CamerasViewModel) :
    ListAdapter<SystemDevice, CamerasAdapter.ViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.camera_item,
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
        private val monitoringNo = ResourcesCompat.getDrawable(
            itemView.resources,
            R.drawable.ic_monitoring_state_no,
            null
        )

        init {
            itemView.setOnClickListener {
                val camera = getItem(layoutPosition)
                viewModel.navigateToCameraDetails(camera)
            }
        }

        fun bind(item: SystemDevice) {
            with(itemView) {
                showPreviewImage(item.urls.preview)

                cameraName.text = item.name

                item.isRecording?.let {
                    with(monitoringStateIcon) {
                        visibility = View.VISIBLE
                        setImageDrawable(if (it) monitoringYes else monitoringNo)
                    }
                }
            }
        }

        private fun View.showPreviewImage(cameraImageUrl: String?) {
            GlideApp.with(context)
                .load(Headers.getUrlWithHeaders("$BASE_URL$cameraImageUrl"))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .placeholder(R.drawable.ic_outline_image)
                .error(R.drawable.ic_broken_image)
                .into(cameraImage)
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<SystemDevice>() {
    override fun areItemsTheSame(oldItem: SystemDevice, newItem: SystemDevice): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SystemDevice, newItem: SystemDevice): Boolean {
        return oldItem == newItem
    }
}